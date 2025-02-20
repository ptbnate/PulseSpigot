package net.minecraft.server;

import java.util.Random;

public class WorldGenGrass extends WorldGenerator {

    private final IBlockData a;

    public WorldGenGrass(BlockLongGrass.EnumTallGrassType blocklonggrass_enumtallgrasstype) {
        this.a = Blocks.TALLGRASS.getBlockData().set(BlockLongGrass.TYPE, blocklonggrass_enumtallgrasstype);
    }

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        Block block;

        // PulseSpigot start
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        Chunk chunk = world.getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return false;
        }
        while (blockposition.getY() > 0) {
            Material material = chunk.getBlockData(x, y, z).getBlock().getMaterial();
            if (material != Material.AIR && material != Material.LEAVES) {
                break;
            }
            y -= 1;
        }

        for (int i = 0; i < 128; ++i) {
            BlockPosition blockposition1 = new BlockPosition(x + random.nextInt(8) - random.nextInt(8), y + random.nextInt(4) - random.nextInt(4), z + random.nextInt(8) - random.nextInt(8));
            int xLoop = blockposition1.getX();
            int yLoop = blockposition1.getY();
            int zLoop = blockposition1.getZ();
            Chunk chunkLoop = world.getChunkIfLoaded(xLoop >> 4, zLoop >> 4);
            if (chunkLoop == null) {
                continue;
            }
            if (chunkLoop.isEmpty(xLoop, yLoop, zLoop) && Blocks.TALLGRASS.f(world, blockposition1, this.a)) {
                world.setTypeAndData(chunkLoop, blockposition1, this.a, 2);
        // PulseSpigot end
            }
        }

        return true;
    }
}
