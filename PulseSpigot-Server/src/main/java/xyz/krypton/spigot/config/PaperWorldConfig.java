package xyz.krypton.spigot.config;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import xyz.krypton.spigot.config.legacy.Remap;
import xyz.krypton.spigot.config.legacy.RemapTarget;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
@RemapTarget(target = org.github.paperspigot.PaperSpigotWorldConfig.class)
public final class PaperWorldConfig extends EngineConfig.WorldConfig {

    @Comment("Whether to revert tnt cannon logic to 1.7.")
    @Remap("fixCannons")
    public boolean fixCannons = false;

    @Comment("Percent of damage to receive while blocking.")
    @Remap("playerBlockingDamageMultiplier")
    public float playerBlockingDamageMultiplier = 0.5F;

    @Comment("Whether to delete invalid mob spawner items.")
    @Remap("removeInvalidMobSpawnerTEs")
    public boolean removeInvalidMobSpawnerTileEntities = true;

    @Comment("Whether to remove dead entities in the explosion radius.")
    @Remap("optimizeExplosions")
    public boolean optimizeExplosions = false;

    @Comment("Number of ticks to check for mob spawner locations.")
    @Remap("mobSpawnerTickRate")
    public int mobSpawnerTickRate = 1;

    @Comment("Number of ticks to check for grass spread.")
    @Comment("Bumping this value will increase performance, but grass will spread slower.")
    public int grassSpreadTickRate = 1;

    @Comment("Whether to cache chunks when sending packets.")
    @Remap("cacheChunkMaps")
    public boolean cacheChunkMaps = false;

    @Comment("Magic number for how loud tnt explosions should be.")
    @Remap("tntExplosionVolume")
    public float tntExplosionVolume = 4.0F;

    @Comment("Whether to disable the suffocation check before every teleportation event.")
    @Remap("disableTeleportationSuffocationCheck")
    public boolean disableTeleportationSuffocationCheck = false;

    @Comment("Y-level at which squids will spawn.")
    public SquidSpawnHeight squidSpawnHeight = new SquidSpawnHeight();

    public static class SquidSpawnHeight extends PulseConfigSection {

        @Remap("squidMinSpawnHeight")
        public double minimum = 45.0;

        @Remap("squidMaxSpawnHeight")
        public double maximum = 63.0;

    }

    @Comment("Maximum number of blocks certain plants will grow.")
    public MaxGrowthHeight maxGrowthHeight = new MaxGrowthHeight();

    public static class MaxGrowthHeight extends PulseConfigSection {

        @Remap("cactusMaxHeight")
        public int cactus = 3;

        @Remap("reedMaxHeight")
        public int reeds = 3;

    }

    @Comment("Number of ticks before a fish can be caught.")
    public FishingTimeRange fishingTimeRange = new FishingTimeRange();

    public static class FishingTimeRange extends PulseConfigSection {

        @CustomKey("fishingMinTicks")
        public int minimumTicks = 100;

        @CustomKey("fishingMaxTicks")
        public int maximumTicks = 900;

    }

    @Comment("Magic number for food exhaustion rate for certain events.")
    public PlayerExhaustion playerExhaustion = new PlayerExhaustion();

    public static class PlayerExhaustion extends PulseConfigSection {

        @Remap("blockBreakExhaustion")
        public float blockBreak = 0.025F;

        @Remap("playerSwimmingExhaustion")
        public float swimming = 0.015F;

    }

    public DespawnRanges despawnRanges = new DespawnRanges();

    public static class DespawnRanges extends PulseConfigSection {

        @Comment("Distance in blocks that mobs are randomly queued for removal.")
        @Remap("softDespawnDistance")
        @CustomKey("soft")
        public int soft_ = 32;

        @Exclude
        public int soft = 0;

        @Comment("Distance in blocks that mobs are immediately removed.")
        @Remap("hardDespawnDistance")
        @CustomKey("hard")
        public int hard_ = 128;

        @Exclude
        public int hard = 0;

        @Override
        public void loadProperties() {
            if (soft_ > hard_) {
                soft_ = hard_;
            }

            soft = soft_ * soft_;
            hard = hard_ * hard_;
        }

    }

    @Comment("Y-level at which falling blocks will despawn.")
    @Remap("fallingBlockHeightNerf")
    public int fallingBlockHeightNerf = 0;

    @Comment("Whether to remove certain entities when entering unloaded chunks.")
    public RemoveUnloaded removeUnloaded = new RemoveUnloaded();

    public static class RemoveUnloaded extends PulseConfigSection {

        @Remap("removeUnloadedEnderPearls")
        public boolean enderpearls = true;

        @Remap("removeUnloadedTNTEntities")
        public boolean tntEntities = true;

        @Remap("removeUnloadedFallingBlocks")
        public boolean fallingBlocks = true;

    }

    @Comment("Whether to load chunks when certain entities enter them.")
    public LoadChunks loadChunks = new LoadChunks();

    public static class LoadChunks extends PulseConfigSection {

        @Remap("loadUnloadedEnderPearls")
        public boolean enderpearls = false;

        @Remap("loadUnloadedTNTEntities")
        public boolean tntEntities = false;

        @Remap("loadUnloadedFallingBlocks")
        public boolean fallingBlocks = false;

    }

