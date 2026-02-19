package gg.playit.channel;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract base class for version-specific ChannelInserter implementations.
 * Contains shared logic for pipeline setup, address manipulation, and
 * server instance retrieval that is common across all Minecraft versions.
 */
public abstract class BaseChannelInserter implements ChannelInserter {
    protected static final Logger log = Logger.getLogger(BaseChannelInserter.class.getName());

    protected final String version;

    /**
     * @param version the Minecraft version string (e.g. "1.21.1")
     */
    protected BaseChannelInserter(String version) {
        this.version = version;
    }

    /**
     * Get the MinecraftServer instance from the Bukkit server.
     * Works across all CraftBukkit versions by resolving the CraftServer class
     * and calling getServer() or reading the console field.
     */
    protected Object getMinecraftServer(Server server) {
        Class<?> minecraftServerClass;
        try {
            minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
        } catch (ClassNotFoundException e) {
            return null;
        }

        if (minecraftServerClass.isInstance(server)) {
            return server;
        }

        Class<?> craftServerClass = resolveCraftServer(server);
        if (craftServerClass == null) return null;

        // Try getServer() method
        try {
            Method m = craftServerClass.getMethod("getServer");
            m.setAccessible(true);
            Object mcServer = m.invoke(server);
            if (minecraftServerClass.isInstance(mcServer)) return mcServer;
        } catch (Exception ignored) {
        }

        // Try console field
        try {
            Field f = findField(craftServerClass, "console");
            if (f != null) {
                f.setAccessible(true);
                Object mcServer = f.get(server);
                if (minecraftServerClass.isInstance(mcServer)) return mcServer;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private Class<?> resolveCraftServer(Server server) {
        if (server != null) {
            String pkg = server.getClass().getPackage().getName();
            if (pkg.startsWith("org.bukkit.craftbukkit")) {
                try {
                    return Class.forName(pkg + ".CraftServer");
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        try {
            return Class.forName("org.bukkit.craftbukkit.CraftServer");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Get the ServerConnectionListener from the MinecraftServer.
     */
    protected Object getServerConnection(Object minecraftServer, Class<?> serverConnectionClass) {
        log.info("resolved ServerConnectionListener to: " + serverConnectionClass.getName());
        log.info("MinecraftServer class: " + minecraftServer.getClass().getName());

        // Search all fields of the MinecraftServer for one that is a ServerConnectionListener
        Object result = searchForInstance(minecraftServer.getClass(), serverConnectionClass, minecraftServer);
        if (result == null) {
            // Diagnostic: log all fields in the hierarchy
            for (Class<?> c = minecraftServer.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType().getName().contains("Connection") || f.getType().getName().contains("connection")) {
                        log.info("  candidate field in " + c.getSimpleName() + ": "
                                + f.getType().getName() + " " + f.getName());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Add a network manager to the server connection's connection list.
     */
    protected boolean addToServerConnections(Object serverConnection, Object networkManager,
                                             Class<?> serverConnectionClass, Field connectionsField) {
        if (connectionsField != null) {
            try {
                connectionsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) connectionsField.get(serverConnection);
                list.add(networkManager);
                return true;
            } catch (Exception ignored) {
            }
        }

        // Fallback: search for a List field in the server connection class
        if (serverConnectionClass != null) {
            for (Field f : serverConnectionClass.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) f.get(serverConnection);
                        if (list != null) {
                            list.add(networkManager);
                            return true;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return false;
    }

    /**
     * Set the remote address on a Netty channel (AbstractChannel.remoteAddress field).
     */
    protected boolean setRemoteAddress(Channel channel, SocketAddress address) {
        try {
            Field field = AbstractChannel.class.getDeclaredField("remoteAddress");
            field.setAccessible(true);
            field.set(channel, address);
            return true;
        } catch (Exception e) {
            log.warning("failed to set remoteAddress: " + e);
            return false;
        }
    }

    /**
     * Set the address field on the Connection/NetworkManager object.
     */
    protected boolean setConnectionAddress(Object networkManager, SocketAddress address, Field addressField) {
        if (networkManager == null || addressField == null) return false;
        if (!SocketAddress.class.isAssignableFrom(addressField.getType())) return false;
        try {
            addressField.setAccessible(true);
            addressField.set(networkManager, address);
            return true;
        } catch (Exception e) {
            log.warning("failed to set connection address: " + e);
            return false;
        }
    }

    /**
     * Set the packet listener on a network manager via a method or field.
     */
    protected boolean setPacketListener(Object networkManager, Object listener,
                                        Class<?> connectionClass) {

        // Try setListener(PacketListener) method - search by name
        for (Method m : connectionClass.getDeclaredMethods()) {
            if (m.getParameterCount() == 1 && m.getName().equals("setListener")) {
                try {
                    m.setAccessible(true);
                    m.invoke(networkManager, listener);
                    return true;
                } catch (Exception ignored) {
                }
            }
        }

        // Try setListenerForServerboundHandshake or similar
        for (Method m : connectionClass.getDeclaredMethods()) {
            if (m.getParameterCount() == 1 && m.getName().contains("Listener")) {
                try {
                    m.setAccessible(true);
                    m.invoke(networkManager, listener);
                    return true;
                } catch (Exception ignored) {
                }
            }
        }

        // Try packetListener field
        Field f = findField(connectionClass, "packetListener");
        if (f != null) {
            try {
                f.setAccessible(true);
                f.set(networkManager, listener);
                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    /**
     * Install the full Minecraft pipeline onto a tunnel channel.
     * Returns true on success, false if adding to server connections failed (and rolls back).
     */
    protected boolean setupPipeline(Channel tunnelChannel, InetSocketAddress trueIp,
                                    int connectionTimeoutSeconds,
                                    ChannelHandler legacyHandler,
                                    Object packetSplitter, Object packetDecoder,
                                    Object packetPrepender, Object packetEncoder,
                                    Object networkManager,
                                    Object serverConnection,
                                    Class<?> serverConnectionClass,
                                    Field connectionsField,
                                    Field addressField) {
        if (!setRemoteAddress(tunnelChannel, trueIp)) {
            log.warning("failed to set remote address to " + trueIp);
        }

        ChannelHandler removed = tunnelChannel.pipeline().removeLast();
        tunnelChannel.pipeline()
                .addLast("timeout", new ReadTimeoutHandler(connectionTimeoutSeconds))
                .addLast("legacy_query", legacyHandler)
                .addLast("splitter", (ChannelHandler) packetSplitter)
                .addLast("decoder", (ChannelHandler) packetDecoder)
                .addLast("prepender", (ChannelHandler) packetPrepender)
                .addLast("encoder", (ChannelHandler) packetEncoder)
                .addLast("packet_handler", (ChannelHandler) networkManager);

        if (!addToServerConnections(serverConnection, networkManager, serverConnectionClass, connectionsField)) {
            log.info("failed to add to server connections");
            tunnelChannel.pipeline().remove("timeout");
            tunnelChannel.pipeline().remove("legacy_query");
            tunnelChannel.pipeline().remove("splitter");
            tunnelChannel.pipeline().remove("decoder");
            tunnelChannel.pipeline().remove("prepender");
            tunnelChannel.pipeline().remove("encoder");
            tunnelChannel.pipeline().remove("packet_handler");
            tunnelChannel.pipeline().addLast(removed);
            return false;
        }

        tunnelChannel.pipeline().fireChannelActive();
        if (!setConnectionAddress(networkManager, trueIp, addressField)) {
            log.warning("failed to set connection address to " + trueIp);
        }
        return true;
    }

    /**
     * Install the full Minecraft pipeline by delegating to Connection.addHandlers.
     * This ensures the pipeline matches vanilla exactly (including UNBUNDLER/BUNDLER).
     * Returns true on success, false if adding to server connections failed (and rolls back).
     *
     * @param monitor BandwidthDebugMonitor for Varint21FrameDecoder; may be null for older versions
     */
    protected boolean setupPipelineWithAddHandlers(Channel tunnelChannel, InetSocketAddress trueIp,
                                                   int connectionTimeoutSeconds,
                                                   ChannelHandler legacyHandler,
                                                   Object networkManager,
                                                   Object monitor,
                                                   Object serverConnection,
                                                   Class<?> serverConnectionClass,
                                                   Field connectionsField,
                                                   Field addressField) {
        if (!setRemoteAddress(tunnelChannel, trueIp)) {
            log.warning("failed to set remote address to " + trueIp);
        }

        ChannelHandler removed = tunnelChannel.pipeline().removeLast();
        var pipeline = tunnelChannel.pipeline();

        pipeline.addLast("timeout", new ReadTimeoutHandler(connectionTimeoutSeconds));
        pipeline.addLast("legacy_query", legacyHandler);

        if (!invokeAddHandlers(networkManager, pipeline, monitor)) {
            log.info("Connection.addHandlers failed");
            pipeline.remove("timeout");
            pipeline.remove("legacy_query");
            pipeline.addLast(removed);
            return false;
        }

        if (pipeline.get("packet_handler") == null) {
            pipeline.addLast("packet_handler", (ChannelHandler) networkManager);
        }

        if (!addToServerConnections(serverConnection, networkManager, serverConnectionClass, connectionsField)) {
            log.info("failed to add to server connections");
            rollbackPipeline(pipeline, removed);
            return false;
        }

        tunnelChannel.pipeline().fireChannelActive();
        if (!setConnectionAddress(networkManager, trueIp, addressField)) {
            log.warning("failed to set connection address to " + trueIp);
        }
        return true;
    }

    /**
     * Invoke Connection.addHandlers(pipeline, PacketFlow.SERVERBOUND, false, monitor) via reflection.
     */
    private boolean invokeAddHandlers(Object connection, io.netty.channel.ChannelPipeline pipeline, Object monitor) {
        try {
            Object serverBound = resolvePacketFlowServerbound();
            if (serverBound == null) return false;

            for (Method m : connection.getClass().getMethods()) {
                if ((m.getName().equals("addHandlers") || m.getName().equals("configureSerialization"))
                        && m.getParameterCount() == 4) {
                    Class<?>[] params = m.getParameterTypes();
                    if (io.netty.channel.ChannelPipeline.class.isAssignableFrom(params[0])
                            && params[2] == boolean.class
                            && (monitor == null || params[3].isInstance(monitor))) {
                        m.setAccessible(true);
                        m.invoke(connection, pipeline, serverBound, false, monitor);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.warning("failed to invoke addHandlers: " + e);
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object resolvePacketFlowServerbound() {
        try {
            Class<?> flowClass = Class.forName("net.minecraft.network.protocol.PacketFlow");
            return Enum.valueOf((Class<Enum>) flowClass, "SERVERBOUND");
        } catch (Exception e) {
            return null;
        }
    }

    private void rollbackPipeline(io.netty.channel.ChannelPipeline pipeline, ChannelHandler removed) {
        pipeline.remove("timeout");
        pipeline.remove("legacy_query");
        pipeline.remove("splitter");
        pipeline.remove("decoder");
        pipeline.remove("prepender");
        pipeline.remove("encoder");
        pipeline.remove("unbundler");
        pipeline.remove("bundler");
        pipeline.remove("packet_handler");
        pipeline.addLast(removed);
    }

    /**
     * Create a fallback legacy handler that passes through all reads.
     */
    protected ChannelHandler createPassthroughHandler() {
        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ctx.fireChannelRead(msg);
            }
        };
    }

    // --- Reflection utilities ---

    protected Field findField(Class<?> subject, String name) {
        if (subject == null || name == null) return null;
        for (Class<?> c = subject; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    protected Object searchForInstance(Class<?> parent, Class<?> targetType, Object subject) {
        // Walk up the class hierarchy to find fields declared in superclasses too
        // (e.g., ServerConnectionListener is declared in MinecraftServer but
        //  the runtime class is DedicatedServer)
        for (Class<?> c = parent; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (targetType.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object res = field.get(subject);
                        if (targetType.isInstance(res)) return res;
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }
}
