package gg.playit.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class PlayitBukkit extends JavaPlugin {
    static Logger log = Logger.getLogger(ReflectionHelper.class.getName());

    private PlayitManager control;
    PlayitConnectionTracker tracker = new PlayitConnectionTracker();

    @Override
    public void onEnable() {
        getConfig().addDefault("agent-secret", "");
        saveDefaultConfig();

        var secretKey = getConfig().getString("agent-secret");
        if (secretKey == null || secretKey.length() == 0) {
            log.warning("secret key not set");
            return;
        }

        if (control == null) {
            control = new PlayitManager(secretKey, tracker, Bukkit.getServer());
        }

        new Thread(control).start();
    }

    @Override
    public void onDisable() {
        if (control != null) {
            control.shutdown();
        }
    }
}
