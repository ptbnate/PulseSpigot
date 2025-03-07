package me.nate.spigot.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.configs.exception.OkaeriException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

@Header("This is the main configuration file for PulseSpigot.")
@Header("As you can see, there's tons to configure. Some options may impact gameplay, so use")
@Header("with caution, and make sure you know what each option does before configuring.")
@Header("If you need help with the configuration or have any questions related to PulseSpigot,")
@Header("join our Discord server")
@Header(" ")
@Header("Discord: https://discord.gg/24JaQ4ZE4F")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class PulseConfig extends EngineConfig<PulseWorldConfig> implements Server.Pulse.Configuration {

    // For tests to work properly
    @Exclude
    private static final PulseConfig DEFAULT_INSTANCE = new PulseConfig();
    @Exclude
    private static PulseConfig INSTANCE;

    public Commands commands = new Commands();

    public static class Commands extends PulseConfigSection {

        @Comment("Version information displayed in the console.")
        public List<String> versionInfo = Arrays.asList(
                "&8&m-=-------------------------=-",
                "&c&lPulseSpigot Info",
                "",
                " &cVersion: &f{version}",
                " &cDiscord: &fhttps://discord.gg/QMAHrmDhrF",
                " &cAuthor: &fNate (Krypton Development)",
                " &cCustom server software for the PulseNetwork",
                "&8&m-=-------------------------=-"
        );

        @Comment("Should the default version command be enabled")
        public boolean versionCommandVanilla = false;

        @Comment("Should the default plugins command be enabled")
        public boolean pluginsCommandVanilla = false;

        @Comment("Should we override the vanilla version command with Pulse's version command")
        public boolean pulseVersionCommand = true;

        @Comment("Should we override the vanilla plugins command with Pulse's plugins command")
        public boolean pulsePluginsCommand = true;
    }

    public Settings settings = new Settings();

    public static class Settings extends PulseConfigSection {

        @Comment("Instructs the server how to handle player UUIDs and data when behind a proxy.")
        @Comment("Set to match your proxy's online-mode setting.")
        public boolean proxyOnlineMode = true;

        @Comment("Whether checking if everyone is sleeping should ignore players in end/nether.")
        public boolean sleepingIgnoreOtherDimensions = false;

        @Comment("Whether to disable statistics (ESC -> Statistics) and achievements.")
        @Comment("Enabling this option could break some plugins that rely on this mechanics.")
        public boolean disableStatisticsAndAchievements = false;

        @Comment("Whether to disable namespaced commands (for eg. /minecraft:help) and only left normal commands (like /help).")
        public boolean disableNamespacedCommands = false;

        @Comment("Whether or not armor damage should be reduced. (Perfect for bedwars/practice)")
        public boolean reduceArmorDamage = false;

        @Comment("Should we obfuscate the player's health packet. Any mod which displays the opponents health will break")
        public boolean obfuscatePlayerHealth = false;

        @Comment("The delay to pick an item up.")
        public int itemPickupDelay = 40;
    }

    @Comment("Optimizations that could improve performance, but may cause some issues.")
    @Comment("Use with caution.")
    public Optimizations optimizations = new Optimizations();

    public static class Optimizations extends PulseConfigSection {

        @Comment("Whether to use fast Location#clone() method.")
        @Comment("This change applies only for 3-party usages, since #fastClone is used natively internally.")
        @Comment("It is faster, but it may cause some issues with plugins that rely on Location#clone() standard implementation.")
        public boolean fastLocationClone = false;

        @Comment("How many ticks should pass before sending time updates to players.")
        @Comment("The vanilla value is 20 ticks (1 second), but PulseSpigot uses 100 ticks (5 seconds) by default.")
        @Comment("You shouldn't notice much difference, but it may improve performance a bit.")
        @Comment("Higher values may seem more laggy for clients.")
        public int timeUpdateFrequency = 100;

        @Comment("Should we cache and flush movement cache while correcting it everytime it's wrong?")
        @Comment("This reduces the flooding of movement packets on the server, potentially improving performance and reduced resources usage.")
        public boolean optimizedMovementCacheFlushing = true;
    }

    @Comment("Configuration of exploits fixes that could be used to crash the server or cause other issues.")
    public Exploits exploits = new Exploits();

    public static class Exploits extends PulseConfigSection {

        @Comment("The max NBT size in bytes server would read.")
        @Comment("The default value is 2097152 which is ~2Mb - it's huge and allow to use exploits or dupe items.")
        @Comment("PulseSpigot by default sets it to 50000 which is ~50Kb - it should be enough for most plugins.")
        public long nbtReadLimiter = 50000L;

        @Comment("Configuration of Book and Quill limit.")
        @Comment("You can prevent some really nasty exploits with this.")
        @Comment("Check also 'nbt-read-limiter' option.")
        public Book book = new Book();

        public static class Book extends PulseConfigSection {

            @Comment("The max book title length.")
            @Comment("Optimal values is 32.")
            public int maxTitleLength = 1024;

            @Comment("The max book author length.")
            @Comment("Optimal values is 16 (player's names can't be longer).")
            public int maxAuthorLength = 1024;

            @Comment("The max amount of pages a book can have.")
            @Comment("Disable limit with -1")
            @Comment("Optimal values is 64.")
            public int maxPages = -1;

            @Comment("The max length of one page the book can have.")
            @Comment("Optimal values is 400.")
            public int maxPageLength = 2048;

        }

    }

    @Comment("Configuration of systems related to chunks.")
    public Chunks chunks = new Chunks();

    public static class Chunks extends PulseConfigSection {
        @Comment("The amount of threads used for chunk loading.")
        public int baseThreads = 2;
        @Comment("When more players join the server, new threads will be created. This value represents that how many players are needed to create more threads.")
        public int playersPerThread = 50;

        @Comment("Enabling this option prevents entities from moving in unloaded chunk (and for eg. falling down in \"slow-motion\") (Not recommended for practice servers.)")
        public boolean disableUnloadedChunksMovement = false;

        @Comment("Normally server uses bukkit.chunk-gc.load-threshold - it's define how many chunks could be loaded before GC will be triggered.")
        @Comment("This option make that value more flexible and change it dynamically.")
        @Comment("As a factor, it uses the amount of players online and multiply it by square of view-distance.")
        @Comment("Basically if there is no players online, server won't store any chunks in memory.")
        @Comment("However if there are 100 players online and view-distance is 10, server will store up to 10000 chunks in memory.")
        public boolean adaptiveChunkGC = false;

        public Async async = new Async();

        public static class Async extends PulseConfigSection {

            public Loading loading = new Loading();

            public static class Loading extends PulseConfigSection {

                @Comment("The amount of threads to use for chunk loading.")
                @Comment("If values is -1, it will be set to recommended value (amount of cores).")
                public int threads = -1;

                @Comment("Whether to use load executor per world or one shared executor.")
                public boolean executorPerWorld = false;

            }

            public Generation generation = new Generation();

            public static class Generation extends PulseConfigSection {

                @Comment("Whether to generate chunks asynchronously.")
                public boolean enabled = false;

                @Comment("Whether to use generation executor per world or one shared executor.")
                public boolean executorPerWorld = true;

                @Comment("Whether to force async generation (for eg. when custom world generator is used).")
                public boolean forceAsync = false;

                @Comment("List of worlds that should use async generation (even if they're using custom world generator, that doesn't support it). This option works independently from force-async-generation.")
                public Set<String> forceAsyncWorlds = new HashSet<>();

            }

        }

    }

    @Comment("Per world settings.")
    public Map<String, PulseWorldConfig> worldSettings = new HashMap<>();

    @Override
    public @NotNull Map<String, PulseWorldConfig> getWorldConfigs() {
        return this.worldSettings;
    }

    @Override
    protected @NotNull PulseWorldConfig createDefaultWorldConfig() {
        return new PulseWorldConfig();
    }

    @Override
    public OkaeriConfig load() throws OkaeriException {
        super.load();
        INSTANCE = this;
        return this;
    }

    @Override
    public boolean disableNamespacedCommands() {
        return this.settings.disableNamespacedCommands;
    }

    public static @NotNull PulseConfig get() {
        return (INSTANCE == null) ? DEFAULT_INSTANCE : INSTANCE;
    }

    static void log(Level level, String log, Object... params) {
        Logger logger = Bukkit.getLogger();
        if (logger == null) {
            return;
        }
        logger.log(level, log, params);
    }

    static void log(String log, Object... params) {
        log(Level.INFO, log, params);
    }

}
