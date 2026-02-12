package gg.playit.minecraft;

import gg.playit.api.ApiClient;
import gg.playit.api.model.ApiSuccess;
import gg.playit.api.model.ApiSuccessNoFail;
import gg.playit.api.model.enums.PlayitNetwork;
import gg.playit.api.model.enums.TunnelType;
import gg.playit.api.model.request.AccountTunnelOriginCreate;
import gg.playit.api.model.request.AgentOrigin;
import gg.playit.api.model.request.CreateTunnelEndpoint;
import gg.playit.api.model.request.ReqTunnelsCreateV1;
import gg.playit.api.model.request.TunnelProtocol;
import gg.playit.api.model.request.UseAllocRegion;
import gg.playit.api.model.response.AccountTunnelV1;
import gg.playit.api.model.response.AccountTunnelsV1;
import gg.playit.api.model.response.AgentNotice;
import gg.playit.api.model.response.AgentTunnelConfig;
import gg.playit.api.model.response.ConnectAddress;
import gg.playit.api.model.response.WebSession;
import gg.playit.control.PlayitControlChannel;
import gg.playit.messages.ControlFeedReader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
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

        setup = new PlayitKeysSetup(secret, state, PlayitConstants.VERSION_STRING);
    }

    private final PlayitKeysSetup setup;
    private volatile PlayitKeysSetup.PlayitKeys keys;

    public boolean isGuest() {
        return keys != null && keys.isGuest;
    }

    public boolean emailVerified() {
        return keys == null || keys.isEmailVerified;
    }

    public String getAddress() {
        if (keys == null) {
            return null;
        }
        return keys.tunnelAddress;
    }

    public AgentNotice getNotice() {
        var k = keys;
        if (k == null) {
            return null;
        }
        return k.notice;
    }

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

                if (state.get() == PlayitKeysSetup.STATE_CHECKING_SECRET) {
                    var currentKeys = setup.getKeys();
                    if (currentKeys != null && currentKeys.secretKey != null) {
                        var savedSecret = plugin.getConfig().getString(PlayitBukkit.CFG_AGENT_SECRET_KEY);
                        if (!currentKeys.secretKey.equals(savedSecret)) {
                            plugin.getConfig().set(PlayitBukkit.CFG_AGENT_SECRET_KEY, currentKeys.secretKey);
                            plugin.saveConfig();
                        }
                    }
                }

                if (keys != null) {
                    log.info("keys setup complete, ready to connect");
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
                var code = setup.getClaimCode();
                if (code != null) {
                    for (var player : plugin.server.getOnlinePlayers()) {
                        if (player.isOp()) {
                            player.sendMessage("Visit " + ChatColor.RED + "https://playit.gg/mc/" + code + ChatColor.RESET + " to setup playit");
                        } else {
                            player.sendMessage("Check server logs to get playit.gg claim link to setup tunnel (or be a Server Operator)");
                        }
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

        var savedSecret = plugin.getConfig().getString(PlayitBukkit.CFG_AGENT_SECRET_KEY);
        if (keys.secretKey != null && !keys.secretKey.equals(savedSecret)) {
            plugin.getConfig().set(PlayitBukkit.CFG_AGENT_SECRET_KEY, keys.secretKey);
            plugin.saveConfig();
        }

        if (keys.isGuest) {
            plugin.broadcast(ChatColor.RED + "WARNING: " + ChatColor.RESET + " plugin is running with a guest account");
            plugin.broadcast("see server console for setup URL");

            var api = new ApiClient(keys.secretKey);

            try {
                var result = api.loginGuest();
                String key = result instanceof ApiSuccess<WebSession, ?> success ? success.data().session_key() : null;
                if (key == null) {
                    log.severe("failed to generate web session key: " + result);
                } else {
                var url = "https://playit.gg/login/guest-account/" + key;
                log.info("setup playit.gg account: " + url);

                if (state.get() == STATE_SHUTDOWN) {
                    return;
                }

                for (var player : plugin.server.getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.sendMessage("setup playit.gg account");
                        player.sendMessage(ChatColor.RED + "URL: " + ChatColor.RESET + url);
                    }
                }
                }
            } catch (IOException e) {
                log.severe("failed to generate web session key: " + e);
            }
        } else if (!keys.isEmailVerified) {
            plugin.broadcast(ChatColor.RED + "WARNING: " + ChatColor.RESET + "email associated with playit.gg account is not verified");
        }

        if (state.get() == STATE_SHUTDOWN) {
            return;
        }

        state.set(STATE_CONNECTING);

        while (state.get() == STATE_CONNECTING) {
            try (PlayitControlChannel channel = PlayitControlChannel.setup(keys.secretKey, PlayitConstants.MINECRAFT_AGENT_VERSION)) {
                state.compareAndSet(STATE_CONNECTING, STATE_ONLINE);

                keys.tunnelAddress = ensureTunnelExists(keys);
                if (keys.tunnelAddress != null) {
                    plugin.broadcast("tunnel setup");
                    plugin.broadcast(keys.tunnelAddress);
                }

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

    private String ensureTunnelExists(PlayitKeysSetup.PlayitKeys keys) throws IOException {
        var api = new ApiClient(keys.secretKey);

        var tunnelsResult = api.v1TunnelsList();
        if (!(tunnelsResult instanceof ApiSuccessNoFail<AccountTunnelsV1> tunnelsSuccess)) {
            throw new IOException("tunnels list failed: " + tunnelsResult);
        }
        var tunnels = tunnelsSuccess.data();

        if (tunnels.tunnels() != null) {
            for (AccountTunnelV1 tunnel : tunnels.tunnels()) {
                if (tunnel.tunnel_type() == TunnelType.MinecraftJava) {
                    var addr = extractDisplayAddress(tunnel);
                    if (addr != null) {
                        log.info("found minecraft java tunnel: " + addr);
                        return addr;
                    }
                }
            }
        }

        log.info("create new minecraft java tunnel");

        var create = new ReqTunnelsCreateV1(
                "Minecraft",
                new TunnelProtocol.TunnelTypeDetail(TunnelType.MinecraftJava),
                new AccountTunnelOriginCreate.Agent(new AgentOrigin(keys.agentId, new AgentTunnelConfig())),
                new CreateTunnelEndpoint.Region(new UseAllocRegion(PlayitNetwork.Global, null)),
                true,
                null
        );

        api.v1TunnelsCreate(create);

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("interrupted while waiting for tunnel", e);
            }

            tunnelsResult = api.v1TunnelsList();
            if (tunnelsResult instanceof ApiSuccessNoFail<AccountTunnelsV1> tunnelsSuccess2) {
                var tunnelList = tunnelsSuccess2.data().tunnels();
                if (tunnelList != null) {
                    for (AccountTunnelV1 tunnel : tunnelList) {
                        if (tunnel.tunnel_type() == TunnelType.MinecraftJava) {
                            var addr = extractDisplayAddress(tunnel);
                            if (addr != null) {
                                log.info("found minecraft java tunnel: " + addr);
                                return addr;
                            }
                        }
                    }
                }
            }
        }

        log.warning("tunnel creation may have failed, address unavailable");
        return null;
    }

    private static String extractDisplayAddress(AccountTunnelV1 tunnel) {
        List<ConnectAddress> addrs = tunnel.connect_addresses();
        if (addrs == null || addrs.isEmpty()) return null;
        var first = addrs.get(0);
        if (first instanceof ConnectAddress.Ip4 ip4) return ip4.value().address() + ":" + ip4.value().default_port();
        if (first instanceof ConnectAddress.Ip6 ip6) return ip6.value().address() + ":" + ip6.value().default_port();
        if (first instanceof ConnectAddress.Addr4 addr4) return addr4.value().address();
        if (first instanceof ConnectAddress.Addr6 addr6) return addr6.value().address();
        if (first instanceof ConnectAddress.Auto auto) return auto.value().address();
        if (first instanceof ConnectAddress.Domain domain) return domain.value().address();
        return null;
    }
}
