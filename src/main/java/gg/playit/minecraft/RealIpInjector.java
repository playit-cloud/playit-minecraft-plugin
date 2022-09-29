package gg.playit.minecraft;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import gg.playit.minecraft.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RealIpInjector implements Closeable {
    private ProtocolLibHandshakeHandler libHandler = null;

    public RealIpInjector(NewPlayerHandler handler, Plugin plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            libHandler = new ProtocolLibHandshakeHandler(plugin, handler);
            protocolManager.addPacketListener(libHandler);
        }
    }

    @Override
    public void close() throws IOException {
        if (libHandler != null) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.removePacketListener(libHandler);
        }
    }

    public interface NewPlayerHandler {
        void handlePlayer(PlayerWithSetIp player);
    }

    public static abstract class PlayerWithSetIp {
        public final Player player;

        PlayerWithSetIp(Player player) {
            this.player = player;
        }

        public abstract void setIp(InetSocketAddress ip);
    }

    private static class ProtocolLibHandshakeHandler extends PacketAdapter {
        private final NewPlayerHandler handler;

        public ProtocolLibHandshakeHandler(Plugin plugin, NewPlayerHandler handler) {
            super(plugin, ListenerPriority.LOWEST, PacketType.Handshake.Client.SET_PROTOCOL);
            this.handler = handler;
        }

        private static class BukkitPlayer extends PlayerWithSetIp {
            BukkitPlayer(org.bukkit.entity.Player player) {
                super(player);
            }

            @Override
            public void setIp(InetSocketAddress ip) {
                try {
                    var ignored = TemporaryPlayerFactory.getInjectorFromPlayer(this.player);
                    Object injector = ReflectionUtil.getObjectInPrivateField(ignored, "injector");
                    Object networkManager = ReflectionUtil.getObjectInPrivateField(injector, "networkManager");

                    ReflectionUtil.setFinalField(networkManager, ReflectionUtil.searchFieldByClass(networkManager.getClass(), SocketAddress.class), ip);

                    Object channel = ReflectionUtil.getObjectInPrivateField(injector, "wrappedChannel");
                    ReflectionUtil.setFinalField(channel, ReflectionUtil.getDeclaredField(abstractChannelClass, "remoteAddress"), ip);
                } catch (ReflectionUtil.ReflectionException error) {
                }
            }
        }

        @Override
        public void onPacketReceiving(PacketEvent e) {
            this.handler.handlePlayer(new BukkitPlayer(e.getPlayer()));
        }

        /*
         * Reflection objects needed for manipulation
         */
        private static Class<?> abstractChannelClass;

        static {
            try {
                abstractChannelClass = Class.forName("io.netty.channel.AbstractChannel");
            } catch (ClassNotFoundException e) {
                try {
                    abstractChannelClass = Class.forName("net.minecraft.util.io.netty.channel.AbstractChannel");
                } catch (ClassNotFoundException e2) {
                    throw new RuntimeException(new ReflectionUtil.ReflectionException(e2));
                }
            }
        }

    }
}
