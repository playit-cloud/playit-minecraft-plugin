package gg.playit.minecraft;

import gg.playit.control.PlayitControlChannel;
import gg.playit.messages.ControlFeedReader;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayitManager implements Runnable {
    private final String secretKey;
    private final AtomicInteger state = new AtomicInteger(0);
    private final PlayitConnectionTracker tracker;

    public PlayitManager(String secretKey, PlayitConnectionTracker tracker) {
        this.tracker = tracker;
        this.secretKey = secretKey;
    }

    private static final int STATE_OFFLINE = 0;
    private static final int STATE_ONLINE = 1;
    private static final int STATE_SHUTDOWN = 2;

    private final EventLoopGroup group = new NioEventLoopGroup();

    public void shutdown() {
        state.compareAndSet(STATE_ONLINE, STATE_SHUTDOWN);
    }

    @Override
    public void run() {
        if (!state.compareAndSet(STATE_OFFLINE, STATE_ONLINE)) {
            return;
        }

        try (PlayitControlChannel channel = PlayitControlChannel.setup(secretKey)) {
            while (state.get() == STATE_ONLINE) {
                var messageOpt = channel.update();
                if (messageOpt.isPresent()) {
                    var feedMessage = messageOpt.get();

                    if (feedMessage instanceof ControlFeedReader.NewClient newClient) {
                        System.out.println("Got new client " + feedMessage);

                        var key = newClient.peerAddr + "-" + newClient.connectAddr;
                        if (tracker.addConnection(key)) {
                            System.out.println("Start TCP connection to MC server");

                            new PlayitTcpTunnel(
                                    new InetSocketAddress(InetAddress.getByAddress(newClient.peerAddr.ipBytes), Short.toUnsignedInt(newClient.peerAddr.portNumber)),
                                    group,
                                    tracker,
                                    key,
                                    new InetSocketAddress(Bukkit.getIp(), Bukkit.getPort()),
                                    new InetSocketAddress(ipString(newClient.claimAddress.ipBytes), Short.toUnsignedInt(newClient.claimAddress.portNumber)),
                                    newClient.claimToken
                            ).start();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            state.set(STATE_OFFLINE);
        }
    }

    private static String ipString(byte[] ipBytes) {
        if (ipBytes.length == 4) {
            return String.format(
                    "%s.%s.%s.%s",
                    Byte.toUnsignedInt(ipBytes[0]), Byte.toUnsignedInt(ipBytes[1]),
                    Byte.toUnsignedInt(ipBytes[2]), Byte.toUnsignedInt(ipBytes[3])
            );
        }

        if (ipBytes.length == 16) {
            var sb = new StringBuilder();

            for (var i = 0; i < 16; ++i) {
                var b = ipBytes[i];

                if ((i % 2) == 0 && i != 0) {
                    sb.append(':');
                }

                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        }

        throw new RuntimeException("invalid ip length: " + ipBytes.length);
    }
}
