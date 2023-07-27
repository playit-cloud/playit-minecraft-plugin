package gg.playit.minecraft;

import io.netty.channel.*;
import io.netty.util.AttributeKey;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.*;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ReflectionHelper {
    static Logger log = Logger.getLogger(ReflectionHelper.class.getName());

    private final Class<ChannelHandler> ServerBootstrapAcceptor;
    private final Field SBA_childHandler;
    private final Field SBA_childOptions;
    private final Field SBA_childAttrs;

    private final Class<?> ServerConnection;
    private final Class<?> LegacyPingHandler;
    private final Class<?> MinecraftServer;

    private final Class<?> PacketSplitter;
    private final Class<?> PacketDecoder;
    private final Class<?> EnumProtocolDirection;
    private final Class<?> PacketPrepender;
    private final Class<?> PacketEncoder;
    private final Class<?> NetworkManagerServer;
    private final Class<?> NetworkManager;
    private final Class<?> HandshakeListener;
    private final Class<?> PacketListener;

    private final Class<?> CraftServer;

    public ReflectionHelper() {
        ServerBootstrapAcceptor = (Class<ChannelHandler>) cls(
                "io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor"
        );
        try {
            SBA_childHandler = searchForFieldByName(ServerBootstrapAcceptor, "childHandler");
            SBA_childHandler.setAccessible(true);
            SBA_childOptions = searchForFieldByName(ServerBootstrapAcceptor, "childOptions");
            SBA_childOptions.setAccessible(true);
            SBA_childAttrs = searchForFieldByName(ServerBootstrapAcceptor, "childAttrs");
            SBA_childAttrs.setAccessible(true);
        } catch (Throwable throwable) {
            throw new IllegalStateException("failed to get fields", throwable);
        }
        ServerConnection = cls("net.minecraft.server.network.ServerConnection");
        LegacyPingHandler = cls("net.minecraft.server.network.LegacyPingHandler");
        MinecraftServer = cls("net.minecraft.server.MinecraftServer");
        PacketSplitter = cls("net.minecraft.network.PacketSplitter");
        PacketDecoder = cls("net.minecraft.network.PacketDecoder");
        EnumProtocolDirection = cls("net.minecraft.network.protocol.EnumProtocolDirection");
        PacketPrepender = cls("net.minecraft.network.PacketPrepender");
        PacketEncoder = cls("net.minecraft.network.PacketEncoder");
        NetworkManagerServer = cls("net.minecraft.network.NetworkManagerServer");
        NetworkManager = cls("net.minecraft.network.NetworkManager");
        HandshakeListener = cls("net.minecraft.server.network.HandshakeListener");
        PacketListener = cls("net.minecraft.network.PacketListener");
        CraftServer = cls(
                "org.bukkit.craftbukkit.CraftServer",
                "org.bukkit.craftbukkit.v1_19_R1.CraftServer"
        );
    }

    static Class<?> cls(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    static Class<?> cls(String... classNames) {
        for (var name : classNames) {
            var res = cls(name);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public boolean setRemoteAddress(Channel channel, SocketAddress address) {
        try {
            Field field = AbstractChannel.class.getDeclaredField("remoteAddress");
            field.setAccessible(true);
            field.set(channel, address);
            return true;
        } catch (Exception error) {
            log.warning("failed to set remoteAddress, error: " + error);
            return false;
        }
    }

    public ServerChannel findServerChannel(Object serverConnection) {
        for (Field field : searchForFieldByType(serverConnection.getClass(), List.class)) {
            try {
                field.setAccessible(true);
                List<?> list = (List<?>) field.get(serverConnection);
                if (list != null && list.size() > 0 && list.get(0) instanceof ChannelFuture future &&
                        future.isSuccess() && future.channel() instanceof ServerChannel) {
                    return (ServerChannel) future.channel();
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("should not reach here", e);
            }
        }

        return null;
    }


    public ChannelHandler findServerHandler(ServerChannel serverChannel) {
        return serverChannel.pipeline().get(ServerBootstrapAcceptor);
    }

    public ChannelHandler findChildHandler(ChannelHandler serverAcceptor) {
        try {
            return (ChannelHandler) SBA_childHandler.get(serverAcceptor);
        } catch (IllegalAccessException error) {
            log.warning("failed to get childHandler, error: " + error);
            return null;
        }
    }

    public Map.Entry<ChannelOption<Object>, Object>[] findChildOptions(ChannelHandler serverAcceptor) {
        try {
            return (Map.Entry<ChannelOption<Object>, Object>[]) SBA_childOptions.get(serverAcceptor);
        } catch (IllegalAccessException error) {
            log.warning("failed to get childOptions, error: " + error);
            return null;
        }
    }

    public Map.Entry<AttributeKey<Object>, Object>[] findChildAttrs(ChannelHandler serverAcceptor) {
        try {
            return (Map.Entry<AttributeKey<Object>, Object>[]) SBA_childAttrs.get(serverAcceptor);
        } catch (IllegalAccessException error) {
            log.warning("failed to get childAttrs, error: " + error);
            return null;
        }
    }

    public Object getMinecraftServer(Server server) {
        if (MinecraftServer == null) {
            return null;
        }
        if (MinecraftServer.isInstance(server)) {
            return server;
        }

        if (CraftServer != null) {
            try {
                Method method = searchMethod(CraftServer, "getServer");
                method.setAccessible(true);

                Object mcServer = method.invoke(server);
                if (MinecraftServer.isInstance(mcServer)) {
                    return mcServer;
                }
            } catch (Exception ignore) {
            }

            try {
                var field = searchForFieldByName(CraftServer, "console");
                field.setAccessible(true);
                Object mcServer = field.get(server);
                if (MinecraftServer.isInstance(mcServer)) {
                    return mcServer;
                }
            } catch (Exception ignore) {
            }
        }


        return null;
    }

    public Object serverConnectionFromMCServer(Object object) {
        if (object == null || MinecraftServer == null) {
            return null;
        }

        try {
            Method getConnection = searchMethod(MinecraftServer, "getConnection");
            getConnection.setAccessible(true);
            var res = getConnection.invoke(object);
            if (ServerConnection.isInstance(res)) {
                return res;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignore) {
        }

        try {
            var field = MinecraftServer.getDeclaredField("connection");
            field.setAccessible(true);
            var res = field.get(object);
            if (ServerConnection.isInstance(res)) {
                return res;
            }
        } catch (Exception e) {
        }

        return searchForAttribute(MinecraftServer, ServerConnection, object);
    }

    public Method searchMethod(Class<?> subject, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return subject.getMethod(name, parameterTypes);
        } catch (Exception ignore) {
        }

        while (subject != null) {
            try {
                return subject.getDeclaredMethod(name, parameterTypes);
            } catch (Exception ignore) {
            }

            for (var method : subject.getDeclaredMethods()) {
                Class<?>[] searchParamTypes = method.getParameterTypes();
                if (!method.getName().equals(name) || searchParamTypes.length != parameterTypes.length) {
                    continue;
                }

                boolean match = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!searchParamTypes[i].isAssignableFrom(parameterTypes[i])) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return method;
                }
            }

            subject = subject.getSuperclass();
        }

        throw new NoSuchMethodException(name);
    }

    public Object searchForAttribute(Class<?> parent, Class<?> child, Object subject) {
        for (var field : parent.getFields()) {
            if (child.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    var res = field.get(subject);
                    if (child.isInstance(res)) {
                        return res;
                    }
                } catch (Exception ignore) {
                }
            }
        }

        for (var field : parent.getDeclaredFields()) {
            if (child.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    var res = field.get(subject);
                    if (child.isInstance(res)) {
                        return res;
                    }
                } catch (Exception ignore) {
                }
            }
        }

        return null;
    }

    public Field searchForFieldByName(Class<?> subject, String name) throws NoSuchFieldException {
        while (subject != null) {
            try {
                return subject.getDeclaredField(name);
            } catch (Exception ignore) {
            }
            subject = subject.getSuperclass();
        }

        throw new NoSuchFieldException(name);
    }

    public List<Field> searchForFieldByType(Class<?> subject, Class<?> type) {
        var fields = new ArrayList<Field>();

        while (subject != null) {
            try {
                for (var f : subject.getDeclaredFields()) {
                    if (type.isAssignableFrom(f.getType())) {
                        fields.add(f);
                    }
                }
            } catch (Exception ignore) {
            }

            subject = subject.getSuperclass();
        }

        return fields;
    }

    @Override
    public String toString() {
        return "ReflectionHelper{" +
                "ServerBootstrapAcceptor=" + ServerBootstrapAcceptor +
                ", SBA_childHandler=" + SBA_childHandler +
                ", SBA_childOptions=" + SBA_childOptions +
                ", SBA_childAttrs=" + SBA_childAttrs +
                ", ServerConnection=" + ServerConnection +
                ", LegacyPingHandler=" + LegacyPingHandler +
                ", MinecraftServer=" + MinecraftServer +
                ", PacketSplitter=" + PacketSplitter +
                ", PacketDecoder=" + PacketDecoder +
                ", EnumProtocolDirection=" + EnumProtocolDirection +
                ", PacketPrepender=" + PacketPrepender +
                ", PacketEncoder=" + PacketEncoder +
                ", NetworkManagerServer=" + NetworkManagerServer +
                ", NetworkManager=" + NetworkManager +
                ", HandshakeListener=" + HandshakeListener +
                ", PacketListener=" + PacketListener +
                ", CraftServer=" + CraftServer +
                '}';
    }
}
