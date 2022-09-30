package gg.playit.minecraft;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class PlayitBukkit extends JavaPlugin {
    private static final String CFG_AGENT_SECRET_KEY = "agent-secret";
    private static final String CFG_CONNECTION_TIMEOUT_SECONDS = "mc-timeout-sec";

    static Logger log = Logger.getLogger(PlayitBukkit.class.getName());
    final EventLoopGroup eventGroup = new NioEventLoopGroup();

    private final Object managerSync = new Object();
    private volatile PlayitManager playitManager;

    @Override
    public void onEnable() {
        var command = getCommand("playit");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            log.severe("failed to setup command /playit");
        }

        getConfig().addDefault("agent-secret", "");
        saveDefaultConfig();

        var secretKey = getConfig().getString("agent-secret");
        if (secretKey == null || secretKey.length() == 0) {
            log.warning("secret key not set");
        } else {
            setSecret(secretKey);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("OP required");
            return true;
        }

        if (args.length > 0 && args[0].equals("agent")) {
            if (args.length > 1 && args[1].equals("status")) {
                var manager = playitManager;

                if (manager == null) {
                    String currentSecret = getConfig().getString(CFG_AGENT_SECRET_KEY);
                    if (currentSecret == null || currentSecret.length() == 0) {
                        sender.sendMessage(CFG_AGENT_SECRET_KEY + " is not set");
                    } else {
                        sender.sendMessage("playit status: offline (or shutting down)");
                    }
                } else {
                    switch (manager.state()) {
                        case PlayitManager.STATE_CONNECTING -> sender.sendMessage("playit status: connecting");
                        case PlayitManager.STATE_ONLINE -> sender.sendMessage("playit status: connected");
                        case PlayitManager.STATE_SHUTDOWN -> sender.sendMessage("playit status: shutting down");
                        case PlayitManager.STATE_OFFLINE -> sender.sendMessage("playit status: offline");
                        case PlayitManager.STATE_ERROR_WAITING -> sender.sendMessage("playit status: got error, retrying");
                        case PlayitManager.STATE_INVALID_AUTH -> sender.sendMessage("playit status: invalid secret key");
                        default -> sender.sendMessage("playit status: unknown");
                    }
                }

                return true;
            }

            if (args.length > 1 && args[1].equals("restart")) {
                var secret = getConfig().getString(CFG_AGENT_SECRET_KEY);
                if (secret == null || secret.length() == 0) {
                    sender.sendMessage("cannot restart playit.gg connection as secret is not set");
                    return true;
                }

                setSecret(secret);
                Bukkit.broadcastMessage("restarting playit.gg connection as requested by: " + sender.getName());
                return true;
            }

            if (args.length > 1 && args[1].equals("shutdown")) {
                synchronized (managerSync) {
                    if (playitManager != null) {
                        playitManager.shutdown();
                        playitManager = null;
                    }
                }
                Bukkit.broadcastMessage("shutting down playit.gg connection as requested by: " + sender.getName());
                return true;
            }

            if (args.length > 2 && args[1].equals("set-secret")) {
                String secretKey = args[2];
                if (secretKey.length() < 32) {
                    sender.sendMessage("invalid secret key");
                    return true;
                }
                setSecret(secretKey);
                sender.sendMessage("updated secret key, connecting to new tunnel server");
                return true;
            }

            return false;
        }

        if (args.length > 0 && args[0].equals("prop")) {
            if (args.length > 1 && args[1].equals("get")) {
                {
                    int current = 30;
                    var p = playitManager;
                    if (p != null) {
                        current = playitManager.connectionTimeoutSeconds;
                    }

                    int settings = 30;
                    try {
                        settings = getConfig().getInt(CFG_CONNECTION_TIMEOUT_SECONDS);
                    } catch (Exception ignore) {
                    }

                    sender.sendMessage("prop: " + CFG_CONNECTION_TIMEOUT_SECONDS + ", current: " + current + ", setting: " + settings);
                }

                return true;
            }

            if (args.length > 2 && args[1].equals("set")) {
                if (args[2].equals(CFG_CONNECTION_TIMEOUT_SECONDS)) {
                    try {
                        var value = Integer.parseInt(args[3]);
                        getConfig().set(CFG_CONNECTION_TIMEOUT_SECONDS, value);
                        saveConfig();

                        sender.sendMessage("configuration set, run \"/playit agent restart\" to apply");
                    } catch (Exception ignore) {
                        sender.sendMessage("invalid integer");
                    }

                    return true;
                }
            }

            return false;
        }

        return false;
    }

    private void setSecret(String secretKey) {
        getConfig().set(CFG_AGENT_SECRET_KEY, secretKey);
        saveConfig();

        synchronized (managerSync) {
            if (playitManager != null) {
                playitManager.shutdown();
            }

            playitManager = new PlayitManager(secretKey, Bukkit.getServer(), eventGroup);
            try {
                int waitSeconds = getConfig().getInt(CFG_CONNECTION_TIMEOUT_SECONDS);
                if (waitSeconds != 0) {
                    playitManager.connectionTimeoutSeconds = waitSeconds;
                }
            } catch (Exception ignore) {
            }
            new Thread(playitManager).start();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return null;
        }

        int argCount = args.length;
        if (argCount != 0 && args[argCount - 1].length() == 0) {
            argCount -= 1;
        }

        if (argCount == 0) {
            return List.of("agent", "tunnel", "prop");
        }

        if (args[0].equals("agent")) {
            if (argCount == 1) {
                return List.of("set-secret", "shutdown", "status", "restart");
            }
        }

        if (args[0].equals("prop")) {
            if (argCount == 1) {
                return List.of("set", "get");
            }

            if (argCount == 2) {
                if (!args[1].equals("set") && !args[1].equals("get")) {
                    return null;
                }

                return List.of(CFG_CONNECTION_TIMEOUT_SECONDS);
            }
        }

        if (args[0].equals("tunnel")) {
            if (argCount == 1) {
                return List.of("get-address", "status");
            }
        }

        return null;
    }

    @Override
    public void onDisable() {
        if (playitManager != null) {
            playitManager.shutdown();
            playitManager = null;
        }
    }
}
