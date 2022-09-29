package gg.playit.minecraft;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class PlayitConnectionTracker {
    private final Object sync = new Object();
    private final HashSet<String> activeConnections = new HashSet<>();
    private final HashMap<Integer, InetSocketAddress> realIps = new HashMap<>();

    public boolean addConnection(String key) {
        synchronized (sync) {
            return activeConnections.add(key);
        }
    }

    public void removeConnection(String key, Integer localPort) {
        synchronized (sync) {
            activeConnections.remove(key);
            if (localPort != null) {
                realIps.remove(localPort);
            }
        }
    }

    public void addTrueIp(int localPort, InetSocketAddress address) {
        synchronized (sync) {
            realIps.put(localPort, address);
        }
    }

    public Optional<InetSocketAddress> getTrueIp(int localPort) {
        synchronized (sync) {
            return Optional.ofNullable(realIps.get(localPort));
        }
    }
}
