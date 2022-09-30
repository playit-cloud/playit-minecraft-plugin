package gg.playit.minecraft;

import java.util.HashSet;

public class PlayitConnectionTracker {
    private final Object sync = new Object();
    private final HashSet<String> activeConnections = new HashSet<>();

    public boolean addConnection(String key) {
        synchronized (sync) {
            return activeConnections.add(key);
        }
    }

    public void removeConnection(String key) {
        synchronized (sync) {
            activeConnections.remove(key);
        }
    }
}
