package gg.playit.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * ChannelInserter for Minecraft 1.17 through 1.20.x.
 * <p>
 * Uses the modern NMS naming convention:
 * <ul>
 *   <li>Connection (was NetworkManager)</li>
 *   <li>ServerConnectionListener (was ServerConnection)</li>
 *   <li>LegacyQueryHandler (was LegacyPingHandler)</li>
 *   <li>Varint21FrameDecoder (was PacketSplitter)</li>
 *   <li>PacketFlow (was EnumProtocolDirection)</li>
 * </ul>
 * LegacyQueryHandler takes MinecraftServer as constructor arg.
 * PacketDecoder/PacketEncoder take PacketFlow.
 * No BandwidthDebugMonitor support.
 */
public final class ChannelInserterV1_17 extends BaseChannelInserter {

    public ChannelInserterV1_17(String version) {
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

        // 3. Create LegacyQueryHandler - takes ServerConnectionListener (ServerNetworkIo)
        ChannelHandler legacyHandler;
        try {
            legacyHandler = new LegacyQueryHandler((ServerConnectionListener) serverConnection);
        } catch (Exception e) {
            log.warning("LegacyQueryHandler construction failed: " + e);
            legacyHandler = createPassthroughHandler();
        }

        // 4. Create Varint21FrameDecoder (no-arg constructor)
        Object packetSplitter;
        try {
            packetSplitter = new Varint21FrameDecoder();
        } catch (Exception e) {
            log.info("Varint21FrameDecoder construction failed: " + e);
            return false;
        }

        // 5. Resolve SERVERBOUND PacketFlow
        Object serverBound = resolveEnumValue(PacketFlow.class, "SERVERBOUND");
        if (serverBound == null) {
            log.info("failed to resolve SERVERBOUND enum");
            return false;
        }

        // 6. Create PacketDecoder(PacketFlow.SERVERBOUND)
        Object packetDecoder;
        try {
            packetDecoder = new PacketDecoder((PacketFlow) serverBound);
        } catch (Exception e) {
            log.info("PacketDecoder construction failed: " + e);
            return false;
        }

        // 7. Create Varint21LengthFieldPrepender (no-arg)
        Object packetPrepender;
        try {
            packetPrepender = new Varint21LengthFieldPrepender();
        } catch (Exception e) {
            log.info("Varint21LengthFieldPrepender construction failed: " + e);
            return false;
        }

        // 8. Resolve CLIENTBOUND
        Object clientBound = resolveEnumValue(PacketFlow.class, "CLIENTBOUND");

        // 9. Create PacketEncoder(PacketFlow.CLIENTBOUND)
        Object packetEncoder;
        try {
            packetEncoder = new PacketEncoder((PacketFlow) (clientBound != null ? clientBound : serverBound));
        } catch (Exception e) {
            log.info("PacketEncoder construction failed: " + e);
            return false;
        }

        // 10. Create Connection - use RateKickingConnection if rate limit > 0
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

        // 11. Create ServerHandshakePacketListenerImpl(MinecraftServer, Connection)
        Object handshakeListener;
        try {
            handshakeListener = new ServerHandshakePacketListenerImpl((MinecraftServer) minecraftServer, (Connection) networkManager);
        } catch (Exception e) {
            log.info("ServerHandshakePacketListenerImpl construction failed: " + e);
            return false;
        }

        // 12. Set the packet listener
        if (!setPacketListener(networkManager, handshakeListener, connectionClass)) {
            log.info("failed to set handshake listener on network manager");
            return false;
        }

        // 13. Setup the pipeline
        return setupPipeline(tunnelChannel, trueIp, connectionTimeoutSeconds,
                legacyHandler, packetSplitter, packetDecoder, packetPrepender, packetEncoder,
                networkManager, serverConnection, serverConnectionClass, connectionsField, addressField);
    }

    private Object createConnection(Object serverBound) {
        try {
            return new Connection((PacketFlow) serverBound);
        } catch (Exception e) {
            log.info("Connection construction failed: " + e);
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
}
