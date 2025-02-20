package xyz.krypton.spigot.config;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Warning;

@Header("This is the main configuration file for Bukkit.")
@Header("As you can see, there's actually not that much to configure without any plugins.")
@Header("For a reference for any variable inside this file, check out the Bukkit Wiki at")
@Header("https://bukkit.fandom.com/wiki/Bukkit.yml")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class BukkitConfig extends PulseConfigSection {

    public Settings settings = new Settings();

    public static class Settings extends PulseConfigSection {

        @Comment("Whether the end world is loaded.")
        public boolean allowEnd = true;

        @Comment("Should a warning message be sent to console when the server is lagging or behind.")
        public boolean warnOnOverload = true;

        @Comment("On restart, plugins in this folder will be moved and updated.")
        public String updateFolder = "update";

        @Comment("TBH I don't know what is it, but maybe here it is explained better https://bukkit.fandom.com/wiki/Permissions.yml.")
        public String permissionsFile = "permissions.yml";

        @Comment("Enable the /timings command to track the execution time of events by plugins.")
        public boolean pluginProfiling = false;

        @Comment("Delay in milliseconds for a client to reconnect.")
        @Comment("Disabled if less than or equal to 0.")
        public int connectionThrottle = 4000;

        @Comment("Whether to publish the plugin list in pings.")
        public boolean queryPlugins = false;

        @Comment("Should a warning message be sent to console when a deprecated event is used by a plugin.")
        public Warning.WarningState deprecatedVerbose = Warning.WarningState.DEFAULT;

        @Comment("Message sent to clients before shutdown.")
        public String shutdownMessage = "Server closed!";

        @Comment("Whether to not check if the login location is safe.")
        public boolean useExactLoginLocation = false;

        @Comment("Directory for where world files are stored.")
        public String worldContainer = ".";

    }

    public ChunkGC chunkGC = new ChunkGC();

    public static class ChunkGC extends PulseConfigSection {

        @Comment("Ticks between each chunk garbage collection event.")
        @Comment("Disabled if set to 0.")
        public int periodInTicks = 600;

        @Comment("Number of chunks that must have been loaded since the last chunk garbage collection event before another event is queued.")
        @Comment("Disabled if set to 0.")
        public int loadThreshold = 0;

    }

    public SpawnLimits spawnLimits = new SpawnLimits();

    public static class SpawnLimits extends PulseConfigSection {

        @Comment("Maximum number of naturally spawned monsters per-world.")
        public int monsters = 80;
        @Comment("Maximum number of naturally spawned animals per-world.")
        public int animals = 15;
        @Comment("Maximum number of naturally spawned water animals per-world.")
        public int waterAnimals = 5;
        @Comment("Maximum number of naturally spawned ambient entities per-world")
        public int ambient = 15;

    }

    public TicksPer ticksPer = new TicksPer();

    public static class TicksPer extends PulseConfigSection {

        @Comment("Number of ticks before considering a natural animal spawn event.")
        @Comment("Below 0 will reset to server default.")
        public int animalSpawns = 400;

        @Comment("Number of ticks before considering a natural monster spawn event.")
        @Comment("Below 0 will reset to server default.")
        public int monsterSpawn = 1;

        @Comment("Number of ticks before all worlds are auto-saved.")
        @Comment("Below 0 will reset to server default, so set to a high value if you want to disable.")
        public int autosave = 6000;

    }

    public Database database = new Database();

    public static class Database extends PulseConfigSection {

        public String username = "bukkit";
        public String isolation = "SERIALIZABLE";
        public String driver = "org.sqlite.JDBC";
        public String password = "walrus";
        public String url = "jdbc:sqlite:{DIR}{NAME}.db";

    }

    @Comment("Worlds generators settings (see https://bukkit.fandom.com/wiki/Bukkit.yml#*OPTIONAL*_worlds).")
    public Map<String, WorldConfig> worlds = new LinkedHashMap<>();

    public static class WorldConfig extends PulseConfigSection {

        private String generator;

        public WorldConfig(String generator) {
            this.generator = generator;
        }

        public String getGenerator() {
            return this.generator;
        }

    }

}
