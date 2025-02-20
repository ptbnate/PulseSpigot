package net.minecraft.server;

import java.util.Random;

public class WorldGenReed extends WorldGenerator {

    public WorldGenReed() {}

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        // PulseSpigot start
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        // PulseSpigot end
        for (int i = 0; i < 20; ++i) {
            // PulseSpigot start
            int x1 = x + random.nextInt(4) - random.nextInt(4);
            int z1 = z + random.nextInt(4) - random.nextInt(4);
            Chunk chunk = world.getChunkIfLoaded(x >> 4, z >> 4);
            if (chunk == null) {
                continue;
            }
            if (chunk.isEmpty(x, y, z)) {
                int y1 = y - 1;

                if (world.getType(x1 - 1, y1, z1).getBlock().getMaterial() == Material.WATER || world.getType(x1 + 1, y1, z1).getBlock().getMaterial() == Material.WATER || world.getType(x1, y1, z1 - 1).getBlock().getMaterial() == Material.WATER || world.getType(x1, y1, z + 1).getBlock().getMaterial() == Material.WATER) {
                    int j = 2 + random.nextInt(random.nextInt(3) + 1);
                    for (int k = 0; k < j; ++k) {
                        if (Blocks.REEDS.canPlace(world, x1, y, z1)) {
                            world.setTypeAndData(chunk, new BlockPosition(x1, y + k, z1), Blocks.REEDS.getBlockData(), 2);
                        }
                    }
                }
            }
        }
        // PulseSpigot end

        return true;
    }
}
