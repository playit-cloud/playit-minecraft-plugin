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
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.bukkit.Server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;

/**
 * ChannelInserter for Minecraft 1.21.0 through 1.21.3.
 * <p>
 * Same class names as V1_17 but with protocol state changes:
 * <ul>
 *   <li>PacketDecoder/PacketEncoder may take ProtocolInfo instead of PacketFlow</li>
 *   <li>HandshakeProtocols provides initial protocol state</li>
 *   <li>Connection may have a static INITIAL_PROTOCOL field</li>
 * </ul>
 * LegacyQueryHandler takes MinecraftServer as constructor arg.
 * No BandwidthDebugMonitor support for Varint21FrameDecoder.
 */
public final class ChannelInserterV1_21 extends BaseChannelInserter {

    public ChannelInserterV1_21(String version) {
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

        // 3. Create LegacyQueryHandler - 1.21+ takes MinecraftServer (implements ServerInfo)
        ChannelHandler legacyHandler;
        try {
            legacyHandler = new LegacyQueryHandler((MinecraftServer) minecraftServer);
        } catch (Exception e) {
            log.warning("LegacyQueryHandler construction failed: " + e);
            legacyHandler = createPassthroughHandler();
        }

        // 4. Create Varint21FrameDecoder - 1.21.0-1.21.3 has no-arg; some builds require BandwidthDebugMonitor
        Object packetSplitter = createVarint21FrameDecoder();
        if (packetSplitter == null) {
            log.info("Varint21FrameDecoder construction failed");
            return false;
        }

        // 5. Resolve PacketFlow and initial protocol
        Object serverBound = resolveEnumValue(PacketFlow.class, "SERVERBOUND");
        Object clientBound = resolveEnumValue(PacketFlow.class, "CLIENTBOUND");
        Object initialProtocol = resolveInitialProtocol();

        // 6. Create PacketDecoder - try ProtocolInfo first, fallback to PacketFlow
        Object packetDecoder = createPacketDecoder(initialProtocol, serverBound);
        if (packetDecoder == null) {
            log.info("PacketDecoder creation failed");
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

        // 8. Create PacketEncoder - try ProtocolInfo first, fallback to PacketFlow
        Object packetEncoder = createPacketEncoder(initialProtocol, clientBound, serverBound);
        if (packetEncoder == null) {
            log.info("PacketEncoder creation failed");
            return false;
        }

        // 9. Create Connection - use RateKickingConnection if rate limit > 0
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

        // 10. Create ServerHandshakePacketListenerImpl(MinecraftServer, Connection)
        Object handshakeListener;
        try {
            handshakeListener = new ServerHandshakePacketListenerImpl((MinecraftServer) minecraftServer, (Connection) networkManager);
        } catch (Exception e) {
            log.info("ServerHandshakePacketListenerImpl construction failed: " + e);
            return false;
        }

        // 11. Set the packet listener
        if (!setPacketListener(networkManager, handshakeListener, connectionClass)) {
            log.info("failed to set handshake listener on network manager");
            return false;
        }

        // 12. Setup the pipeline
        return setupPipeline(tunnelChannel, trueIp, connectionTimeoutSeconds,
                legacyHandler, packetSplitter, packetDecoder, packetPrepender, packetEncoder,
                networkManager, serverConnection, serverConnectionClass, connectionsField, addressField);
    }

    private Object resolveInitialProtocol() {
        try {
            Field f = HandshakeProtocols.class.getDeclaredField("SERVERBOUND");
            f.setAccessible(true);
            Object value = f.get(null);
            if (value != null) return value;
        } catch (Exception ignored) {
        }
        try {
            Field f = HandshakeProtocols.class.getDeclaredField("C2S");
            f.setAccessible(true);
            Object value = f.get(null);
            if (value != null) return value;
        } catch (Exception ignored) {
        }
        for (String fieldName : new String[]{"C2S_HANDSHAKE_STATE", "INITIAL_PROTOCOL"}) {
            try {
                Field f = Connection.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                Object value = f.get(null);
                if (value != null) return value;
            } catch (Exception ignored) {
            }
        }
        for (Field f : Connection.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                String typeName = f.getType().getName();
                if (typeName.contains("ProtocolInfo") || typeName.contains("NetworkState")) {
                    try {
                        f.setAccessible(true);
                        Object value = f.get(null);
                        if (value != null) return value;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }

    private Object createVarint21FrameDecoder() {
        String[] loggerNames = {"net.minecraft.network.handler.PacketSizeLogger", "net.minecraft.network.BandwidthDebugMonitor"};
        String[] sampleLogNames = {"net.minecraft.util.profiler.MultiValueDebugSampleLogImpl", "net.minecraft.util.debugchart.LocalSampleLogger"};
        for (String loggerName : loggerNames) {
            for (String sampleLogName : sampleLogNames) {
                try {
                    Class<?> sampleLogClass = Class.forName(sampleLogName);
                    Object sampleLog = sampleLogClass.getConstructor(int.class).newInstance(1);
                    Class<?> loggerClass = Class.forName(loggerName);
                    Object monitor = loggerClass.getConstructor(sampleLogClass).newInstance(sampleLog);
                    for (Constructor<?> ctor : Varint21FrameDecoder.class.getConstructors()) {
                        if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isInstance(monitor)) {
                            return ctor.newInstance(monitor);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        log.warning("Varint21FrameDecoder creation failed");
        return null;
    }

    private Object createPacketDecoder(Object protocolInfo, Object serverBound) {
        if (protocolInfo != null) {
            try {
                return PacketDecoder.class.getConstructor(protocolInfo.getClass()).newInstance(protocolInfo);
            } catch (Exception ignored) {
            }
            for (Class<?> iface : protocolInfo.getClass().getInterfaces()) {
                try {
                    return PacketDecoder.class.getConstructor(iface).newInstance(protocolInfo);
                } catch (Exception ignored) {
                }
            }
        }
        if (serverBound != null) {
            try {
                return PacketDecoder.class.getConstructor(PacketFlow.class).newInstance(serverBound);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Object createPacketEncoder(Object protocolInfo, Object clientBound, Object serverBound) {
        if (protocolInfo != null) {
            try {
                return PacketEncoder.class.getConstructor(protocolInfo.getClass()).newInstance(protocolInfo);
            } catch (Exception ignored) {
            }
            for (Class<?> iface : protocolInfo.getClass().getInterfaces()) {
                try {
                    return PacketEncoder.class.getConstructor(iface).newInstance(protocolInfo);
                } catch (Exception ignored) {
                }
            }
        }
        Object flow = clientBound != null ? clientBound : serverBound;
        if (flow != null) {
            try {
                return PacketEncoder.class.getConstructor(PacketFlow.class).newInstance(flow);
            } catch (Exception ignored) {
            }
        }
        return null;
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
