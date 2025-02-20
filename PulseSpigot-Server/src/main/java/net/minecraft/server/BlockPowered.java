package net.minecraft.server;

public class BlockPowered extends Block {

    public BlockPowered(Material material, MaterialMapColor materialmapcolor) {
        super(material, materialmapcolor);
    }

    public boolean isPowerSource() {
        return true;
    }

    public int a(IBlockAccess iblockaccess, int x, int y, int z, IBlockData iblockdata, EnumDirection enumdirection) { // PulseSpigot
        return 15;
    }
}