    public GameMechanics gameMechanics = new GameMechanics();

    public static class GameMechanics extends PulseConfigSection {

        @Comment("Whether boats should always drop boat items.")
        @Remap("boatsDropBoats")
        public boolean boatsDropBoats = false;

        @Comment("Whether to disable critical hits in PvP.")
        @Remap("disablePlayerCrits")
        public boolean disablePlayerCrits = false;

        @Comment("Whether to allow chests to open with cats on top of them.")
        @Remap("disableChestCatDetection")
        public boolean disableChestCatDetections = false;

        @Comment("Whether to never send end credits.")
        @Remap("disableEndCredits")
        public boolean disableEndCredits = false;

    }

    @Comment("Whether to apply void damage to entities on top of a nether portal.")
    @Remap("netherVoidTopDamage")
    public boolean netherCeilingVoidDamage = false;

    @Comment("Whether liquids should drain faster than normal.")
    public FastDrain fastDrain = new FastDrain();

    public static class FastDrain extends PulseConfigSection {

        @Remap("fastDrainLava")
        public boolean lava = false;

        @Remap("fastDrainWater")
        public boolean water = false;

    }

    @Comment("Speed in ticks at which lava flows in different worlds.")
    public LavaFlowSpeed lavaFlowSpeed = new LavaFlowSpeed();

    public static class LavaFlowSpeed extends PulseConfigSection {

        @Remap("lavaFlowSpeedNormal")
        public int normal = 30;

        @Remap("lavaFlowSpeedNether")
        public int nether = 10;

    }

    @Comment("Whether entities should not receive knockback from explosions.")
    @Remap("disableExplosionKnockback")
    public boolean disableExplosionKnockback = false;

    @Comment("Whether undead horses are allowed to be leashed.")
    @Remap("allowUndeadHorseLeashing")
    public boolean allowUndeadHorseLeashing = false;

    @Comment("Speed in ticks at which water flows over lava.")
    @Remap("waterOverLavaFlowSpeed")
    public int waterOverLavaFlowSpeed = 5;

    @Comment("Y-level at which primed tnt will despawn.")
    @Remap("tntEntityHeightNerf")
    public int tntEntityHeightNerf = 0;

    @Comment("Whether to recognize the hopper-check configuration options.")
    @Remap("useHopperCheck")
    public boolean useHopperCheck = false;

    @Comment("Whether all chunks are allowed to spawn slimes.")
    @Remap("allChunksAreSlimeChunks")
    public boolean allChunksAreSlimeChunks = false;

    @Comment("Number of ticks to update inventory containers.")
    @Remap("containerUpdateTickRate")
    public int containerUpdateTickRate = 1;

    @Comment("Whether to keep spawn chunks loaded at all times.")
    @Remap("keepSpawnInMemory")
    public boolean keepSpawnLoaded = true;

    @Comment("Whether falling blocks should not break when colliding with signs.")
    @Remap("fallingBlocksCollideWithSigns")
    public boolean fallingBlocksCollideWithSigns = false;

    @Comment("Whether to not send cave and ambient sounds.")
    @Remap("disableMoodSounds")
    public boolean disableMoodSounds = false;

    @Comment("Whether to use asynchronous lighting for better performance.")
    @Remap("useAsyncLighting")
    public boolean useAsyncLightning = false;

    @Comment("Maximum number of blocks to search for a nether portal before generating a new one.")
    @Remap("portalSearchRadius")
    public int portalSearchRadius = 128;

    @Comment("Whether to disable thunder events.")
    @Remap("disableThunder")
    public boolean disableThunder = false;

    @Comment("Whether to disable ice and snow formation.")
    @Remap("disableIceAndSnow")
    public boolean disableIceAndSnow = false;

    @Comment("Maximum number of objects that follow the tick loop.")
    @Comment("Use with extreme caution.")
    @Remap("tickNextTickCap")
    public int tickNextTickListCap = 10000;

    @Comment("Whether or not to always tick redstone even if the number of ticking objects is above the limit.")
    @Remap("tickNextTickListCapIgnoresRedstone")
    public boolean tickNextTickListCapIgnoresRedstone = false;

    @Comment("Whether to enable certain features during world generation.")
    public GeneratorSettings generatorSettings = new GeneratorSettings();

    public static class GeneratorSettings extends PulseConfigSection {

        @Remap("generateCanyon")
        public boolean canyon = true;

        @Remap("generateCaves")
        public boolean caves = true;

        @Remap("generateDungeon")
        public boolean dungeon = true;

        @Remap("generateFortress")
        public boolean fortress = true;

        @Remap("generateMineshaft")
        public boolean mineshaft = true;

        @Remap("generateMonument")
        public boolean monument = true;

        @Remap("generateStronghold")
        public boolean stronghold = true;

        @Remap("generateTemple")
        public boolean temple = true;

        @Remap("generateVillage")
        public boolean village = true;

        @Remap("generateFlatBedrock")
        public boolean flatBedrock = false;

    }

    @Comment("Whether block locations are allowed to be tab completed.")
    @Remap("allowBlockLocationTabCompletion")
    public boolean allowBlockLocationTabCompletion = true;

    @Comment("Whether to remove corrupted tile entities.")
    public boolean removeCorruptTileEntities = false;

}
