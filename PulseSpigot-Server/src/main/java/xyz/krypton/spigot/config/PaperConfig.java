package xyz.krypton.spigot.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.configs.exception.OkaeriException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.Items;
import org.bukkit.Material;
import org.github.paperspigot.PaperSpigotConfig;
import org.jetbrains.annotations.NotNull;
import xyz.krypton.spigot.config.legacy.Remap;
import xyz.krypton.spigot.config.legacy.RemapTarget;

@Header("This is the main configuration file for PaperSpigot.")
@Header("As you can see, there's tons to configure. Some options may impact gameplay, so use")
@Header("with caution, and make sure you know what each option does before configuring.")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
@RemapTarget(target = PaperSpigotConfig.class)
public final class PaperConfig extends EngineConfig<PaperWorldConfig> {

    // For tests to work properly
    @Exclude
    private static final PaperConfig DEFAULT_INSTANCE = new PaperConfig();
    @Exclude
    private static PaperConfig INSTANCE;

    public Settings settings = new Settings();

    public static class Settings extends PulseConfigSection {

        @Comment("Whether interaction packets should be limited from clients.")
        @Remap("interactLimitEnabled")
        public boolean limitPlayerInteractions = true;

        @Comment("Speed at which baby zombies can move.")
        @Remap("babyZombieMovementSpeed")
        public double babyZombieMovementSpeed = 0.5;

        @Comment("Multiplier for critical hit damage.")
        public float criticalHitMultiplier = 1.5F;

        @Comment("Whether arrow projectiles should have a random factor (like in vanilla minecraft).")
        public boolean includeRandomnessInArrowTrajectory = true;

        @Comment("Whether arrow projectiles should have chance to deal extra damage on critical hit.")
        public boolean includeRandomnessInArrowDamage = true;

        @Comment("Whether to save player data to disk.")
        @Comment("Disable this only, if you really don't want to save player data to disk.")
        public boolean savePlayerData = true;

        @Override
        public void loadProperties() {
            if (!this.limitPlayerInteractions) {
                PulseConfig.log("Disabling player interaction limiter, your server may be more vulnerable to malicious users");
            }

            if (!this.savePlayerData) {
                PulseConfig.log(Level.WARNING, "Player Data Saving is currently disabled. Any changes to your players data, " +
                        "such as inventories, experience points, advancements and the like will not be saved when they log out.");
            }
        }

    }

    @Comment("Percentage improvement to player damage with certain potions.")
    public EffectModifiers effectModifiers = new EffectModifiers();

    public static class EffectModifiers extends PulseConfigSection {

        @Remap("strengthEffectModifier")
        public double strength = 1.3;

        @Remap("weaknessEffectModifier")
        public double weakness = -0.3;

    }

    @Comment("Whether to allow certain buckets to stack as items.")
    public StackableBuckets stackableBuckets = new StackableBuckets();

    public static class StackableBuckets extends PulseConfigSection {

        @Remap("stackableLavaBuckets")
        public boolean lava = false;

        @Remap("stackableWaterBuckets")
        public boolean water = false;

        @Remap("stackableMilkBuckets")
        public boolean milk = false;

        @Override
        public void loadProperties() {
            try {
                Field maxStack = Material.class.getDeclaredField("maxStack");
                maxStack.setAccessible(true);

                if (this.lava) {
                    maxStack.set(Material.LAVA_BUCKET, Material.BUCKET.getMaxStackSize());
                    Items.LAVA_BUCKET.c(Material.BUCKET.getMaxStackSize());
                }

                if (this.water) {
                    maxStack.set(Material.WATER_BUCKET, Material.BUCKET.getMaxStackSize());
                    Items.WATER_BUCKET.c(Material.BUCKET.getMaxStackSize());
                }

                if (this.milk) {
                    maxStack.set(Material.MILK_BUCKET, Material.BUCKET.getMaxStackSize());
                    Items.MILK_BUCKET.c(Material.BUCKET.getMaxStackSize());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Comment("Send a warning to console when players are moving at excessive speeds.")
    @Remap("warnForExcessiveVelocity")
    @CustomKey("warnWhenSettingExcessiveVelocity")
    public boolean warnWhenSettingExcessiveVelocity = true;

    @Comment("List of items that are allowed to keep invalid data values.")
    @Remap("dataValueAllowedItems")
    public Set<Integer> dataValueAllowedItems = new HashSet<>();

    @Comment("Per world settings.")
    public Map<String, PaperWorldConfig> worldSettings = new HashMap<>();

    @Override
    public @NotNull Map<String, PaperWorldConfig> getWorldConfigs() {
        return this.worldSettings;
    }

    @Override
    protected @NotNull PaperWorldConfig createDefaultWorldConfig() {
        return new PaperWorldConfig();
    }

    @Override
    public OkaeriConfig load() throws OkaeriException {
        super.load();
        INSTANCE = this;
        return this;
    }

    public static @NotNull PaperConfig get() {
        return (INSTANCE == null) ? DEFAULT_INSTANCE : INSTANCE;
    }

}

