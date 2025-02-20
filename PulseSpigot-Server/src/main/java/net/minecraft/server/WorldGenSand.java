package net.minecraft.server;

import java.util.Random;

public class WorldGenSand extends WorldGenerator {

    private Block a;
    private int b;

    public WorldGenSand(Block block, int i) {
        this.a = block;
        this.b = i;
    }

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        // PulseSpigot start
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        IBlockData blockData = world.getTypeIfLoaded(x, y, z);
        if (blockData == null) {
            return false;
        }
        if (blockData.getBlock().getMaterial() != Material.WATER) {
        // PulseSpigot end
            return false;
        } else {
            int i = random.nextInt(this.b - 2) + 2;
            byte b0 = 2;

            // PulseSpigot start
            for (int j = x - i; j <= x + i; ++j) {
                for (int k = z - i; k <= z + i; ++k) {
                    int l = j - x;
                    int i1 = k - z;
            // PulseSpigot end

                    if (l * l + i1 * i1 <= i * i) {
                        for (int j1 = y - b0; j1 <= y + b0; ++j1) {
                            // PulseSpigot start
                            Chunk chunk = world.getChunkIfLoaded(j >> 4, k >> 4);
                            if (chunk == null) {
                                continue;
                            }
                            Block block = chunk.getBlockData(j, j1, k).getBlock();
                            // PulseSpigot end
                            if (block == Blocks.DIRT || block == Blocks.GRASS) {
                                world.setTypeAndData(chunk, new BlockPosition(j, j1, k), this.a.getBlockData(), 2); // PulseSpigot
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}
