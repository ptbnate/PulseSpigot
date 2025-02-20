package net.minecraft.server;

import xyz.krypton.spigot.config.PaperConfig;

public class MobEffectAttackDamage extends MobEffectList {

    protected MobEffectAttackDamage(int i, MinecraftKey minecraftkey, boolean flag, int j) {
        super(i, minecraftkey, flag, j);
    }

    public double a(int i, AttributeModifier attributemodifier) {
        // PaperSpigot - Configurable modifiers for strength and weakness effects
        return this.id == MobEffectList.WEAKNESS.id ? (double) (PaperConfig.get().effectModifiers.weakness * (float) (i + 1)) : PaperConfig.get().effectModifiers.strength * (double) (i + 1);
    }
}
