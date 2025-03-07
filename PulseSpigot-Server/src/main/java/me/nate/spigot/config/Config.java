package me.nate.spigot.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.configs.exception.OkaeriException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.AttributeRanged;
import net.minecraft.server.GenericAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.jetbrains.annotations.NotNull;
import me.nate.spigot.config.legacy.Remap;
import me.nate.spigot.config.legacy.RemapTarget;
import org.spigotmc.WatchdogThread;

@Header("This is the main configuration file for Spigot.")
@Header("As you can see, there's tons to configure. Some options may impact gameplay, so use")
@Header("with caution, and make sure you know what each option does before configuring.")
@Header("For a reference for any variable inside this file, check out the Spigot wiki at")
@Header("https://www.spigotmc.org/wiki/spigot-configuration/")
@Header("")
@Header("Forum: https://www.spigotmc.org/")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
@RemapTarget(target = org.spigotmc.SpigotConfig.class)
public final class Config extends EngineConfig<WorldConfig> {

    // For tests to work properly
    @Exclude
    private static final Config DEFAULT_INSTANCE = new Config();
    @Exclude
    private static Config INSTANCE;

    public Settings settings = new Settings();

    public static class Settings extends PulseConfigSection {

        @Comment("Whether to run in debugger mode.")
        @Remap("debug")
        public boolean debug = false;

        @Comment("Whether to write the user cache to disk only before shutdown.")
        @Remap("saveUserCacheOnStopOnly")
        public boolean saveUserCacheOnStopOnly = false;

        @Comment("Number of seconds to abort and print a thread dump when the server is unresponsive.")
        @Remap("timeoutTime")
        public int timeoutTime = 60;

        @Comment("Whether to run the restart-script on server-crash.")
        @Remap("restartOnCrash")
        public boolean restartOnCrash = false;

        @Comment("Path to a restart script to turn the server back on.")
        @Remap("restartScript")
        public String restartScript = "./start.sh";

        @Comment("Whether to bind to the network only after all plugins are loaded.")
        @Remap("lateBind")
        public boolean lateBind = false;

        @Comment("Whether the server is running behind a bungee cord proxy")
        @Remap("bungee")
        public boolean bungeecord = false;

        @Comment("Maximum number of players to publish in ping messages.")
        @Remap("playerSample")
        public int sampleCount = 12;

        @Comment("Number of ticks before shuffling the tick-order of players to prevent relog gaming to be first to get items.")
        @Remap("playerShuffle")
        public int playerShuffle = 0;

        @Comment("Whether to blacklist certain items that creative players can spawn.")
        @Remap("filterCreativeItems")
        public boolean filterCreativeItems = true;

        @Comment("Maximum number of entries in the usercache.json file.")
        @Remap("userCacheCap")
        public int userCacheSize = 1000;

        @Comment("Maximum number of entries in the integer cache, which is used extensively during world generation.")
        @Remap("intCacheLimit")
        public int cacheLimit = 1024;

        @Comment("Magic number used as a threshold for invalid move events.")
        @Remap("movedWronglyThreshold")
        public double movedWronglyThreshold = 0.0625;

        @Comment("Maximum number of blocks per second a player can move before the event is cancelled.")
        @Remap("movedTooQuicklyThreshold")
        public double movedTooQuicklyThreshold = 100.0;

        @Comment("Number of networking threads to allocate.")
        public int nettyThreads = 4;

        @Comment("Maximum attribute values for various modifiers.")
        public Attribute attribute = new Attribute();

        public static class Attribute extends PulseConfigSection {

            @CustomKey("maxHealth.max")
            @Remap("maxHealth")
            public double maxHealth = 2048;

            @CustomKey("movementSpeed.max")
            @Remap("movementSpeed")
            public double movementSpeed = 2048;

            @CustomKey("attackDamage.max")
            @Remap("attackDamage")
            public double attackDamage = 2048;

            @Override
            public void loadProperties() {
                ((AttributeRanged) GenericAttributes.maxHealth).b = maxHealth;
                ((AttributeRanged) GenericAttributes.MOVEMENT_SPEED).b = movementSpeed;
                ((AttributeRanged) GenericAttributes.ATTACK_DAMAGE).b = attackDamage;
            }

        }

