package net.minecraft.server;

public abstract class BlockContainer extends Block implements IContainer {

    protected BlockContainer(Material material) {
        this(material, material.r());
    }

    protected BlockContainer(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
        this.isTileEntity = true;
    }

    protected boolean a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
    // PulseSpigot start
        return this.a(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), enumdirection);
    }

    protected boolean a(World world, int x, int y, int z, EnumDirection enumdirection) {
        return world.getType(x, y, z, enumdirection).getBlock().getMaterial() == Material.CACTUS;
    }
    // PulseSpigot end

    protected boolean e(World world, BlockPosition blockposition) {
    // PulseSpigot start
        return this.e(world, blockposition.getX(), blockposition.getY(), blockposition.getZ());
    }

    protected boolean e(World world, int x, int y, int z) {
        return this.a(world, x, y, z, EnumDirection.NORTH) || this.a(world, x, y, z, EnumDirection.SOUTH) || this.a(world, x, y, z, EnumDirection.WEST) || this.a(world, x, y, z, EnumDirection.EAST);
    }
    // PulseSpigot end

    public int b() {
        return -1;
    }

    public void remove(World world, BlockPosition blockposition, IBlockData iblockdata) {
        super.remove(world, blockposition, iblockdata);
        world.t(blockposition);
    }

    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        super.a(world, blockposition, iblockdata, i, j);
        TileEntity tileentity = world.getTileEntity(blockposition);

        return tileentity == null ? false : tileentity.c(i, j);
    }
}
