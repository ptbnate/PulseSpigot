package net.minecraft.server;

import java.util.Random;

public class WorldGenAcaciaTree extends WorldGenTreeAbstract {

    private static final IBlockData a = Blocks.LOG2.getBlockData().set(BlockLog2.VARIANT, BlockWood.EnumLogVariant.ACACIA);
    private static final IBlockData b = Blocks.LEAVES2.getBlockData().set(BlockLeaves2.VARIANT, BlockWood.EnumLogVariant.ACACIA).set(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

    public WorldGenAcaciaTree(boolean flag) {
        super(flag);
    }

    public boolean generate(World world, Random random, BlockPosition blockposition) {
        int i = random.nextInt(3) + random.nextInt(3) + 5;
        boolean flag = true;

        // PulseSpigot start
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        if (y >= 1 && y + i + 1 <= 256) {
        // PulseSpigot end
            int j;
            int k;

            for (int l = y; l <= y + 1 + i; ++l) { // PulseSpigot
                byte b0 = 1;

                if (l == y) { // PulseSpigot
                    b0 = 0;
                }

                if (l >= y + 1 + i - 2) { // PulseSpigot
                    b0 = 2;
                }

                //BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(); // PulseSpigot

                for (j = x - b0; j <= x + b0 && flag; ++j) { // PulseSpigot
                    for (k = z - b0; k <= z + b0 && flag; ++k) { // PulseSpigot
                        if (l >= 0 && l < 256) {
                            // PulseSpigot start
                            IBlockData blockData = world.getTypeIfLoaded(j, l, k);
                            if (blockData != null && !this.a(blockData.getBlock())) {
                                flag = false;
                            }
                            // PulseSpigot end
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            } else {
                // PulseSpigot start
                if (y >= 256 - i - 1) {
                    return false;
                }
                IBlockData blockData = world.getTypeIfLoaded(x, y - 1, z);
                if (blockData == null) {
                    return false;
                }
                Block block = blockData.getBlock();
                // PulseSpigot end

                if ((block == Blocks.GRASS || block == Blocks.DIRT)) { // PulseSpigot
                    this.a(world, blockposition.down());
                    EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random);
                    int i1 = i - random.nextInt(4) - 1;

                    j = 3 - random.nextInt(3);
                    k = x; // PulseSpigot
                    int j1 = z; // PulseSpigot
                    int k1 = 0;

                    int l1;

                    for (int i2 = 0; i2 < i; ++i2) {
                        l1 = y + i2;
                        if (i2 >= i1 && j > 0) {
                            k += enumdirection.getAdjacentX();
                            j1 += enumdirection.getAdjacentZ();
                            --j;
                        }

                        // PulseSpigot start
                        IBlockData blockData1 = world.getTypeIfLoaded(k, l1, j1);
                        if (blockData1 == null) {
                            continue;
                        }
                        Material material = blockData1.getBlock().getMaterial();
                        // PulseSpigot end

                        if (material == Material.AIR || material == Material.LEAVES) {
                            this.b(world, new BlockPosition(k, l1, j1)); // PulseSpigot
                            k1 = l1;
                        }
                    }

                    //BlockPosition blockposition2 = new BlockPosition(k, k1, j1); // PulseSpigot

                    int j2;

                    for (l1 = -3; l1 <= 3; ++l1) {
                        for (j2 = -3; j2 <= 3; ++j2) {
                            if (Math.abs(l1) != 3 || Math.abs(j2) != 3) {
                                this.cIfLoaded(world, k + l1, k1, j1 + j2); // PulseSpigot
                            }
                        }
                    }

                    //blockposition2 = blockposition2.up(); // PulseSpigot

                    for (l1 = -1; l1 <= 1; ++l1) {
                        for (j2 = -1; j2 <= 1; ++j2) {
                            this.cIfLoaded(world, k + l1, k1 + 1, j1 + j2); // PulseSpigot
                        }
                    }

                    this.cIfLoaded(world, k + 2, k1 + 1, j1); // PulseSpigot
                    this.cIfLoaded(world, k - 2, k1 + 1, j1); // PulseSpigot
                    this.cIfLoaded(world, k, k1 + 1, j1 + 2); // PulseSpigot
                    this.cIfLoaded(world, k, k1 + 1, j1 - 2); // PulseSpigot
                    k = x; // PulseSpigot
                    j1 = z; // PulseSpigot
                    EnumDirection enumdirection1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.a(random);

                    if (enumdirection1 != enumdirection) {
                        l1 = i1 - random.nextInt(2) - 1;
                        j2 = 1 + random.nextInt(3);
                        k1 = 0;

                        int k2;

                        for (int l2 = l1; l2 < i && j2 > 0; --j2) {
                            if (l2 >= 1) {
                                k2 = y + l2;
                                k += enumdirection1.getAdjacentX();
                                j1 += enumdirection1.getAdjacentZ();
                                // PulseSpigot start
                                IBlockData blockData2 = world.getTypeIfLoaded(k, k2, j1);
                                if (blockData2 == null) {
                                    continue;
                                }
                                Material material1 = blockData2.getBlock().getMaterial();
                                // PulseSpigot end

                                if (material1 == Material.AIR || material1 == Material.LEAVES) {
                                    this.b(world, new BlockPosition(k, k2, j1)); // PulseSpigot
                                    k1 = k2;
                                }
                            }

                            ++l2;
                        }

                        if (k1 > 0) {
                            //BlockPosition blockposition4 = new BlockPosition(k, k1, j1);

                            int i3;

                            for (k2 = -2; k2 <= 2; ++k2) {
                                for (i3 = -2; i3 <= 2; ++i3) {
                                    if (Math.abs(k2) != 2 || Math.abs(i3) != 2) {
                                        this.cIfLoaded(world, k + k2, k1, j1 + i3); // PulseSpigot
                                    }
                                }
                            }

                            //blockposition4 = blockposition4.up();

                            for (k2 = -1; k2 <= 1; ++k2) {
                                for (i3 = -1; i3 <= 1; ++i3) {
                                    this.cIfLoaded(world, k + k2, k1 + 1, j1 + i3); // PulseSpigot
                                }
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private void b(World world, BlockPosition blockposition) {
        this.a(world, blockposition, WorldGenAcaciaTree.a);
    }

    private void c(World world, BlockPosition blockposition) {
        Material material = world.getType(blockposition).getBlock().getMaterial();

        if (material == Material.AIR || material == Material.LEAVES) {
            this.a(world, blockposition, WorldGenAcaciaTree.b);
        }

    }

    // PulseSpigot start
    private void cIfLoaded(World world, int x, int y, int z) {
        IBlockData blockData = world.getTypeIfLoaded(x, y, z);
        if (blockData != null) {
            Material material = blockData.getBlock().getMaterial();

            if (material == Material.AIR || material == Material.LEAVES) {
                this.a(world, new BlockPosition(x, y, z), WorldGenAcaciaTree.b);
            }
        }
    }
    // PulseSpigot end

}
