package net.minecraft.server;

import java.util.Random;

// CraftBukkit start
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockFadeEvent;
// CraftBukkit end

public class BlockGrass extends Block implements IBlockFragilePlantElement {

    public static final BlockStateBoolean SNOWY = BlockStateBoolean.of("snowy");

    protected BlockGrass() {
        super(Material.GRASS);
        this.j(this.blockStateList.getBlockData().set(BlockGrass.SNOWY, Boolean.valueOf(false)));
        this.a(true);
        this.a(CreativeModeTab.b);
    }

    public IBlockData updateState(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        Block block = iblockaccess.getType(blockposition.up()).getBlock();

        return iblockdata.set(BlockGrass.SNOWY, Boolean.valueOf(block == Blocks.SNOW || block == Blocks.SNOW_LAYER));
    }

    public void b(World world, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        if (!world.isClientSide) {
            int lightLevel = -1; // Paper
            // PulseSpigot start
            int x = blockposition.getX();
            int y = blockposition.getY();
            int z = blockposition.getZ();
            if (world.getLightLevel(x, y + 1, z) < 4 && (lightLevel = world.getType(x, y + 1, z).getBlock().p()) > 2) { // Paper
            // PulseSpigot end
                // CraftBukkit start
                // world.setTypeUpdate(blockposition, Blocks.DIRT.getBlockData());
                org.bukkit.World bworld = world.getWorld();
                BlockState blockState = bworld.getBlockAt(x, y, z).getState(); // PulseSpigot
                blockState.setType(CraftMagicNumbers.getMaterial(Blocks.DIRT));

                BlockFadeEvent event = new BlockFadeEvent(blockState.getBlock(), blockState);
                world.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    blockState.update(true);
                }
                // CraftBukkit end
            } else {
                boolean ignoreLightLevel = world.tacoConfigPulseSpigot.grassIgnoresLight; // TacoSpigot
                // Paper start
                // If light was calculated above, reuse it, else grab it
                if (!ignoreLightLevel && lightLevel == -1) { // TacoSpigot
                    lightLevel = world.getLightLevel(x, y + 1, z); // PulseSpigot
                }

                if (ignoreLightLevel || lightLevel >= 9) { // TacoSpigot
                // Paper end
                    for (int i = 0; i < Math.min(4, Math.max(20, (int) (4 * 100F / world.growthOdds))); ++i) { // Spigot
                        // PulseSpigot start
                        int x1 = x + random.nextInt(3) - 1;
                        int y1 = y + random.nextInt(5) - 3;
                        int z1 = z + random.nextInt(3) - 1;
                        IBlockData iblockdata1 = world.getTypeIfLoaded(x1, y1, z1);
                        if (iblockdata1 == null) {
                            continue;
                        }
                        world.getTypeIfLoaded(x1, y1 + 1, z1);
                        IBlockData iblockdata2 = world.getTypeIfLoaded(x1, y1 + 1, z1);
                        if (iblockdata2 == null) {
                            continue;
                        }
                        Block block = iblockdata1.getBlock();
                        // PulseSpigot end

                        if (iblockdata1.getBlock() == Blocks.DIRT && iblockdata1.get(BlockDirt.VARIANT) == BlockDirt.EnumDirtVariant.DIRT && world.isLightLevel(x1, y1 + 1, z1,4) && block.p() <= 2) { // Paper // PulseSpigot
                            // CraftBukkit start
                            // world.setTypeUpdate(blockposition1, Blocks.GRASS.getBlockData());
                            org.bukkit.World bworld = world.getWorld();
                            BlockState blockState = bworld.getBlockAt(x1, y1, z1).getState(); // PulseSpigot
                            blockState.setType(CraftMagicNumbers.getMaterial(Blocks.GRASS));

                            BlockSpreadEvent event = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(x, y, z), blockState); // PulseSpigot
                            world.getServer().getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                blockState.update(true);
                            }
                            // CraftBukkit end
                        }
                    }
                }

            }
        }
    }

    public Item getDropType(IBlockData iblockdata, Random random, int i) {
        return Blocks.DIRT.getDropType(Blocks.DIRT.getBlockData().set(BlockDirt.VARIANT, BlockDirt.EnumDirtVariant.DIRT), random, i);
    }

    public boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return true;
    }

    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    public void b(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        int grassUpdateRate = world.paperConfigPulseSpigot.grassSpreadTickRate; // PulseSpigot
        if (grassUpdateRate != 1 && (grassUpdateRate < 1 || (MinecraftServer.currentTick + blockposition.hashCode()) % grassUpdateRate != 0)) { return; } // Paper
        BlockPosition blockposition1 = blockposition.up();
        int i = 0;

        while (i < 128) {
            BlockPosition blockposition2 = blockposition1;
            int j = 0;

            while (true) {
                if (j < i / 16) {
                    blockposition2 = blockposition2.a(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                    if (world.getType(blockposition2.down()).getBlock() == Blocks.GRASS && !world.getType(blockposition2).getBlock().isOccluding()) {
                        ++j;
                        continue;
                    }
                } else if (world.getType(blockposition2).getBlock().material == Material.AIR) {
                    if (random.nextInt(8) == 0) {
                        BlockFlowers.EnumFlowerVarient blockflowers_enumflowervarient = world.getBiome(blockposition2).a(random, blockposition2);
                        BlockFlowers blockflowers = blockflowers_enumflowervarient.a().a();
                        IBlockData iblockdata1 = blockflowers.getBlockData().set(blockflowers.n(), blockflowers_enumflowervarient);

                        if (blockflowers.f(world, blockposition2, iblockdata1)) {
                            // world.setTypeAndData(blockposition2, iblockdata1, 3); // CraftBukkit
                            CraftEventFactory.handleBlockGrowEvent(world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), iblockdata1.getBlock(), iblockdata1.getBlock().toLegacyData(iblockdata1)); // CraftBukkit
                        }
                    } else {
                        IBlockData iblockdata2 = Blocks.TALLGRASS.getBlockData().set(BlockLongGrass.TYPE, BlockLongGrass.EnumTallGrassType.GRASS);

                        if (Blocks.TALLGRASS.f(world, blockposition2, iblockdata2)) {
                            // world.setTypeAndData(blockposition2, iblockdata2, 3); // CraftBukkit
                            CraftEventFactory.handleBlockGrowEvent(world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), iblockdata2.getBlock(), iblockdata2.getBlock().toLegacyData(iblockdata2)); // CraftBukkit
                        }
                    }
                }

                ++i;
                break;
            }
        }

    }

    public int toLegacyData(IBlockData iblockdata) {
        return 0;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockGrass.SNOWY});
    }
}
