package gg.playit.minecraft;

import gg.playit.api.ApiClient;
import gg.playit.api.ApiError;
import gg.playit.api.models.Notice;
import gg.playit.minecraft.utils.Logger;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

public final class PlayitBukkit extends JavaPlugin implements Listener {
    public static final String CFG_AGENT_SECRET_KEY = "agent-secret";
    public static final String CFG_CONNECTION_TIMEOUT_SECONDS = "mc-timeout-sec";
    public static final String CFG_DEBUG_LOGGING = "debug-logging";
    private static boolean debugLogging = false;

    static Logger log = new Logger(PlayitBukkit.class.getName());
    final EventLoopGroup eventGroup = new NioEventLoopGroup();

    private final Object managerSync = new Object();
    private volatile PlayitManager playitManager;

    Server server;

    @Override
    public void onEnable() {
        server = Bukkit.getServer();

        var command = getCommand("playit");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            log.error("failed to setup command /playit");
        }

        getConfig().addDefault("agent-secret", "");
        getConfig().addDefault("debug-logging", "false");
        saveDefaultConfig();

        var secretKey = getConfig().getString("agent-secret");
        resetConnection(secretKey);

        var debugLogging = getConfig().getString(CFG_DEBUG_LOGGING);
        if ("true".equals(debugLogging)) {
            setDebugLogging(true);
        } else if ("false".equals(debugLogging)) {
            setDebugLogging(false);
        }
        getConfig().set(CFG_DEBUG_LOGGING, hasDebugLogging());

        try {
            PluginManager pm = Bukkit.getServer().getPluginManager();
            pm.registerEvents(this, this);
        } catch (Exception e) {
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var manager = playitManager;

        if (player.isOp()) {
            if (manager.isGuest()) {
                player.sendMessage(ChatColor.RED + "WARNING:" + ChatColor.RESET + " playit.gg is running with a guest account");
            } else if (!manager.emailVerified()) {
                player.sendMessage(ChatColor.RED + "WARNING:" + ChatColor.RESET + " your email on playit.gg is not verified");
            }

            Notice notice = manager.getNotice();
            if (notice != null) {
                player.sendMessage(ChatColor.RED + "NOTICE:" + ChatColor.RESET + " " + notice.message);
                player.sendMessage(ChatColor.RED + "URL:" + ChatColor.RESET + " " + notice.url);
            }
        }
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.UNDERLINE + "playit.gg:" + ChatColor.RESET + " " + message);
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
                    String message = switch (manager.state()) {
                        case PlayitKeysSetup.STATE_INIT -> "preparing secret";
                        case PlayitKeysSetup.STATE_MISSING_SECRET -> "waiting for claim";
                        case PlayitKeysSetup.STATE_CHECKING_SECRET -> "checking secret";
                        case PlayitKeysSetup.STATE_CREATING_TUNNEL -> "preparing tunnel";
                        case PlayitKeysSetup.STATE_ERROR -> "error setting up key / tunnel";

                        case PlayitManager.STATE_CONNECTING -> "connecting";
                        case PlayitManager.STATE_ONLINE -> "connected";
                        case PlayitManager.STATE_OFFLINE -> "offline";
                        case PlayitManager.STATE_ERROR_WAITING -> "got error, retrying";
                        case PlayitManager.STATE_INVALID_AUTH -> "invalid secret key";

                        case PlayitManager.STATE_SHUTDOWN -> "shutdown";
                        default -> "unknown";
                    };

                    sender.sendMessage(ChatColor.BLUE + "" + ChatColor.UNDERLINE + "playit status:" + ChatColor.RESET + " " + message);
                }

                return true;
            }

            if (args.length > 1 && args[1].equals("restart")) {
                resetConnection(null);
                broadcast("restarting connection as requested by: " + sender.getName());
                return true;
            }

            if (args.length > 1 && args[1].equals("reset")) {
                getConfig().set(CFG_AGENT_SECRET_KEY, "");
                resetConnection(null);
                return true;
            }

            if (args.length > 1 && args[1].equals("shutdown")) {
                synchronized (managerSync) {
                    if (playitManager != null) {
                        playitManager.shutdown();
                        playitManager = null;
                    }
                }
                broadcast("shutting down connection as requested by: " + sender.getName());
                return true;
            }

            if (args.length > 2 && args[1].equals("set-secret")) {
                String secretKey = args[2];
                if (secretKey.length() < 32) {
                    sender.sendMessage("invalid secret key");
                    return true;
                }
                resetConnection(secretKey);
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

        if (args.length > 0 && args[0].equals("tunnel")) {
            if (args.length > 1 && args[1].equals("get-address")) {
                var m = playitManager;
                if (m != null) {
                    var a = m.getAddress();
                    if (a != null) {
                        broadcast(a);
                        return true;
                    }
                }

                sender.sendMessage("playit.gg is still setting up");
                return true;
            }
        }

        if (args.length > 0 && args[0].equals("account")) {
            if (args.length > 1 && args[1].equals("guest-login-link")) {
                var secret = getConfig().getString(CFG_AGENT_SECRET_KEY);
                if (secret == null) {
                    sender.sendMessage("ERROR: secret not set");
                    return true;
                }

                sender.sendMessage("preparing login link");

                new Thread(() -> {
                    try {
                        var api = new ApiClient(secret);
                        var session = api.createGuestWebSessionKey();

                        var url = "https://playit.gg/login/guest-account/" + session;
                        log.info("generated login url: " + url);

                        sender.sendMessage("generated login url");
                        sender.sendMessage("URL: " + url);
                    } catch (ApiError e) {
                        log.warning("failed to create guest secret: " + e);
                        sender.sendMessage("error: " + e.getMessage());
                    } catch (IOException e) {
                        log.error("failed to create guest secret: " + e);
                    }
                }).start();

                return true;
            }
        }

        if (args.length > 0 && args[0].equals("logging")) {
            if (args.length > 1 && args[1].equals("enable")) {
                getConfig().set(CFG_DEBUG_LOGGING, true);
                saveConfig();
                setDebugLogging(true);
                sender.sendMessage("enabled debug logging");
                return true;
            }

            if (args.length > 1 && args[1].equals("disable")) {
                getConfig().set(CFG_DEBUG_LOGGING, false);
                saveConfig();
                setDebugLogging(false);
                sender.sendMessage("disabled debug logging");
                return true;
            }
        }

        return false;
    }

    private void resetConnection(String secretKey) {
        if (secretKey != null) {
            getConfig().set(CFG_AGENT_SECRET_KEY, secretKey);
            saveConfig();
        }

        synchronized (managerSync) {
            if (playitManager != null) {
                playitManager.shutdown();
            }

            playitManager = new PlayitManager(this);
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
            return List.of("agent", "tunnel", "prop", "account", "logging");
        }

        if (args[0].equals("account")) {
            if (argCount == 1) {
                return List.of("guest-login-link");
            }
        }

        if (args[0].equals("agent")) {
            if (argCount == 1) {
                return List.of("set-secret", "shutdown", "status", "restart", "reset");
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
                return List.of("get-address");
            }
        }

        if (args[0].equals("logging")) {
            if (argCount == 1) {
                return List.of("enable", "disable");
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

    public static boolean hasDebugLogging() {
        return debugLogging;
    }

    public void setDebugLogging(boolean debugLogging) {
        PlayitBukkit.debugLogging = debugLogging;
    }
}
