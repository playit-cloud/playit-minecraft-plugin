package gg.playit.minecraft;

import gg.playit.api.ApiClient;
import gg.playit.control.PlayitControlChannel;
import gg.playit.messages.ControlFeedReader;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PlayitManager implements Runnable {
    static Logger log = Logger.getLogger(PlayitManager.class.getName());
    private final AtomicInteger state = new AtomicInteger(STATE_INIT);
    private final PlayitConnectionTracker tracker = new PlayitConnectionTracker();

    private final PlayitBukkit plugin;

    public PlayitManager(PlayitBukkit plugin) {
        this.plugin = plugin;

        var secret = plugin.getConfig().getString(PlayitBukkit.CFG_AGENT_SECRET_KEY);
        if (secret != null && secret.length() < 32) {
            secret = null;
        }

        setup = new PlayitKeysSetup(secret, state);
    }

    private final PlayitKeysSetup setup;
    private PlayitKeysSetup.PlayitKeys keys;

    public volatile int connectionTimeoutSeconds = 30;
    public static final int STATE_INIT = -1;
    public static final int STATE_OFFLINE = 10;
    public static final int STATE_CONNECTING = 11;
    public static final int STATE_ONLINE = 12;
    public static final int STATE_ERROR_WAITING = 13;
    public static final int STATE_SHUTDOWN = 0;
    public static final int STATE_INVALID_AUTH = 15;

    public void shutdown() {
        state.compareAndSet(STATE_ONLINE, STATE_SHUTDOWN);
    }

    public int state() {
        return state.get();
    }

    @Override
    public void run() {
        /* make sure we don't run two instances */
        if (!state.compareAndSet(STATE_INIT, PlayitKeysSetup.STATE_INIT)) {
            return;
        }

        while (state.get() != STATE_SHUTDOWN) {
            try {
                keys = setup.progress();

                if (keys != null) {
                    log.info("keys and tunnel setup");
                    break;
                }
            } catch (IOException e) {
                log.severe("got error during setup: " + e);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignore) {
                }

                continue;
            }

            if (state.get() == PlayitKeysSetup.STATE_MISSING_SECRET) {
                for (var player : plugin.server.getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.sendMessage("Visit https://playit.gg/mc/" + setup.getClaimCode() + " to setup playit");
                    } else {
                        player.sendMessage("Check server logs to get playit.gg claim link to setup tunnel (or be a Server Operator)");
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignore) {
                }
            }
        }

        if (keys == null) {
            log.info("shutdown reached, tunnel connection never started");
            return;
        }

        plugin.getConfig().set(PlayitBukkit.CFG_AGENT_SECRET_KEY, keys.secretKey);
        plugin.saveConfig();

        if (keys.isGuest) {
            plugin.server.broadcastMessage("WARNING: playit.gg plugin is running with a guest account");
            plugin.server.broadcastMessage("see server console for setup URL");

            var api = new ApiClient(keys.secretKey);

            try {
                var key = api.createGuestWebSessionKey();
                var url = "https://playit.gg/login/guest-account/" + key;
                log.info("setup playit.gg account: " + url);

                if (state.get() == STATE_SHUTDOWN) {
                    return;
                }

                for (var player : plugin.server.getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.sendMessage("setup playit.gg account");
                        player.sendMessage("URL: " + url);
                    }
                }
            } catch (IOException e) {
                log.severe("failed to generate web session key: " + e);
            }
        } else if (keys.isEmailVerified) {
            plugin.server.broadcastMessage("WARNING: email associated with playit.gg account is not verified");
        }

        plugin.server.broadcastMessage("playit.gg tunnel address");
        plugin.server.broadcastMessage("ADDR: " + keys.tunnelAddress);

        if (state.get() == STATE_SHUTDOWN) {
            return;
        }

        state.set(STATE_CONNECTING);

        while (state.get() == STATE_CONNECTING) {
            try (PlayitControlChannel channel = PlayitControlChannel.setup(keys.secretKey)) {
                state.compareAndSet(STATE_CONNECTING, STATE_ONLINE);

                while (state.get() == STATE_ONLINE) {
                    var messageOpt = channel.update();
                    if (messageOpt.isPresent()) {
                        var feedMessage = messageOpt.get();

                        if (feedMessage instanceof ControlFeedReader.NewClient newClient) {
                            log.info("got new client: " + feedMessage);

                            var key = newClient.peerAddr + "-" + newClient.connectAddr;
                            if (tracker.addConnection(key)) {
                                log.info("starting tcp tunnel for client");

                                new PlayitTcpTunnel(
                                        new InetSocketAddress(InetAddress.getByAddress(newClient.peerAddr.ipBytes), Short.toUnsignedInt(newClient.peerAddr.portNumber)),
                                        plugin.eventGroup,
                                        tracker,
                                        key,
                                        new InetSocketAddress(Bukkit.getIp(), Bukkit.getPort()),
                                        new InetSocketAddress(InetAddress.getByAddress(newClient.claimAddress.ipBytes), Short.toUnsignedInt(newClient.claimAddress.portNumber)),
                                        newClient.claimToken,
                                        plugin.server,
                                        connectionTimeoutSeconds
                                ).start();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                state.compareAndSet(STATE_ONLINE, STATE_ERROR_WAITING);
                log.severe("failed when communicating with tunnel server, error: " + e);

                if (e.getMessage().contains("invalid authentication")) {
                    state.set(STATE_INVALID_AUTH);
                }

                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException ignore) {
                }
            } finally {
                if (state.compareAndSet(STATE_SHUTDOWN, STATE_OFFLINE)) {
                    log.info("control channel shutdown");
                } else if (state.compareAndSet(STATE_ERROR_WAITING, STATE_CONNECTING)) {
                    log.info("trying to connect again");
                } else if (state.compareAndSet(STATE_ONLINE, STATE_CONNECTING)) {
                    log.warning("unexpected state ONLINE, moving to CONNECTING");
                }
                if (state.get() == STATE_CONNECTING) {
                    log.info("failed to connect, retrying");
                }
                if (state.get() == STATE_INVALID_AUTH) {
                    log.info("invalid auth, done trying");
                }
            }
        }
    }
}
