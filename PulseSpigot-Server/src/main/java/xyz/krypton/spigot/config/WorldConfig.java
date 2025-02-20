package xyz.krypton.spigot.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import xyz.krypton.spigot.config.legacy.Remap;
import xyz.krypton.spigot.config.legacy.RemapTarget;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
@RemapTarget(target = org.spigotmc.SpigotWorldConfig.class)
public final class WorldConfig extends EngineConfig.WorldConfig {

    public AntiXray antiXray = new AntiXray();

    public static class AntiXray extends PulseConfigSection {

        @Exclude
        @Remap("antiXrayInstance")
        private org.spigotmc.AntiXray instance;

        @Comment("Whether to enable the anti-xray module.")
        @Remap("antiXray")
        public boolean enabled = true;

        @Comment("Controls which anti-xray modes to use.")
        @Comment("Mode 1 will hide all ores with stone.")
        @Comment("Mode 2 will create fake, client-side ores around the world, which can impact client lag and network latency.")
        @Remap("engineMode")
        public int engineMode = 1;

        @Comment("IDs of blocks that should be hidden in mode 1.")
        @Remap("hiddenBlocks")
        @CustomKey("hide-blocks")
        public List<Integer> hideBlocks_ = ImmutableList.<Integer>builder()
                .add(14, 15, 16, 21, 48, 49, 54, 56, 73, 74, 82, 129, 130)
                .build();

        @Exclude
        public Set<Integer> hideBlocks = ImmutableSet.<Integer>builder()
                .add(14, 15, 16, 21, 48, 49, 54, 56, 73, 74, 82, 129, 130)
                .build();

        @Comment("IDs of blocks that should be replaced in mode 2.")
        @Remap("replaceBlocks")
        @CustomKey("replace-blocks")
        public List<Integer> replaceBlocks_ = ImmutableList.<Integer>builder()
                .add(1, 5)
                .build();

        @Exclude
        public Set<Integer> replaceBlocks = ImmutableSet.<Integer>builder()
                .add(1, 5)
                .build();

        @Override
        public void loadProperties() {
            this.instance = new org.spigotmc.AntiXray(this);
            this.hideBlocks = new HashSet<>(this.hideBlocks_);
            this.replaceBlocks = new HashSet<>(this.replaceBlocks_);
        }

    }

    @Comment("Whether to remove AI from mobs that originate from mob spawners.")
    @Remap("nerfSpawnerMobs")
    public boolean nerfSpawnerMobs = false;

    @Comment("Speed at which certain crops will grow.")
    @Comment("Changing these values will not affect performance.")
    public Growth growth = new Growth();

    public static class Growth extends PulseConfigSection {

        @Remap("cactusModifier")
        public int cactusModifier = 100;

        @Remap("caneModifier")
        public int caneModifier = 100;

        @Remap("melonModifier")
        public int melonModifier = 100;

        @Remap("pumpkinModifier")
        public int pumpkinModifier = 100;

        @Remap("wheatModifier")
        public int wheatModifier = 100;

        @Remap("mushroomModifier")
        public int mushroomModifier = 100;

        @Remap("saplingModifier")
        public int saplingModifier = 100;

        @Remap("wartModifier")
        public int netherWartModifier = 100;

        @Override
        public void loadProperties() {
            this.cactusModifier = this.validate("Cactus", this.cactusModifier);
            this.caneModifier = this.validate("Cane", this.caneModifier);
            this.melonModifier = this.validate("Melon", this.melonModifier);
            this.pumpkinModifier = this.validate("Pumpkin", this.pumpkinModifier);
            this.wheatModifier = this.validate("Wheat", this.wheatModifier);
            this.mushroomModifier = this.validate("Mushroom", this.mushroomModifier);
            this.saplingModifier = this.validate("Sapling", this.saplingModifier);
            this.netherWartModifier = this.validate("Nether Wart", this.netherWartModifier);
        }

        private int validate(String crop, int modifier) {
            if (modifier == 0) {
                log("Cannot set " + crop + " growth to zero, defaulting to 100");
                modifier = 100;
            }
            return modifier;
        }

    }

    @Comment("Radius of chunks around players that mobs will naturally spawn.")
    @Remap("mobSpawnRange")
    public byte mobSpawnRange = 4;

    @Comment("Radius in blocks around players that mobs are marked as active and are ticked.")
    public EntityActivationRange entityActivationRange = new EntityActivationRange();

    public static class EntityActivationRange extends PulseConfigSection {

        @Remap("animalActivationRange")
        public int animals = 32;

        @Remap("monsterActivationRange")
        public int monsters = 32;

        @Remap("miscActivationRange")
        public int ambient = 16;

    }

    @Comment("Radius in blocks around players that the client will see.")
    @Comment("Only affects client-side performance.")
    public EntityTrackingRange entityTrackingRange = new EntityTrackingRange();

    public static class EntityTrackingRange extends PulseConfigSection {

        @Remap("playerTrackingRange")
        public int players = 48;

        @Remap("animalTrackingRange")
        public int animals = 48;

        @Remap("monsterTrackingRange")
        public int monsters = 48;

