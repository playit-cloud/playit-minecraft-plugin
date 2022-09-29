package gg.playit.minecraft;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class PlayitBukkit extends JavaPlugin {
    private PlayitManager control;
    PlayitConnectionTracker tracker = new PlayitConnectionTracker();
    RealIpInjector realIpInjector;

    @Override
    public void onEnable() {
        var secret = "<>";

//        if (secret == null) {
//            throw new RuntimeException("playit-secret not set");
//        }

        if (control == null) {
            control = new PlayitManager(secret, tracker);
        }

        realIpInjector = new RealIpInjector(player -> {
            var currentAddress = player.player.getAddress();
            if (currentAddress == null) {
                return;
            }

            if (!currentAddress.getAddress().isLoopbackAddress()) {
                return;
            }

            var trueIp = tracker.getTrueIp(currentAddress.getPort());
            System.out.println("got new client, true ip: " + trueIp);
//            trueIp.ifPresent(player::setIp);
        }, this);

        new Thread(control).start();
    }

    @Override
    public void onDisable() {
        if (control != null) {
            control.shutdown();
        }

        if (realIpInjector != null) {
            try {
                realIpInjector.close();
            } catch (IOException ignore) {
            }
        }
    }
}
