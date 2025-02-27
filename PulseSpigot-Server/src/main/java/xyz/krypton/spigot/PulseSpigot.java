package xyz.krypton.spigot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.command.defaults.VersionCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.krypton.spigot.command.*;
import xyz.krypton.spigot.command.plugin.PulsePluginsCommand;
import xyz.krypton.spigot.config.BukkitConfig;
import xyz.krypton.spigot.config.ConfigurationFactory;
import xyz.krypton.spigot.config.PaperConfig;
import xyz.krypton.spigot.config.Config;
import xyz.krypton.spigot.config.TacoConfig;
import xyz.krypton.spigot.config.PulseConfig;
import xyz.krypton.spigot.config.serdes.WarningStateTransformer;
import xyz.krypton.spigot.knockback.KnockbackAPI;
import xyz.krypton.spigot.knockback.KnockbackAPIDelegate;
import xyz.krypton.spigot.knockback.KnockbackAPIServerDelegate;
import xyz.krypton.spigot.knockback.KnockbackHandler;
import xyz.krypton.spigot.motion.MotionHandler;
import xyz.krypton.spigot.util.Pair;
import org.spigotmc.RestartCommand;
import org.spigotmc.TicksPerSecondCommand;
import xyz.krypton.spigot.util.yaml.YamlConfig;

public class PulseSpigot {

    private static PulseSpigot INSTANCE;
    private static String version = "1.6.0";

    private final MinecraftServer server;

    private final KnockbackHandler knockbackHandler;
    private final KnockbackAPIServerDelegate knockbackAPIServerDelegate;
    private final KnockbackAPIDelegate knockbackAPIDelegate;
    private final YamlConfig knockbackConfig;

    private final MotionHandler motionHandler;

    private final YamlConfiguration pulseConfiguration = new YamlConfiguration();
    private final YamlConfiguration bukkitConfiguration = new YamlConfiguration();
    private final YamlConfiguration spigotConfiguration = new YamlConfiguration();
    private final YamlConfiguration paperConfiguration = new YamlConfiguration();
    private final YamlConfiguration tacoConfiguration = new YamlConfiguration();
    private PulseConfig pulseConfig;
    private BukkitConfig bukkitConfig;
    private Config spigotConfig;
    private PaperConfig paperConfig;
    private TacoConfig tacoConfig;
    private boolean isReload = false;

    private final Map<String, Pair<String, Command>> commands = new HashMap<>();

    public PulseSpigot(@NotNull MinecraftServer server) {
        INSTANCE = this;

        this.server = server;

        knockbackConfig = new YamlConfig("knockback.yml");
        knockbackHandler = new KnockbackHandler();
        knockbackAPIServerDelegate = new KnockbackAPIServerDelegate(knockbackHandler);
        knockbackAPIDelegate = knockbackAPIServerDelegate;
        KnockbackAPI.registerDelegate(knockbackAPIDelegate);

        motionHandler = new MotionHandler();
    }

    public void init() {
        // Configs
        ConfigurationFactory configurationFactory = new ConfigurationFactory(this.server);
        this.pulseConfig = configurationFactory.createServerConfig(PulseConfig.class, this.pulseConfiguration, "pulse-settings", it -> it.withSerdesPack(registry -> registry.register(new WarningStateTransformer())));
        this.bukkitConfig = configurationFactory.createServerConfig(BukkitConfig.class, this.bukkitConfiguration, "bukkit-settings");
        this.spigotConfig = configurationFactory.createServerConfig(Config.class, this.spigotConfiguration, "spigot-settings");
        this.paperConfig = configurationFactory.createServerConfig(PaperConfig.class, this.paperConfiguration, "paper-settings");
        this.tacoConfig = configurationFactory.createServerConfig(TacoConfig.class, this.tacoConfiguration, "taco-settings");
        this.remapConfigs();

        // Commands
        this.registerCommand("restart", "Spigot", new RestartCommand("restart"));
        this.registerCommand("tps", "Spigot", new TicksPerSecondCommand("tps"));
        this.registerCommand("pulse", new PulseCommand());
        this.registerCommand("knockback", new KnockbackCommand());
        this.registerCommand("motion", new MotionCommand());
        this.registerCommand("wipe", new EntitiesWipeCommand());
        this.registerCommand("ping", new PingCommand());

        if (PulseConfig.get().commands.pulseVersionCommand) {
            this.registerCommand("version", new PulseVersionCommand("version"));
        } else if (PulseConfig.get().commands.versionCommandVanilla) {
            this.registerCommand("bukkit", new VersionCommand("version"));
        }

        if (PulseConfig.get().commands.pulsePluginsCommand) {
            this.registerCommand("plugin", new PulsePluginsCommand("plugin"));
        } else if (PulseConfig.get().commands.pluginsCommandVanilla) {
            this.registerCommand("bukkit", new PluginsCommand("plugins"));
        }
    }

