package xyz.krypton.spigot.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import java.io.File;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.krypton.spigot.config.serdes.DecolorTransformer;

public class ConfigurationFactory {

    private final MinecraftServer server;

    public ConfigurationFactory(@NotNull MinecraftServer server) {
        this.server = server;
    }

    public  <T extends OkaeriConfig> @NotNull T createServerConfig(
            @NotNull Class<T> configClass,
            @NotNull YamlConfiguration configuration,
            @NotNull String fileOptions,
            @NotNull Consumer<T> create
    ) {
        return ConfigManager.create(configClass, it -> {
            it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(configuration), true), new SerdesCommons());
            it.withSerdesPack(registry -> {
                registry.register(new DecolorTransformer());
            });
            it.withBindFile((File) this.server.options.valueOf(fileOptions));
            it.withLogger(Bukkit.getLogger());
            it.saveDefaults();
            it.load(true);
            create.accept((T) it);
        });
    }

    public  <T extends OkaeriConfig> @NotNull T createServerConfig(
            @NotNull Class<T> configClass,
            @NotNull YamlConfiguration configuration,
            @NotNull String fileOptions
    ) {
        return this.createServerConfig(configClass, configuration, fileOptions, it -> {
        });
    }

}
