package xyz.krypton.spigot.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.configs.exception.OkaeriException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

@Header("This is the main configuration file for TacoSpigot.")
@Header("As you can see, there's tons to configure. Some options may impact gameplay, so use")
@Header("with caution, and make sure you know what each option does before configuring.")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class TacoConfig extends EngineConfig<TacoWorldConfig> {

    // For tests to work properly
    @Exclude
    private static final TacoConfig DEFAULT_INSTANCE = new TacoConfig();
    @Exclude
    private static TacoConfig INSTANCE;

    @Comment("Per world settings.")
    public Map<String, TacoWorldConfig> worldSettings = new HashMap<>();

    @Override
    public @NotNull Map<String, TacoWorldConfig> getWorldConfigs() {
        return this.worldSettings;
    }

    @Override
    protected @NotNull TacoWorldConfig createDefaultWorldConfig() {
        return new TacoWorldConfig();
    }

    @Override
    public OkaeriConfig load() throws OkaeriException {
        super.load();
        INSTANCE = this;
        return this;
    }

    public static @NotNull TacoConfig get() {
        return (INSTANCE == null) ? DEFAULT_INSTANCE : INSTANCE;
    }

}
