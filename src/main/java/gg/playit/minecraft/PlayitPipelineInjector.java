package gg.playit.minecraft;

import io.netty.channel.*;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Uses reflection to inject {@link PlayitHeaderDecoder} into the Minecraft
 * server's Netty pipeline so that every new inbound connection gets the
 * decoder as its first handler.
 */
public class PlayitPipelineInjector {
    private static final Logger log = Logger.getLogger(PlayitPipelineInjector.class.getName());
    private static final String HANDLER_NAME = "playit-injector";

    private final List<Channel> injectedChannels = new ArrayList<>();

    private PlayitPipelineInjector() {
    }

    public static PlayitPipelineInjector inject(Server server) throws ReflectiveOperationException {
        Object mcServer = getMinecraftServer(server);
        if (mcServer == null) {
            throw new ReflectiveOperationException("could not obtain MinecraftServer instance");
        }

        Object serverConnection = getServerConnection(mcServer);
        if (serverConnection == null) {
            throw new ReflectiveOperationException("could not obtain ServerConnection instance");
        }

        List<ChannelFuture> channelFutures = getChannelFutures(serverConnection);
        if (channelFutures == null || channelFutures.isEmpty()) {
            throw new ReflectiveOperationException("no listening channels found on ServerConnection");
        }

        var injector = new PlayitPipelineInjector();

        for (ChannelFuture cf : channelFutures) {
            Channel ch = cf.channel();
            ch.pipeline().addFirst(HANDLER_NAME, new AcceptHandler());
            injector.injectedChannels.add(ch);
            log.info("injected playit header decoder into server channel: " + ch);
        }

        return injector;
    }

    public void remove() {
        for (Channel ch : injectedChannels) {
            try {
                if (ch.pipeline().get(HANDLER_NAME) != null) {
                    ch.pipeline().remove(HANDLER_NAME);
                }
            } catch (Exception e) {
                log.warning("failed to remove injector from channel " + ch + ": " + e);
            }
        }
        injectedChannels.clear();
    }

    @ChannelHandler.Sharable
    private static class AcceptHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Channel child) {
                child.pipeline().addFirst("playit-header", new PlayitHeaderDecoder());
            }
            ctx.fireChannelRead(msg);
        }
    }

    // --- Reflection helpers (derived from the deleted ReflectionHelper) ---

    private static Object getMinecraftServer(Server server) {
        Class<?> craftServerClass = findClass(
                "org.bukkit.craftbukkit.CraftServer",
                "org.bukkit.craftbukkit.v1_16_R3.CraftServer",
                "org.bukkit.craftbukkit.v1_17_R1.CraftServer",
                "org.bukkit.craftbukkit.v1_18_R1.CraftServer",
                "org.bukkit.craftbukkit.v1_18_R2.CraftServer",
                "org.bukkit.craftbukkit.v1_19_R1.CraftServer",
                "org.bukkit.craftbukkit.v1_19_R2.CraftServer",
                "org.bukkit.craftbukkit.v1_19_R3.CraftServer",
                "org.bukkit.craftbukkit.v1_20_R1.CraftServer",
                "org.bukkit.craftbukkit.v1_20_R2.CraftServer",
                "org.bukkit.craftbukkit.v1_20_R3.CraftServer",
                "org.bukkit.craftbukkit.v1_20_R4.CraftServer",
                "org.bukkit.craftbukkit.v1_21_R1.CraftServer",
                "org.bukkit.craftbukkit.v1_21_R2.CraftServer",
                "org.bukkit.craftbukkit.v1_21_R3.CraftServer"
        );
        if (craftServerClass == null) {
            log.warning("CraftServer class not found");
            return null;
        }

        try {
            Method getServer = findMethod(craftServerClass, "getServer");
            if (getServer != null) {
                getServer.setAccessible(true);
                return getServer.invoke(server);
            }
        } catch (Exception e) {
            log.warning("getServer() failed: " + e);
        }

        try {
            Field console = findDeclaredField(craftServerClass, "console");
            if (console != null) {
                console.setAccessible(true);
                return console.get(server);
            }
        } catch (Exception e) {
            log.warning("console field access failed: " + e);
        }

        return null;
    }

    private static Object getServerConnection(Object mcServer) {
        Class<?> serverConnectionClass = findClass(
                "net.minecraft.server.network.ServerConnection",
                "net.minecraft.server.network.ServerConnectionListener"
        );

        if (serverConnectionClass == null) {
            log.warning("ServerConnection class not found");
            return null;
        }

        Class<?> mcClass = mcServer.getClass();

        try {
            Method m = findMethod(mcClass, "getConnection");
            if (m != null) {
                m.setAccessible(true);
                Object result = m.invoke(mcServer);
                if (serverConnectionClass.isInstance(result)) {
                    return result;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Field f = findDeclaredField(mcClass, "connection");
            if (f != null) {
                f.setAccessible(true);
                Object result = f.get(mcServer);
                if (serverConnectionClass.isInstance(result)) {
                    return result;
                }
            }
        } catch (Exception ignored) {
        }

        for (Class<?> c = mcClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (serverConnectionClass.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object result = f.get(mcServer);
                        if (serverConnectionClass.isInstance(result)) {
                            return result;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<ChannelFuture> getChannelFutures(Object serverConnection) {
        Class<?> clazz = serverConnection.getClass();

        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                if (f.getGenericType() instanceof ParameterizedType pt) {
                    var typeArgs = pt.getActualTypeArguments();
                    if (typeArgs.length == 1 && typeArgs[0] == ChannelFuture.class) {
                        try {
                            f.setAccessible(true);
                            return (List<ChannelFuture>) f.get(serverConnection);
                        } catch (Exception e) {
                            log.warning("failed to access ChannelFuture list: " + e);
                        }
                    }
                }
            }
        }

        return null;
    }

    private static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
        }
        return null;
    }

    private static Field findDeclaredField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
