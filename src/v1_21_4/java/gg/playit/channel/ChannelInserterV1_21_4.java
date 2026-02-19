package gg.playit.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * ChannelInserter for Minecraft 1.21.4 and later.
 * <p>
 * Delegates pipeline setup to Connection.addHandlers() to ensure exact vanilla
 * compatibility (including PacketBundleUnpacker/PacketBundlePacker for 1.21+).
 */
public final class ChannelInserterV1_21_4 extends BaseChannelInserter {

    public ChannelInserterV1_21_4(String version) {
        super(version);
    }

    @Override
    public boolean insertChannel(Server server, Channel tunnelChannel, InetSocketAddress trueIp,
                                 int connectionTimeoutSeconds, int rateLimit) {
        Class<?> serverConnectionClass = ServerConnectionListener.class;
        Class<?> connectionClass = Connection.class;
        Field connectionsField = findField(ServerConnectionListener.class, "connections");
        Field addressField = findField(Connection.class, "address");

        // 1. Get MinecraftServer
        Object minecraftServer = getMinecraftServer(server);
        if (minecraftServer == null) {
            log.info("failed to get Minecraft server from Bukkit.getServer()");
            return false;
        }

        // 2. Get ServerConnectionListener
        Object serverConnection = getServerConnection(minecraftServer, serverConnectionClass);
        if (serverConnection == null) {
            log.info("failed to get ServerConnectionListener from Minecraft Server");
            return false;
        }

        // 3. Create LegacyQueryHandler - 1.21.4+ takes MinecraftServer (implements ServerInfo)
        ChannelHandler legacyHandler;
        try {
            legacyHandler = new LegacyQueryHandler((MinecraftServer) minecraftServer);
        } catch (Exception e) {
            log.warning("LegacyQueryHandler construction failed: " + e);
            legacyHandler = createPassthroughHandler();
        }

        // 4. Create BandwidthDebugMonitor for Connection.addHandlers
        Object monitor = createBandwidthDebugMonitor();
        if (monitor == null) {
            log.info("BandwidthDebugMonitor creation failed");
            return false;
        }

        // 5. Create Connection - use RateKickingConnection if rate limit > 0
        Object serverBound = resolveEnumValue(PacketFlow.class, "SERVERBOUND");
        Object networkManager;
        if (rateLimit > 0) {
            try {
                networkManager = new RateKickingConnection(rateLimit);
            } catch (Exception e) {
                log.info("RateKickingConnection construction failed: " + e);
                networkManager = createConnection(serverBound);
                if (networkManager == null) return false;
            }
        } else {
            networkManager = createConnection(serverBound);
            if (networkManager == null) return false;
        }

        // 6. Create ServerHandshakePacketListenerImpl(MinecraftServer, Connection)
        Object handshakeListener;
        try {
            handshakeListener = new ServerHandshakePacketListenerImpl((MinecraftServer) minecraftServer, (Connection) networkManager);
        } catch (Exception e) {
            log.info("ServerHandshakePacketListenerImpl construction failed: " + e);
            return false;
        }

        // 7. Set the packet listener
        if (!setPacketListener(networkManager, handshakeListener, connectionClass)) {
            log.info("failed to set handshake listener on network manager");
            return false;
        }

        // 8. Setup the pipeline via Connection.addHandlers (vanilla-compatible)
        return setupPipelineWithAddHandlers(tunnelChannel, trueIp, connectionTimeoutSeconds,
                legacyHandler, networkManager, monitor,
                serverConnection, serverConnectionClass, connectionsField, addressField);
    }

    private Object createBandwidthDebugMonitor() {
        String[] loggerNames = {"net.minecraft.network.handler.PacketSizeLogger", "net.minecraft.network.BandwidthDebugMonitor"};
        String[] sampleLogNames = {"net.minecraft.util.profiler.MultiValueDebugSampleLogImpl", "net.minecraft.util.debugchart.LocalSampleLogger"};
        for (String loggerName : loggerNames) {
            for (String sampleLogName : sampleLogNames) {
                try {
                    Class<?> sampleLogClass = Class.forName(sampleLogName);
                    Object sampleLog = sampleLogClass.getConstructor(int.class).newInstance(1);
                    Class<?> loggerClass = Class.forName(loggerName);
                    return loggerClass.getConstructor(sampleLogClass).newInstance(sampleLog);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object resolveEnumValue(Class<?> enumClass, String name) {
        if (enumClass == null || !enumClass.isEnum()) return null;
        try {
            return Enum.valueOf((Class<Enum>) enumClass, name);
        } catch (Exception e) {
            return null;
        }
    }

    private Object createConnection(Object serverBound) {
        try {
            return new Connection((PacketFlow) serverBound);
        } catch (Exception e) {
            log.info("Connection construction failed: " + e);
        }
        return null;
    }
}
