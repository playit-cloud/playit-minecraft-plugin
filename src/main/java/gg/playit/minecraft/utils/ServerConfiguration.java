package gg.playit.minecraft.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ServerConfiguration {
    private final FileConfiguration spigotConfig;

    public ServerConfiguration() {
        spigotConfig = new YamlConfiguration();
    }

    public void init() {
        try {
            spigotConfig.load(new File("spigot.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public boolean debugModeEnabled() {
        if (spigotConfig != null) {
            return spigotConfig.getBoolean("settings.debug");
        } else {
            return false;
        }
    }
}
