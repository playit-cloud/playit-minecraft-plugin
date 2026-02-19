package gg.playit.channel;

import io.netty.channel.Channel;
import org.bukkit.Server;

import java.net.InetSocketAddress;

/**
 * Interface for inserting a Netty channel directly into a running Minecraft server's
 * handler pipeline, bypassing the public network interface.
 */
public interface ChannelInserter {
    /**
     * Attempts to add the tunnel channel to the Minecraft server.
     *
     * @param server                   the Bukkit server
     * @param tunnelChannel            the channel to insert (from the tunnel connection)
     * @param trueIp                   the client's real IP address
     * @param connectionTimeoutSeconds timeout for the connection
     * @param rateLimit                rate limit packets per second (0 if none)
     * @return true if the channel was successfully inserted
     */
    boolean insertChannel(Server server, Channel tunnelChannel, InetSocketAddress trueIp,
                          int connectionTimeoutSeconds, int rateLimit);
}
