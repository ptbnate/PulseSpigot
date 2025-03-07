package me.nate.spigot.config;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public final class PulseWorldConfig extends EngineConfig.WorldConfig {

    public Settings settings = new Settings();

    public static class Settings extends PulseConfigSection {

        @Comment("Whether to save fireworks and arrows.")
        public boolean saveFireworksAndArrows = true;

    }

    @Comment("Optimizations that could improve performance, but may cause some issues.")
    @Comment("Use with caution.")
    public Optimizations optimizations = new Optimizations();

    public static class Optimizations extends PulseConfigSection {

        @Comment("Whether to show explosion particles.")
        @Comment("Disabling this option mainly helps clients performance, but may improve performance a bit.")
        public boolean explosionParticles = true;

        @Comment("Whether to sync TNT position in water.")
        @Comment("Disabling this option may improve performance a bit, but it may cause visual issues for clients.")
        public boolean syncTntInWater = true;

        @Comment("Whether entities collisions should be handled.")
        @Comment("Disabling this option may improve performance a bit, but it will break entities collisions.")
        public boolean entitiesCollisions = true;

        @Comment("Whether to optimize armor stands by disabling it's collisions and gravity.")
        public boolean optimizeArmorStands = false;

        @Comment("Whether to handle head rotations for entities.")
        @Comment("Disabling this option may improve performance a bit, but it will cause visual issues for clients.")
        @Comment("This option doesn't applies for players and villagers - they will always have head rotation handled.")
        public boolean handleHeadRotation = true;

        @Comment("Configuration of fluids flow.")
        public Flow flow = new Flow();

        public static class Flow extends PulseConfigSection {

            @Comment("Whether water should flow.")
            public boolean water = true;

            @Comment("Whether lava should flow.")
            public boolean lava = true;

        }

        @Comment("Configuration of ticking specific mechanics.")
        public Ticking ticking = new Ticking();

        public static class Ticking extends PulseConfigSection {

            @Comment("Whether chunks should be ticked.")
            @Comment("Disabling this option can improve performance by a lot, but it may cause lots of issues.")
            @Comment("Use with caution and test, if you really could use this option.")
            public boolean chunks = true;

            @Comment("Whether fire should be ticked.")
            @Comment("Disabling this option can improve performance, but fire won't spread or extinguish.")
            public boolean fire = true;

            @Comment("Configuration of fluids ticking.")
            public Fluids fluids = new Fluids();

            public static class Fluids extends PulseConfigSection {

                @Comment("Whether water should be ticked.")
                @Comment("Disabling this option can improve performance, but fluids won't spread or flow.")
                public boolean water = true;

                @Comment("Whether lava should be ticked.")
                @Comment("Disabling this option can improve performance, but fluids won't spread or flow.")
                public boolean lava = true;

            }

            @Comment("Configuration of light detector ticking.")
            public LightDetector lightDetector = new LightDetector();

            public static class LightDetector extends PulseConfigSection {

                @Comment("Whether light detector should tick.")
                @Comment("Disabling this option can improve performance by a lot, but will break mechanisms relaying on it.")
                public boolean tick = true;

                @Comment("How often light detector should be ticked (in ticks).")
                public long checkInterval = 20L;

            }

        }

    }

}
