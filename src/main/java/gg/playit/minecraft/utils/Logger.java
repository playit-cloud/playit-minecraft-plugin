package gg.playit.minecraft.utils;

import gg.playit.minecraft.PlayitBukkit;
import org.slf4j.LoggerFactory;

public class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("playit-minecraft-plugin");
    private final ServerConfiguration serverConfig;
    public final String className;

    public Logger(String className) {
        this.className = className;
        this.serverConfig = new ServerConfiguration();

        serverConfig.init();
    }

    public void info(String format, Object... data) {
        LOGGER.info("[" + className + "] " + format, data);
    }

    public void debug(String format, Object... data) {
        if (PlayitBukkit.hasDebugLogging() || serverConfig.debugModeEnabled()) {
            LOGGER.info("[" + className + "] " + format, data);
        }
    }

    public void error(String format, Object... data) {
        if (PlayitBukkit.hasDebugLogging() || serverConfig.debugModeEnabled()) {
            LOGGER.error("[" + className + "] " + format, data);
        }
    }

    public void warning(String format, Object... data) {
        if (PlayitBukkit.hasDebugLogging() || serverConfig.debugModeEnabled()) {
            LOGGER.warn("[" + className + "] " + format, data);
        }
    }
}