    public @NotNull YamlConfiguration getPulseSpigotConfiguration() {
        return this.pulseConfiguration;
    }

    public @NotNull PulseConfig getPulseSpigotConfig() {
        return Objects.requireNonNull(this.pulseConfig, "PulseSpigotConfig not initialized");
    }

    public @NotNull YamlConfiguration getBukkitConfiguration() {
        return this.bukkitConfiguration;
    }

    public @NotNull BukkitConfig getBukkitConfig() {
        return Objects.requireNonNull(this.bukkitConfig, "BukkitConfig not initialized");
    }

    public @NotNull YamlConfiguration getSpigotConfiguration() {
        return this.spigotConfiguration;
    }

    public @NotNull Config getSpigotConfig() {
        return Objects.requireNonNull(this.spigotConfig, "SpigotConfig not initialized");
    }

    public @NotNull YamlConfiguration getPaperConfiguration() {
        return this.paperConfiguration;
    }

    public @NotNull PaperConfig getPaperConfig() {
        return Objects.requireNonNull(this.paperConfig, "PaperConfig not initialized");
    }

    public @NotNull YamlConfiguration getTacoConfiguration() {
        return this.tacoConfiguration;
    }

    public @NotNull TacoConfig getTacoConfig() {
        return Objects.requireNonNull(this.tacoConfig, "TacoConfig not initialized");
    }

    public void reloadConfigs() {
        this.isReload = true;

        this.pulseConfig.load();
        this.bukkitConfig.load();
        this.spigotConfig.load();
        this.paperConfig.load();
        this.tacoConfig.load();

        this.server.worlds.forEach(server -> server.antiXray = this.spigotConfig.getWorldConfig(server.getWorld().getName()).getAntiXray());

        this.remapConfigs();

        this.isReload = false;
    }

    public void remapConfigs() {
        this.pulseConfig.remap();
        this.spigotConfig.remap();
        this.paperConfig.remap();
        this.tacoConfig.remap();
        this.server.worlds.forEach(this::remapConfigs);
    }

    public void remapConfigs(@NotNull World world) {
        String worldName = world.getWorldName();
        this.pulseConfig.getWorldConfig(worldName).remap(world.pulseConfig);
        this.spigotConfig.getWorldConfig(worldName).remap(world.spigotConfig);
        this.paperConfig.getWorldConfig(worldName).remap(world.paperSpigotConfig);
    }

    public void registerCommand(@NotNull String name, @NotNull String fallbackPrefix, @NotNull Command command) {
        this.commands.put(name, Pair.of(fallbackPrefix, command));
    }

    public void registerCommand(@NotNull String name, @NotNull Command command) {
        this.registerCommand(name, "PulseSpigot", command);
    }

    public void registerCommandsInServer() {
        this.commands.forEach((key, command) -> this.server.server
                .getCommandMap()
                .register(key, command.getLeft(), command.getRight()));
    }

    public boolean isProxyOnlineMode() {
        return Bukkit.getOnlineMode() || (this.spigotConfig.settings.bungeecord && this.pulseConfig.settings.proxyOnlineMode);
    }

    public static @NotNull PulseSpigot get() {
        return Objects.requireNonNull(INSTANCE, "PulseSpigot not initialized");
    }

    public MotionHandler getMotionHandler() {
        return motionHandler;
    }

    public YamlConfig getKnockbackConfig() {
        return knockbackConfig;
    }

    public KnockbackHandler getKnockbackHandler() {
        return knockbackHandler;
    }

    public static String getVersion() {
        return version;
    }
}