        @Override
        public void loadProperties() {
            if (debug && !LogManager.getRootLogger().isTraceEnabled()) {
                // Enable debug logging
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                Configuration conf = ctx.getConfiguration();
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(org.apache.logging.log4j.Level.ALL);
                ctx.updateLoggers(conf);
            }

            if (LogManager.getRootLogger().isTraceEnabled()) {
                PulseConfig.log("Debug logging is enabled");
            } else {
                PulseConfig.log("Debug logging is disabled");
            }

            WatchdogThread.doStart(timeoutTime, restartOnCrash);

            System.setProperty("io.netty.eventLoopThreads", Integer.toString(nettyThreads));
        }

    }

    @Comment("Various messages sent to clients for errors.")
    public Messages messages = new Messages();

    public static class Messages extends PulseConfigSection {

        @Remap("restartMessage")
        public String restart = "&cServer is restarting!";

        @Remap("whitelistMessage")
        public String whitelist = "&cYou are not on the whitelist!";

        @Remap("serverFullMessage")
        public String serverFull = "&cThe server is full!";

        @Comment("You can use {0} to insert the server version.")
        @Remap("outdatedClientMessage")
        public String outdatedClient = "&7Outdated client! Please use &c{0}";

        @Comment("You can use {0} to insert the server version.")
        @Remap("outdatedServerMessage")
        public String outdatedServer = "&7Outdated server! Server is on &c{0}";

        @Comment("You can use {0} to insert the command that was tried to use (without slash).")
        @Remap("unknownCommandMessage")
        public String unknownCommand = "&7Unknown command. Type &c/help &7for help.";

    }

    public Commands commands = new Commands();

    public static class Commands extends PulseConfigSection {

        @Comment("Whether player commands should be logged.")
        @Remap("logCommands")
        public boolean log = true;

        @Comment("Whether command block commands should be logged to console.")
        @Remap("silentCommandBlocks")
        public boolean silentCommandblockConsole = false;

        @Comment("Disables the bukkit variants of the following commands and defaults to the vanilla variants.")
        @Remap("replaceCommands")
        public Set<String> replaceCommands = ImmutableSet.<String>builder()
                .add("setblock")
                .add("summon")
                .add("testforblock")
                .add("tellraw")
                .build();

        @Comment("Which commands should be excluded from spamming disconnect (if player use commands to often servers disconnect him).")
        @Remap("spamExclusions")
        public List<String> spamExclusions = ImmutableList.<String>builder()
                .add("/skill")
                .build();

        @Comment("Change the verbosity of tab complete events.")
        @Comment("Disable all tab completion with -1.")
        @Comment("Enable server commands to be queryable with 0.")
        @Comment("Require n letters to be typed before completing with n.")
        @Remap("tabComplete")
        public int tabComplete = 0;

    }

    public Stats stats = new Stats();

    public static class Stats extends PulseConfigSection {

        @Comment("Whether to disable saving of player statistics and achievements.")
        @Remap("disableStatSaving")
        public boolean disableSaving = false;

        @Comment("Map of statistics and the values they should be forced.")
        @CustomKey("forced-stats")
        private Map<String, Map<String, Integer>> forcedStats_ = new HashMap<>();

        @Exclude
        @Remap("forcedStats")
        public Map<String, Integer> forcedStats = new it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<>(); // PulseSpigot - Use fastutil map

        @Override
        public void loadProperties() {
            this.forcedStats_.forEach((key, value) ->
                    value.forEach((key2, value2) ->
                            forcedStats.put(key + "." + key2, value2)));

            if (disableSaving && forcedStats.getOrDefault("achievement.openInventory", 0) < 1) {
                PulseConfig.log(Level.WARNING, "*** WARNING *** stats.disable-saving is true but stats.forced-stats.achievement.openInventory" +
                        " isn't set to 1. Disabling stat saving without forcing the achievement may cause it to get stuck on the player's " +
                        "screen.");
            }
        }

    }

    @Comment("Per world settings.")
    public Map<String, me.nate.spigot.config.WorldConfig> worldSettings = new HashMap<>();

    @Override
    public @NotNull Map<String, me.nate.spigot.config.WorldConfig> getWorldConfigs() {
        return this.worldSettings;
    }

    @Override
    protected @NotNull me.nate.spigot.config.WorldConfig createDefaultWorldConfig() {
        return new me.nate.spigot.config.WorldConfig();
    }

    @Override
    public OkaeriConfig load() throws OkaeriException {
        super.load();
        INSTANCE = this;
        return this;
    }

    public static @NotNull Config get() {
        return (INSTANCE == null) ? DEFAULT_INSTANCE : INSTANCE;
    }

}
