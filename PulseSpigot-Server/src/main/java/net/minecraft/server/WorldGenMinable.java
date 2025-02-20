package net.minecraft.server;

import com.google.common.base.Predicate;
import java.util.Random;

public class WorldGenMinable extends WorldGenerator {

    private final IBlockData a;
    private final int b;
    private final float b8; // PulseSpigot
    private final Block toCheck; // PulseSpigot
    private final Predicate<IBlockData> c;

    public WorldGenMinable(IBlockData iblockdata, int i) {
        this(iblockdata, i, Blocks.STONE);
    }

    public WorldGenMinable(IBlockData iblockdata, int i, Block toCheck, Predicate<IBlockData> predicate) {
        this.a = iblockdata;
        this.b = i;
        this.b8 = (float) this.b / 8.0F; // PulseSpigot
        this.toCheck = toCheck; // PulseSpigot
        this.c = predicate;
    }

    // PulseSpigot start
    public WorldGenMinable(IBlockData iblockdata, int i, Block toCheck) {
        this(iblockdata, i, toCheck, null);
    }
    // PulseSpigot end

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        float f = random.nextFloat() * 3.1415927F;
        // PulseSpigot start - micro optimization
        float sinF = MathHelper.sin(f) * this.b8;
        float cosF = MathHelper.cos(f) * this.b8;
        double d0 = ((float) (blockposition.getX() + 8) + sinF);
        double d1 = ((float) (blockposition.getX() + 8) - sinF);
        double d2 = ((float) (blockposition.getZ() + 8) + cosF);
        double d3 = ((float) (blockposition.getZ() + 8) - cosF);
        // PulseSpigot end
        double d4 = (double) (blockposition.getY() + random.nextInt(3) - 2);
        double d5 = (double) (blockposition.getY() + random.nextInt(3) - 2);

        for (int i = 0; i < this.b; ++i) {
            float f1 = (float) i / (float) this.b;
            double d6 = d0 + (d1 - d0) * (double) f1;
            double d7 = d4 + (d5 - d4) * (double) f1;
            double d8 = d2 + (d3 - d2) * (double) f1;
            double d9 = random.nextDouble() * (double) this.b / 16.0D;
            double d10 = (double) (MathHelper.sin(3.1415927F * f1) + 1.0F) * d9 + 1.0D;
            //double d11 = (double) (MathHelper.sin(3.1415927F * f1) + 1.0F) * d9 + 1.0D; // PulseSpigot - don't calculate twice
            int j = MathHelper.floor(d6 - d10 / 2.0D);
            int k = MathHelper.floor(d7 - d10 / 2.0D); // PulseSpigot - d11 -> d10
            int l = MathHelper.floor(d8 - d10 / 2.0D);
            int i1 = MathHelper.floor(d6 + d10 / 2.0D);
            int j1 = MathHelper.floor(d7 + d10 / 2.0D); // PulseSpigot - d11 -> d10
            int k1 = MathHelper.floor(d8 + d10 / 2.0D);

            for (int l1 = j; l1 <= i1; ++l1) {
                double d12 = ((double) l1 + 0.5D - d6) / (d10 / 2.0D);

                if (d12 * d12 < 1.0D) {
                    for (int i2 = k; i2 <= j1; ++i2) {
                        double d13 = ((double) i2 + 0.5D - d7) / (d10 / 2.0D); // PulseSpigot - d11 -> d10

                        if (d12 * d12 + d13 * d13 < 1.0D) {
                            for (int j2 = l; j2 <= k1; ++j2) {
                                double d14 = ((double) j2 + 0.5D - d8) / (d10 / 2.0D);

                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    // PulseSpigot start
                                    Chunk chunk = world.getChunkIfLoaded(l1 >> 4, j2 >> 4);
                                    if (chunk != null) {
                                        IBlockData blockData = chunk.getBlockData(l1, i2, j2);

                                        if (blockData != null && this.c == null ? blockData.getBlock() == this.toCheck : this.c.apply(blockData)) {
                                            world.setTypeAndData(chunk, new BlockPosition(l1, i2, j2), this.a, 2);
                                        }
                                    }
                                    // PulseSpigot end
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }
}
