package net.minecraft.server;

public class ChunkCache implements IBlockAccess {

    protected int a;
    protected int b;
    protected Chunk[][] c;
    protected boolean d;
    protected World e;

    public ChunkCache(World world, BlockPosition blockposition, BlockPosition blockposition1, int i) {
        this.e = world;
        this.a = blockposition.getX() - i >> 4;
        this.b = blockposition.getZ() - i >> 4;
        int j = blockposition1.getX() + i >> 4;
        int k = blockposition1.getZ() + i >> 4;

        this.c = new Chunk[j - this.a + 1][k - this.b + 1];
        this.d = true;

        int l;
        int i1;

        for (l = this.a; l <= j; ++l) {
            for (i1 = this.b; i1 <= k; ++i1) {
                this.c[l - this.a][i1 - this.b] = world.getChunkIfLoaded(l, i1); // Paper
            }
        }

        for (l = blockposition.getX() >> 4; l <= blockposition1.getX() >> 4; ++l) {
            for (i1 = blockposition.getZ() >> 4; i1 <= blockposition1.getZ() >> 4; ++i1) {
                Chunk chunk = this.c[l - this.a][i1 - this.b];

                if (chunk != null && !chunk.c(blockposition.getY(), blockposition1.getY())) {
                    this.d = false;
                }
            }
        }

    }

    public TileEntity getTileEntity(BlockPosition blockposition) {
        int i = (blockposition.getX() >> 4) - this.a;
        int j = (blockposition.getZ() >> 4) - this.b;

        return this.c[i][j].a(blockposition, Chunk.EnumTileEntityState.IMMEDIATE);
    }

    // PulseSpigot start
    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        int i = (x >> 4) - this.a;
        int j = (z >> 4) - this.b;

        return this.c[i][j].a(new BlockPosition(x, y, z), Chunk.EnumTileEntityState.IMMEDIATE);
    }

    public IBlockData getType(BlockPosition blockposition) {
        return this.getType(blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    @Override
    public IBlockData getType(int x, int y, int z) {
        if (y >= 0 && y < 256) {
            int i = (x >> 4) - this.a;
            int j = (z >> 4) - this.b;
    // PulseSpigot end

            if (i >= 0 && i < this.c.length && j >= 0 && j < this.c[i].length) {
                Chunk chunk = this.c[i][j];

                if (chunk != null) {
                    return chunk.getBlockData(x, y, z ); // PulseSpigot
                }
            }
        }

        return Blocks.AIR.getBlockData();
    }
    // PulseSpigot end

    public boolean isEmpty(BlockPosition blockposition) {
        return this.getType(blockposition).getBlock().getMaterial() == Material.AIR;
    }

    public int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = this.getType(blockposition);

        return iblockdata.getBlock().b((IBlockAccess) this, blockposition, iblockdata, enumdirection);
    }
}
