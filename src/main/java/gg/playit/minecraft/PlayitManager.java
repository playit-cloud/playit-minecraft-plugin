package gg.playit.minecraft;

import gg.playit.control.PlayitControlChannel;
import gg.playit.messages.ControlFeedReader;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PlayitManager implements Runnable {
    static Logger log = Logger.getLogger(PlayitManager.class.getName());

    private final String secretKey;
    private final AtomicInteger state = new AtomicInteger(0);
    private final PlayitConnectionTracker tracker = new PlayitConnectionTracker();

    private final EventLoopGroup group;

    private final Server server;

    public PlayitManager(String secretKey, Server server, EventLoopGroup group) {
        this.secretKey = secretKey;
        this.server = server;
        this.group = group;
    }

    public volatile int connectionTimeoutSeconds = 30;

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_ONLINE = 2;
    public static final int STATE_ERROR_WAITING = 3;
    public static final int STATE_SHUTDOWN = 4;
    public static final int STATE_INVALID_AUTH = 5;

    public void shutdown() {
        state.compareAndSet(STATE_ONLINE, STATE_SHUTDOWN);
    }

    public int state() {
        return state.get();
    }

    @Override
    public void run() {
        if (!state.compareAndSet(STATE_OFFLINE, STATE_CONNECTING)) {
            return;
        }

        while (state.get() == STATE_CONNECTING) {
            try (PlayitControlChannel channel = PlayitControlChannel.setup(secretKey)) {
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
                                        group,
                                        tracker,
                                        key,
                                        new InetSocketAddress(Bukkit.getIp(), Bukkit.getPort()),
                                        new InetSocketAddress(InetAddress.getByAddress(newClient.claimAddress.ipBytes), Short.toUnsignedInt(newClient.claimAddress.portNumber)),
                                        newClient.claimToken,
                                        server,
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
                } if (state.get() == STATE_CONNECTING) {
                    log.info("failed to connect, retrying");
                } if (state.get() == STATE_INVALID_AUTH) {
                    log.info("invalid auth, done trying");
                }
            }
        }
    }
}