        @Remap("miscTrackingRange")
        public int misc = 32;

        @Remap("otherTrackingRange")
        public int other = 64;

    }

    @Comment("Number of ticks between each of the following events.")
    public TicksPer ticksPer = new TicksPer();

    public static class TicksPer extends OkaeriConfig {

        @Remap("hopperTransfer")
        public int hopperTransfer = 8;

        @Remap("hopperCheck")
        public int hopperCheck = 8;

    }

    @Comment("Maximum throughput of items in a hopper per tick cycle.")
    @Remap("hopperAmount")
    public int hopperAmount = 1;

    @Comment("Whether to randomly sample chunks and validate lighting integrity.")
    @Remap("randomLightUpdates")
    public boolean randomLightUpdates = false;

    @Comment("Whether to save generic structure information.")
    @Comment("Only affects certain gameplay features such as witches spawning in huts.")
    @Remap("saveStructureInfo")
    public boolean saveStructureInfo = true;

    @Comment("Maximum number of chunks to send per packet.")
    @Remap("maxBulkChunk")
    public int maxBulkChunks = 10;

    @Comment("Maximum number of other entities and entity can collide with per tick.")
    @Remap("maxCollisionsPerEntity")
    public int maxEntityCollisions = 8;

    @Comment("Radius around dragons that players will hear the death sound.")
    @Remap("dragonDeathSoundRadius")
    public int dragonDeathSoundRadius = 0;

    @Comment("Radius around withers that players will hear the spawn sound.")
    @Remap("witherSpawnSoundRadius")
    public int witherSpawnSoundRadius = 0;

    @Comment("Random seed used to generate and place villages.")
    @Remap("villageSeed")
    public int seedVillage = 10387312;

    @Comment("Random seed used to generate and place nether strongholds.")
    @Remap("largeFeatureSeed")
    public int seedFeature = 14357617;

    @Comment("Number of hunger points to subtract upon each event.")
    public Hunger hunger = new Hunger();

    public static class Hunger extends PulseConfigSection {

        @Remap("walkExhaustion")
        public float walkExhaustion = 0.2F;

        @Remap("sprintExhaustion")
        public float sprintExhaustion = 0.8F;

        @Remap("combatExhaustion")
        public float combatExhaustion = 0.3F;

        @Remap("regenExhaustion")
        public float regenExhaustion = 3F;

    }

    @Comment("Maximum number of primed tnt entities per tick.")
    @Remap("maxTntTicksPerTick")
    public int maxTntPerTick = 100;

    @Exclude
    public int currentPrimedTnt = 0;

    @Comment("Maximum number of milliseconds for entity movements tasks to have before getting skipped.")
    @Comment("Use 1000 to disable this feature, as recommended by paper.")
    public MaxTickTime maxTickTime = new MaxTickTime();

    public static class MaxTickTime extends PulseConfigSection {

        @Remap("tileMaxTickTime")
        public int tile = 50;

        @Remap("entityMaxTickTime")
        public int entity = 50;

    }

    @Comment("Set the tick update interval for hanging entities.")
    @Remap("hangingTickFrequency")
    public int hangingTickFrequency = 100;

    @Comment("Number of ticks before items on the ground will despawn.")
    @Remap("itemDespawnRate")
    public int itemDespawnRate = 6000;

    @Comment("Number of ticks before despawning arrows on the ground.")
    @Remap("arrowDespawnRate")
    public int arrowDespawnRate = 1200;

    @Comment("Radius in blocks for certain entities to merge together.")
    public MergeRadius mergeRadius = new MergeRadius();

    public static class MergeRadius extends PulseConfigSection {

        @Remap("itemMerge")
        public double item = 2.5;

        @Remap("expMerge")
        public double exp = 3.0;

    }

    @Comment("Whether to allow zombie pigmen from spawning inside nether portals.")
    @Remap("enableZombiePigmenPortalSpawns")
    public boolean enableZombiePigmenPortalSpawns = true;

    @Comment("Number of chunks to load around each player.")
    @Comment("Must be within 1 and 15.")
    @Remap("viewDistance")
    public int viewDistance = 10;

    @Comment("Radius around withers that players will hear the spawn sound.")
    @Remap("zombieAggressiveTowardsVillager")
    public boolean zombieAggressiveTowardsVillager = true;

    @Comment("Number of chunks to be updated for growth per tick.")
    @Remap("chunksPerTick")
    public int chunksPerTick = 650;

    @Comment("Prevent the tick list from increasing over time.")
    @Comment("Use with caution, because world growth is stunted.")
    @Remap("clearChunksOnTick")
    public boolean clearTickList = false;

    @Override
    public void loadProperties() {
        if (!this.saveStructureInfo) {
            log("*** WARNING *** You have selected to NOT save structure info. This may cause structures such as fortresses to not spawn mobs!");
            log("*** WARNING *** Please use this option with caution, SpigotMC is not responsible for any issues this option may cause in the future!");
        }
    }

    public @NotNull org.spigotmc.AntiXray getAntiXray() {
        return this.antiXray.instance;
    }

}
