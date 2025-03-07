package me.nate.spigot.config;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class TacoWorldConfig extends EngineConfig.WorldConfig {

    @Comment("Whether grass should ignore light level when growing.")
    public boolean grassIgnoresLight = false;

    @Comment("Whether redstone updates should call BlockPhysicsEvent.")
    @Comment("Calling InventoryMoveEvent could be expensive, especially if there are many redstone in use.")
    @Comment("Disabling this could increase performance, but also break some plugins (cuboids, region protection, etc.).")
    @CustomKey("redstone-fire-BlockPhysicsEvent")
    public boolean redstoneFireBlockPhysicsEvent = true;

    @Comment("Whether to allow non-players (animals, monsters etc.) to be on scoreboard.")
    @Comment("Entities collision is checking for scoreboard setting, which is expensive especially if there are many entities.")
    @Comment("Disabled by default since in most cases there is no need to have entities on scoreboard.")
    public boolean allowNonPlayerEntitiesOnScoreboards = false;

    public Hopper hopper = new Hopper();

    public static class Hopper extends PulseConfigSection {

        @Comment("Whether to use push based hoppers (items and tile entities look for hoppers, not hoppers for them).")
        @Comment("Native Minecraft behavior is pull based, so hopper looks for items and tile entities.")
        @Comment("Every tick hoppers try to find an block-inventory to extract-item from.")
        @Comment("If there is no block to extract-item from, hoppers will try to find minecarts with chest/hopper doing heavy bounding box search.")
        @Comment("If this check also fails, hopper will try to find item entity doing another heavy bounding box search.")
        @Comment("Although this patch should improve performance, it could decrease performance of dropped items.")
        public boolean pushBased = false;

        @Comment("Whether to fire InventoryMoveEvent for hoppers.")
        @Comment("Calling InventoryMoveEvent could be expensive, especially if there are many hoppers.")
        @Comment("Disabling this could increase performance, but also break some plugins (cuboids, region protection, etc.).")
        @CustomKey("fire-InventoryMoveItemEvent")
        public boolean fireInventoryMoveItemEvent = true;

    }

    @Comment("Whether to disable stacking falling blocks at 256 height.")
    public boolean disableFallingBlockStackingAt256 = false;

    @Comment("Cannoning on east and west normally is not calculated properly.")
    @Comment("Enabling this option resolve this issue, but it's not vanilla behavior and could break some TNT cannons.")
    public boolean fixEastWestCannoning = false;

    public Tnt tnt = new Tnt();

    public static class Tnt extends PulseConfigSection {

        @Comment("Whether optimize movement if tnt are traveling on long distance.")
        @Comment("This option may not reflect vanilla behavior.")
        public boolean optimizeMovement = false;

        @Comment("Whether to disable explosions processing if tnt is in water.")
        public boolean optimizeLiquidExplosions = false;

    }

}
