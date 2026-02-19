package gg.playit.channel;

import org.bukkit.Server;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides the correct {@link ChannelInserter} implementation for the detected
 * Minecraft server version at runtime.
 * <p>
 * Version detection uses the CraftBukkit package suffix (e.g. {@code v1_21_R2})
 * to determine the major version and revision.
 */
public final class ChannelInserterProvider {
    private static final Logger log = Logger.getLogger(ChannelInserterProvider.class.getName());

    /**
     * Pattern to extract version components from CraftBukkit package suffix.
     * Matches patterns like v1_16_R3, v1_21_R1, v1_21_R2, etc.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("v(\\d+)_(\\d+)_R(\\d+)");

    private ChannelInserterProvider() {}

    /**
     * Detects the Minecraft server version and returns an appropriate ChannelInserter,
     * or null if the version is not supported.
     *
     * @param server the Bukkit server instance
     * @return a ChannelInserter for the detected version, or null if unsupported
     */
    public static ChannelInserter get(Server server) {
        if (server == null) return null;

        String pkg = server.getClass().getPackage().getName();
        if (!pkg.startsWith("org.bukkit.craftbukkit")) {
            log.info("server class package does not start with org.bukkit.craftbukkit: " + pkg);
            return null;
        }

        // Handle flat CraftBukkit packaging (e.g., Paper 1.20.5+ uses "org.bukkit.craftbukkit" with no version suffix)
        if (pkg.equals("org.bukkit.craftbukkit") || !pkg.startsWith("org.bukkit.craftbukkit.")) {
            return fromBukkitVersion(server);
        }

        // Extract the version suffix (e.g., "v1_21_R2")
        String suffix = pkg.substring("org.bukkit.craftbukkit.".length());
        if (suffix.isEmpty() || !suffix.startsWith("v")) {
            return fromBukkitVersion(server);
        }

        // If the suffix has further dots (e.g., "v1_21_R2.something"), take only the first part
        int dotIdx = suffix.indexOf('.');
        if (dotIdx > 0) {
            suffix = suffix.substring(0, dotIdx);
        }

        Matcher m = VERSION_PATTERN.matcher(suffix);
        if (!m.matches()) {
            log.info("could not parse CraftBukkit version suffix: " + suffix);
            return fromBukkitVersion(server);
        }

        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        int revision = Integer.parseInt(m.group(3));

        return createInserter(major, minor, revision);
    }

    /**
     * Attempt to determine the Minecraft version from {@code Bukkit.getVersion()}
     * or {@code Bukkit.getBukkitVersion()} for servers without a versioned CraftBukkit package
     * (e.g., Paper 1.20.5+ with flat packaging).
     */
    private static ChannelInserter fromBukkitVersion(Server server) {
        String bukkitVersion = server.getBukkitVersion(); // e.g., "1.21.1-R0.1-SNAPSHOT"
        if (bukkitVersion == null) return null;

        // Parse "1.21.1-R0.1-SNAPSHOT" -> major=1, minor=21, patch=1
        String[] parts = bukkitVersion.split("-")[0].split("\\.");
        if (parts.length < 2) return null;

        try {
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            // Map to approximate version group
            // We use revision=1 as default; for 1.21.4+ we need revision=2
            if (minor >= 21 && patch >= 4) {
                String mcVersion = "1." + minor + "." + patch;
                return createInserter("ChannelInserterV1_21_4", mcVersion);
            } else if (minor >= 21) {
                String mcVersion = "1." + minor + (patch > 0 ? "." + patch : "");
                return createInserter("ChannelInserterV1_21", mcVersion);
            } else if (minor >= 17) {
                String mcVersion = "1." + minor + (patch > 0 ? "." + patch : "");
                return createInserter("ChannelInserterV1_17", mcVersion);
            } else if (minor == 16) {
                String mcVersion = "1.16" + (patch > 0 ? "." + patch : "");
                return createInserter("ChannelInserterV1_16", mcVersion);
            }
        } catch (NumberFormatException ignored) {
        }

        log.info("unsupported Minecraft version: " + bukkitVersion);
        return null;
    }

    /**
     * Create a ChannelInserter based on parsed version components from the
     * CraftBukkit package suffix (e.g., v1_21_R2 -> major=1, minor=21, revision=2).
     */
    private static ChannelInserter createInserter(int major, int minor, int revision) {
        if (major != 1) {
            log.info("unsupported major version: " + major);
            return null;
        }

        // Map CraftBukkit package suffix to Minecraft version string
        String mcVersion = toMcVersion(minor, revision);

        if (minor >= 21 && revision >= 2) {
            // v1_21_R2 = 1.21.4+ (BandwidthDebugMonitor required)
            log.info("using ChannelInserterV1_21_4 for v" + major + "_" + minor + "_R" + revision);
            return createInserter("ChannelInserterV1_21_4", mcVersion);
        } else if (minor >= 21) {
            // v1_21_R1 = 1.21.0-1.21.3 (ProtocolInfo-based)
            log.info("using ChannelInserterV1_21 for v" + major + "_" + minor + "_R" + revision);
            return createInserter("ChannelInserterV1_21", mcVersion);
        } else if (minor >= 17) {
            // v1_17_R1 through v1_20_R3
            log.info("using ChannelInserterV1_17 for v" + major + "_" + minor + "_R" + revision);
            return createInserter("ChannelInserterV1_17", mcVersion);
        } else if (minor == 16) {
            // v1_16_R3
            log.info("using ChannelInserterV1_16 for v" + major + "_" + minor + "_R" + revision);
            return createInserter("ChannelInserterV1_16", mcVersion);
        }

        log.info("unsupported Minecraft version: 1." + minor);
        return null;
    }

    /**
     * Create a ChannelInserter by loading the version-specific class via reflection.
     * This allows main to compile without depending on version source sets.
     */
    private static ChannelInserter createInserter(String className, String mcVersion) {
        try {
            Class<?> clazz = Class.forName("gg.playit.channel." + className);
            return (ChannelInserter) clazz.getConstructor(String.class).newInstance(mcVersion);
        } catch (Exception e) {
            log.warning("failed to load " + className + ": " + e);
            return null;
        }
    }

    /**
     * Map a CraftBukkit minor version + revision to the closest Minecraft version string.
     */
    private static String toMcVersion(int minor, int revision) {
        return switch (minor) {
            case 16 -> switch (revision) {
                case 3 -> "1.16.5";
                default -> "1.16.5";
            };
            case 17 -> "1.17.1";
            case 18 -> switch (revision) {
                case 1 -> "1.18.1";
                case 2 -> "1.18.2";
                default -> "1.18.2";
            };
            case 19 -> switch (revision) {
                case 1 -> "1.19.2";
                case 2 -> "1.19.3";
                case 3 -> "1.19.4";
                default -> "1.19.4";
            };
            case 20 -> switch (revision) {
                case 1 -> "1.20.1";
                case 2 -> "1.20.2";
                case 3 -> "1.20.4";
                case 4 -> "1.20.6";
                default -> "1.20.6";
            };
            case 21 -> switch (revision) {
                case 1 -> "1.21.1";
                case 2 -> "1.21.4";
                default -> "1.21.4";
            };
            default -> "1." + minor;
        };
    }
}
