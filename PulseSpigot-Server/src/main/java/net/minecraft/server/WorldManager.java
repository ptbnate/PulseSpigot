package net.minecraft.server;

import java.util.Iterator;

public class WorldManager implements IWorldAccess {

    private MinecraftServer a;
    private WorldServer world;

    public WorldManager(MinecraftServer minecraftserver, WorldServer worldserver) {
        this.a = minecraftserver;
        this.world = worldserver;
    }

    public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {}

    public void a(Entity entity) {
        this.world.getTracker().track(entity);
    }

    public void b(Entity entity) {
        this.world.getTracker().untrackEntity(entity);
        this.world.getScoreboard().a(entity);
    }

    public void a(String s, double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit - this.world.dimension
        this.a.getPlayerList().sendPacketNearby(d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, this.world.dimension, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1));
    }

    public void a(EntityHuman entityhuman, String s, double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit - this.world.dimension, // Paper - this.world.dimension -> this.world
        this.a.getPlayerList().sendPacketNearby(entityhuman, d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, this.world, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1));
    }

    public void a(int i, int j, int k, int l, int i1, int j1) {}

    // PulseSpigot start
    public void a(int x, int y, int z) {
        this.world.getPlayerChunkMap().flagDirty(x, y, z);
    }
    // PulseSpigot end

    public void b(BlockPosition blockposition) {}

    public void a(String s, BlockPosition blockposition) {}

    public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        // CraftBukkit - this.world.dimension
        this.a.getPlayerList().sendPacketNearby(entityhuman, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 64.0D, this.world, new PacketPlayOutWorldEvent(i, blockposition, j, false));
    }

    public void a(int i, BlockPosition blockposition, int j) {
        this.a.getPlayerList().sendAll(new PacketPlayOutWorldEvent(i, blockposition, j, true));
    }

    public void b(int i, BlockPosition blockposition, int j) {
        // Iterator iterator = this.a.getPlayerList().v().iterator(); // Paper
        // CraftBukkit start
        EntityHuman entityhuman = null;
        Entity entity = world.a(i); // PAIL Rename getEntity
        if (entity instanceof EntityHuman) entityhuman = (EntityHuman) entity;
        // CraftBukkit end

        // Paper start
        java.util.List<? extends EntityHuman> list = entity != null ? entity.world.players : this.a.getPlayerList().v();
        Iterator<? extends EntityHuman> iterator = list.iterator();

        PacketPlayOutBlockBreakAnimation packet = null; // SportPaper - cache packet
        while (iterator.hasNext()) {
            EntityHuman human = iterator.next();
            if (!(human instanceof EntityPlayer)) continue;
            EntityPlayer entityplayer = (EntityPlayer) human;
            // Paper end

            if (entityplayer.world == this.world && entityplayer.getId() != i) {
                double d0 = (double) blockposition.getX() - entityplayer.locX;
                double d1 = (double) blockposition.getY() - entityplayer.locY;
                double d2 = (double) blockposition.getZ() - entityplayer.locZ;

                // CraftBukkit start
                if (entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(((EntityPlayer) entityhuman).getBukkitEntity())) {
                    continue;
                }
                // CraftBukkit end

                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                    // SportPaper start
                    if (packet == null) packet = new PacketPlayOutBlockBreakAnimation(i, blockposition, j);
                    entityplayer.playerConnection.sendPacket(packet);
                    // SportPaper end
                }
            }
        }

    }
}
