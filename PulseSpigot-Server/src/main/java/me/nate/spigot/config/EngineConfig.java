package me.nate.spigot.config;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import me.nate.spigot.config.legacy.LegacyRemapper;

public abstract class EngineConfig<W extends EngineConfig.WorldConfig> extends PulseConfigSection {

    public abstract @NotNull Map<String, W> getWorldConfigs();

    public @NotNull W getWorldConfig(@NotNull String worldName) {
        Map<String, W> worldSettings = this.getWorldConfigs();
        W worldConfig = worldSettings.get(worldName);
        if (worldConfig != null) {
            return worldConfig;
        }
        return worldSettings.computeIfAbsent("default", (key) -> this.createDefaultWorldConfig());
    }

    protected abstract @NotNull W createDefaultWorldConfig();

    @Override
    public void loadProperties() {
        this.getWorldConfig("default");
    }

    public void remap() {
        LegacyRemapper.map(this, null);
    }

    public static abstract class WorldConfig extends PulseConfigSection {

        public void remap(@NotNull Object target) {
            LegacyRemapper.map(this, target);
        }

        protected static void log(String log) {
            PulseConfig.log(log);
        }

    }

}
