package net.minecraft.server;

import java.util.Random;

public class WorldGenLiquids extends WorldGenerator {

    private Block a;

    public WorldGenLiquids(Block block) {
        this.a = block;
    }

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        // PulseSpigot start
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        Chunk chunk = world.getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return false;
        }
        // PulseSpigot end
        if (chunk.getBlockData(x, y + 1, z).getBlock() != Blocks.STONE) { // PulseSpigot
            return false;
        } else if (chunk.getBlockData(x, y - 1, z).getBlock() != Blocks.STONE) { // PulseSpigot
            return false;
        // PulseSpigot start
        }
        Block block = chunk.getBlockData(x, y, z).getBlock();
        if (block.getMaterial() != Material.AIR && block != Blocks.STONE) {
            return false;
        // PulseSpigot end
        } else {
            int i = 0;
            // PulseSpigot start
            int j = 0;

            Chunk chunkWest = world.getChunkIfLoaded((x - 1) >> 4, z >> 4);
            if (chunkWest != null) {
                if (chunkWest.getBlockData(x - 1, y, z).getBlock() == Blocks.STONE) ++i;
                if (chunkWest.isEmpty(x - 1, y, z)) ++j;
            }

            Chunk chunkEast = world.getChunkIfLoaded((x + 1) >> 4, z >> 4);
            if (chunkEast != null) {
                if (chunkEast.getBlockData(x + 1, y, z).getBlock() == Blocks.STONE) ++i;
                if (chunkEast.isEmpty(x + 1, y, z)) ++j;
            }

            Chunk chunkNorth = world.getChunkIfLoaded(x >> 4, (z - 1) >> 4);
            if (chunkNorth != null) {
                if (chunkNorth.getBlockData(x, y, z - 1).getBlock() == Blocks.STONE) ++i;
                if (chunkNorth.isEmpty(x, y, z - 1)) ++j;
            }

            Chunk chunkSouth = world.getChunkIfLoaded(x >> 4, (z + 1) >> 4);
            if (chunkSouth != null) {
                if (chunkSouth.getBlockData(x, y, z + 1).getBlock() == Blocks.STONE) ++i;
                if (chunkSouth.isEmpty(x, y, z + 1)) ++j;
            }
            // PulseSpigot end

            if (i == 3 && j == 1) {
                world.setTypeAndData(chunk, blockposition, this.a.getBlockData(), 2); // PulseSpigot
                world.a(this.a, blockposition, random);
            }

            return true;
        }
    }
}
