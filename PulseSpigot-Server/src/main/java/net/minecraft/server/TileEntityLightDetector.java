package net.minecraft.server;

import xyz.krypton.spigot.config.PulseWorldConfig.Optimizations.Ticking.LightDetector; // PulseSpigot

public class TileEntityLightDetector extends TileEntity implements IUpdatePlayerListBox {

    public TileEntityLightDetector() {}

    public void c() {
        // PulseSpigot start
        if (this.world == null) return;
        LightDetector config = this.world.pulseConfig.optimizations.ticking.lightDetector;
        if (!config.tick) return;
        if (!this.world.isClientSide && this.world.getTime() % config.checkInterval == 0L) {
        // PulseSpigot end
            this.e = this.w();
            if (this.e instanceof BlockDaylightDetector) {
                ((BlockDaylightDetector) this.e).f(this.world, this.position);
            }
        }

    }
}
