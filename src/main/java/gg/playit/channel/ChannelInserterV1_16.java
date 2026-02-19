package gg.playit.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.bukkit.Server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * ChannelInserter for Minecraft 1.16.x.
 * <p>
 * Uses reflection to access NMS classes (Spigot 1.16 has no remapped-mojang artifact).
 * Uses the old NMS naming convention:
 * <ul>
 *   <li>NetworkManager instead of Connection</li>
 *   <li>ServerConnection instead of ServerConnectionListener</li>
 *   <li>LegacyPingHandler instead of LegacyQueryHandler</li>
 *   <li>PacketSplitter instead of Varint21FrameDecoder</li>
 *   <li>EnumProtocolDirection instead of PacketFlow</li>
 * </ul>
 * LegacyPingHandler takes ServerConnection as constructor arg.
 */
public final class ChannelInserterV1_16 extends BaseChannelInserter {

    private static final String[] SERVER_CONNECTION_NAMES = {
            "net.minecraft.server.network.ServerConnection",
            "net.minecraft.server.network.ServerConnectionListener"
    };
    private static final String[] CONNECTION_NAMES = {
            "net.minecraft.network.NetworkManager",
            "net.minecraft.network.Connection"
    };

    public ChannelInserterV1_16(String version) {
        super(version);
    }

    @Override
    public boolean insertChannel(Server server, Channel tunnelChannel, InetSocketAddress trueIp,
                                 int connectionTimeoutSeconds, int rateLimit) {
        ClassLoader loader = nmsClassLoader();

        Class<?> serverConnectionClass = loadClass(loader, SERVER_CONNECTION_NAMES);
        Class<?> connectionClass = loadClass(loader, CONNECTION_NAMES);
        if (serverConnectionClass == null || connectionClass == null) {
            log.warning("could not load 1.16 NMS classes");
            return false;
        }

        Field connectionsField = findField(serverConnectionClass, "connections");
        Field addressField = findField(connectionClass, "address");
        if (connectionsField == null || addressField == null) {
            log.warning("could not find connections or address field");
            return false;
        }

        Object minecraftServer = getMinecraftServer(server);
        if (minecraftServer == null) {
            log.info("failed to get Minecraft server from Bukkit.getServer()");
            return false;
        }

        Object serverConnection = getServerConnection(minecraftServer, serverConnectionClass);
        if (serverConnection == null) {
            log.info("failed to get ServerConnection from Minecraft Server");
            return false;
        }

        ChannelHandler legacyHandler = createLegacyHandler(loader, serverConnection);
        Object packetSplitter = createInstance(loader, "net.minecraft.network.PacketSplitter", "net.minecraft.network.Varint21FrameDecoder");
        if (packetSplitter == null) {
            log.info("packetSplitter construction failed");
            return false;
        }

        Class<?> packetFlowClass = loadClass(loader, "net.minecraft.network.protocol.EnumProtocolDirection", "net.minecraft.network.protocol.PacketFlow");
        Object serverBound = resolveEnumValue(packetFlowClass, "SERVERBOUND");
        if (serverBound == null) {
            log.info("failed to resolve SERVERBOUND enum");
            return false;
        }

        Object packetDecoder = createInstance(loader, packetFlowClass, serverBound, "PacketDecoder");
        if (packetDecoder == null) {
            log.info("packetDecoder construction failed");
            return false;
        }

        Object packetPrepender = createInstance(loader, "net.minecraft.network.PacketPrepender", "net.minecraft.network.Varint21LengthFieldPrepender");
        if (packetPrepender == null) {
            log.info("packetPrepender construction failed");
            return false;
        }

        Object clientBound = resolveEnumValue(packetFlowClass, "CLIENTBOUND");
        Object packetEncoder = createInstance(loader, packetFlowClass, clientBound != null ? clientBound : serverBound, "PacketEncoder");
        if (packetEncoder == null) {
            log.info("packetEncoder construction failed");
            return false;
        }

        Object networkManager = createNetworkManager(loader, connectionClass, packetFlowClass, serverBound, rateLimit);
        if (networkManager == null) {
            log.info("networkManager construction failed");
            return false;
        }

        Object handshakeListener = createHandshakeListener(loader, minecraftServer, networkManager);
        if (handshakeListener == null) {
            log.info("handshakeListener construction failed");
            return false;
        }

        if (!setPacketListener(networkManager, handshakeListener, connectionClass)) {
            log.info("failed to set handshake listener on network manager");
            return false;
        }

        return setupPipeline(tunnelChannel, trueIp, connectionTimeoutSeconds,
                legacyHandler, packetSplitter, packetDecoder, packetPrepender, packetEncoder,
                networkManager, serverConnection, serverConnectionClass, connectionsField, addressField);
    }

    private ClassLoader nmsClassLoader() {
        return getClass().getClassLoader();
    }

    private Class<?> loadClass(ClassLoader loader, String... names) {
        for (String name : names) {
            try {
                return Class.forName(name, true, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private ChannelHandler createLegacyHandler(ClassLoader loader, Object serverConnection) {
        String[] names = {"net.minecraft.server.network.LegacyPingHandler", "net.minecraft.server.network.LegacyQueryHandler"};
        for (String name : names) {
            try {
                Class<?> c = Class.forName(name, true, loader);
                for (Constructor<?> ctor : c.getConstructors()) {
                    if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isInstance(serverConnection)) {
                        return (ChannelHandler) ctor.newInstance(serverConnection);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        log.warning("LegacyPingHandler constructor not found, using pass-through");
        return createPassthroughHandler();
    }

    private Object createInstance(ClassLoader loader, String... classNames) {
        for (String name : classNames) {
            try {
                Class<?> c = Class.forName(name, true, loader);
                return c.getConstructor().newInstance();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Object createInstance(ClassLoader loader, Class<?> paramClass, Object param, String baseName) {
        String[] packages = {"net.minecraft.network.", "net.minecraft.server.network."};
        for (String pkg : packages) {
            try {
                Class<?> c = Class.forName(pkg + baseName, true, loader);
                Constructor<?> ctor = c.getConstructor(paramClass);
                return ctor.newInstance(param);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Object createNetworkManager(ClassLoader loader, Class<?> connectionClass, Class<?> packetFlowClass,
                                        Object serverBound, int rateLimit) {
        if (rateLimit > 0) {
            String[] rateNames = {"net.minecraft.server.network.NetworkManagerServer", "net.minecraft.network.RateKickingConnection"};
            for (String name : rateNames) {
                try {
                    Class<?> c = Class.forName(name, true, loader);
                    return c.getConstructor(int.class).newInstance(rateLimit);
                } catch (Exception ignored) {
                }
            }
        }
        try {
            Constructor<?> ctor = connectionClass.getConstructor(packetFlowClass);
            return ctor.newInstance(serverBound);
        } catch (Exception e) {
            log.info("Connection construction failed: " + e);
        }
        return null;
    }

    private Object createHandshakeListener(ClassLoader loader, Object minecraftServer, Object networkManager) {
        String[] names = {"net.minecraft.server.network.HandshakeListener", "net.minecraft.server.network.ServerHandshakePacketListenerImpl"};
        for (String name : names) {
            try {
                Class<?> c = Class.forName(name, true, loader);
                for (Constructor<?> cons : c.getConstructors()) {
                    if (cons.getParameterCount() == 2
                            && cons.getParameterTypes()[0].isInstance(minecraftServer)
                            && cons.getParameterTypes()[1].isInstance(networkManager)) {
                        return cons.newInstance(minecraftServer, networkManager);
                    }
                }
            } catch (Exception ignored) {
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
}
