package gg.playit.minecraft;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import org.bukkit.Server;

import java.lang.reflect.*;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class ReflectionHelper {
    static Logger log = Logger.getLogger(ReflectionHelper.class.getName());

    public final Class<?> DynmapPlugin;

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
        DynmapPlugin = cls("org.dynmap.bukkit.DynmapPlugin");

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

    public boolean networkManagerSetListener(Object networkManager, Object listener) {
        if (NetworkManager == null || PacketListener == null) {
            return false;
        }

        try {
            Method method = searchMethod(NetworkManager, "setListener", PacketListener);
            method.setAccessible(true);
            method.invoke(networkManager, listener);
            return true;
        } catch (Exception e) {
            log.warning("failed to call setListener: " + e);
        }

        try {
            var field = searchForFieldByName(NetworkManager, "packetListener");
            field.setAccessible(true);
            field.set(networkManager, listener);
            return true;
        } catch (Exception e) {
            log.warning("failed to set packetListener" + e);
        }

        var options = searchForFieldByType(NetworkManager, PacketListener);
        if (options.size() == 1) {
            try {
                options.get(0).setAccessible(true);
                options.get(0).set(networkManager, listener);
                return true;
            } catch (Exception e) {
                log.warning("failed to set packetListener directly to type" + options.get(0) + ", error: " + e);
            }
        } else {
            log.warning("got multiple options for packet listener field: " + options);
        }

        return false;
    }

    public boolean addToServerConnections(Object serverConnection, Object networkManager) {
        if (ServerConnection == null || NetworkManager == null) {
            return false;
        }

        try {
            Field field = searchForFieldByName(ServerConnection, "connections");
            field.setAccessible(true);

            var list = (List<Object>) field.get(serverConnection);
            list.add(networkManager);

            return true;
        } catch (Exception e) {
            log.warning("failed set field connections, error: " + e);
        }

        HashSet<Object> potentialFieldObjects = new HashSet<>();

        var search = ServerConnection;
        while (search != null) {
            for (var field : ServerConnection.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
                        var type = parameterizedType.getActualTypeArguments()[0];
                        var typeClass = cls(type.getTypeName());

                        if (typeClass != null && NetworkManager.isAssignableFrom(typeClass)) {
                            try {
                                field.setAccessible(true);
                                potentialFieldObjects.add(field.get(serverConnection));
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
            search = search.getSuperclass();
        }

        if (potentialFieldObjects.size() == 1) {
            var found = potentialFieldObjects.toArray()[0];
            try {
                var list = (List) found;
                list.add(networkManager);
                return true;
            } catch (Exception e) {
                log.warning("failed to add connection to " + found + ", error: " + e);
            }
        } else {
            log.warning("multiple connection lists: " + potentialFieldObjects);
        }

        return false;
    }

    public Object newHandshakeListener(Object minecraftServer, Object networkManager) {
        if (HandshakeListener == null) {
            return null;
        }

        try {
            return HandshakeListener.getConstructor(MinecraftServer, NetworkManager).newInstance(minecraftServer, networkManager);
        } catch (Exception e) {
            return null;
        }
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

    public Integer getRateLimitFromMCServer(Object server) {
        if (MinecraftServer == null) {
            return null;
        }

        try {
            return (Integer) searchMethod(MinecraftServer, "getRateLimitPacketsPerSecond").invoke(server);
        } catch (Exception e) {
            return null;
        }
    }

    public Object newLegacyPingHandler(Object serverConnection) {
        if (LegacyPingHandler == null || ServerConnection == null) {
            return null;
        }

        try {
            return LegacyPingHandler.getConstructor(ServerConnection).newInstance(serverConnection);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            return null;
        }
    }

    public Object newPacketSplitter() {
        if (PacketSplitter == null) {
            return null;
        }

        try {
            return PacketSplitter.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            return null;
        }
    }

    public Object newServerBoundPacketDecoder() {
        if (PacketDecoder == null) {
            return null;
        }

        try {
            return PacketDecoder.getConstructor(EnumProtocolDirection).newInstance(serverBound());
        } catch (Exception e) {
            return null;
        }
    }

    public Object newClientBoundPacketEncoder() {
        if (PacketEncoder == null) {
            return null;
        }

        try {
            return PacketEncoder.getConstructor(EnumProtocolDirection).newInstance(clientBound());
        } catch (Exception e) {
            return null;
        }
    }

    public Object newPacketPrepender() {
        if (PacketPrepender == null) {
            return null;
        }

        try {
            return PacketPrepender.getConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public Object newNetworkManagerServer(int rateLimit) {
        if (NetworkManagerServer == null) {
            return null;
        }

        try {
            return NetworkManagerServer.getConstructor(Integer.class).newInstance(rateLimit);
        } catch (Exception e) {
            return null;
        }
    }

    public Object newServerNetworkManager() {
        if (NetworkManager == null) {
            return null;
        }

        try {
            return NetworkManager.getConstructor(EnumProtocolDirection).newInstance(serverBound());
        } catch (Exception e) {
            return null;
        }
    }

    private Object serverBound() {
        if (EnumProtocolDirection == null) {
            return null;
        }

        try {
            return Enum.valueOf((Class<Enum>) EnumProtocolDirection, "SERVERBOUND");
        } catch (Exception e) {
            return null;
        }
    }

    private Object clientBound() {
        if (EnumProtocolDirection == null) {
            return null;
        }

        try {
            return Enum.valueOf((Class<Enum>) EnumProtocolDirection, "CLIENTBOUND");
        } catch (Exception e) {
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
                "ServerConnection=" + ServerConnection +
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
