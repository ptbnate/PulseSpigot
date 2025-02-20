package org.bukkit;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.util.function.Predicate;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import static org.bukkit.util.NumberConversions.checkFinite;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents a 3-dimensional position in a world
 */
public class Location implements Cloneable, ConfigurationSerializable {
    private Reference<World> world; // PulseSpigot - use reference to prevent memory leak
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    /**
     * Constructs a new Location with the given coordinates
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     */
    public Location(@UnknownNullability final World world, final double x, final double y, final double z) { // PulseSpigot
        this(world, x, y, z, 0, 0);
    }

    /**
     * Constructs a new Location with the given coordinates and direction
     *
     * @param world The world in which this location resides
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     * @param yaw The absolute rotation on the x-plane, in degrees
     * @param pitch The absolute rotation on the y-plane, in degrees
     */
    public Location(@UnknownNullability final World world, final double x, final double y, final double z, final float yaw, final float pitch) { // PulseSpigot
        this.world = this.wrapWorld(world); // PulseSpigot - use reference to prevent memory leak
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    // PulseSpigot start - fast clone
    private Location(@NotNull Reference<World> world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
    // PulseSpigot end

    /**
     * Sets the world that this location resides in
     *
     * @param world New world that this location resides in
     */
    // PulseSpigot start - use reference to prevent memory leak
    public void setWorld(@Nullable World world) {
        this.world = this.wrapWorld(world);
    // PulseSpigot end
    }

    /**
     * Gets the world that this location resides in
     *
     * @return World that contains this location
     */
    // PulseSpigot start - use reference to prevent memory leak
    public @Nullable World getWorld() {
        if (this.world == null) {
            return null;
        }
        return this.world.get();
    }

    /**
     * Gets the world that this location resides in, or null if the world is unloaded
     *
     * @return World that contains this location
     */
    public @UnknownNullability World getWorldSafe() {
        if (this.world == null) {
            return null;
        }
        World world = this.world.get();
        Validate.notNull(world, "World unloaded");
        return world;
    }
    // PulseSpigot end

    // PulseSpigot start
    /**
     * Checks if world in this location is present and loaded.
     *
     * @return true if is loaded, otherwise false
     */
    public boolean isWorldLoaded() {
        if (this.world == null) {
            return false;
        }

        World world = this.world.get();
        return world != null && Bukkit.getWorld(world.getUID()) != null;
    }
    // PulseSpigot end

    /**
     * Gets the chunk at the represented location
     *
     * @return Chunk at the represented location
     */
    public @NotNull Chunk getChunk() { // PulseSpigot
        return this.getWorldSafe().getChunkAt(this); // PulseSpigot - use reference to prevent memory leak
    }

    /**
     * Gets the block at the represented location
     *
     * @return Block at the represented location
     */
    public @NotNull Block getBlock() { // PulseSpigot
        return this.getWorldSafe().getBlockAt(this); // PulseSpigot - use reference to prevent memory leak
    }

    /**
     * Sets the x-coordinate of this location
     *
     * @param x X-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the x-coordinate of this location
     *
     * @return x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the floored value of the X component, indicating the block that
     * this location is contained with.
     *
     * @return block X
     */
    public int getBlockX() {
        return locToBlock(x);
    }

    // PulseSpigot start
    /**
     * Gets the chunk's X component this location is.
     */
    public int getChunkX() {
        return this.getBlockX() >> 4;
    }
    // PulseSpigot end

    /**
     * Sets the y-coordinate of this location
     *
     * @param y y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the y-coordinate of this location
     *
     * @return y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the floored value of the Y component, indicating the block that
     * this location is contained with.
     *
     * @return block y
     */
    public int getBlockY() {
        return locToBlock(y);
    }

    /**
     * Sets the z-coordinate of this location
     *
     * @param z z-coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Gets the z-coordinate of this location
     *
     * @return z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the floored value of the Z component, indicating the block that
     * this location is contained with.
     *
     * @return block z
     */
    public int getBlockZ() {
        return locToBlock(z);
    }

    // PulseSpigot start
    /**
     * Gets the chunk's Z component this location is.
     */
    public int getChunkZ() {
        return this.getBlockZ() >> 4;
    }
    // PulseSpigot end

    /**
     * Sets the yaw of this location, measured in degrees.
     * <ul>
     * <li>A yaw of 0 or 360 represents the positive z direction.
     * <li>A yaw of 180 represents the negative z direction.
     * <li>A yaw of 90 represents the negative x direction.
     * <li>A yaw of 270 represents the positive x direction.
     * </ul>
     * Increasing yaw values are the equivalent of turning to your
     * right-facing, increasing the scale of the next respective axis, and
     * decreasing the scale of the previous axis.
     *
     * @param yaw new rotation's yaw
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * Gets the yaw of this location, measured in degrees.
     * <ul>
     * <li>A yaw of 0 or 360 represents the positive z direction.
     * <li>A yaw of 180 represents the negative z direction.
     * <li>A yaw of 90 represents the negative x direction.
     * <li>A yaw of 270 represents the positive x direction.
     * </ul>
     * Increasing yaw values are the equivalent of turning to your
     * right-facing, increasing the scale of the next respective axis, and
     * decreasing the scale of the previous axis.
     *
     * @return the rotation's yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Sets the pitch of this location, measured in degrees.
     * <ul>
     * <li>A pitch of 0 represents level forward facing.
     * <li>A pitch of 90 represents downward facing, or negative y
     *     direction.
     * <li>A pitch of -90 represents upward facing, or positive y direction.
     * </ul>
     * Increasing pitch values the equivalent of looking down.
     *
     * @param pitch new incline's pitch
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Gets the pitch of this location, measured in degrees.
     * <ul>
     * <li>A pitch of 0 represents level forward facing.
     * <li>A pitch of 90 represents downward facing, or negative y
     *     direction.
     * <li>A pitch of -90 represents upward facing, or positive y direction.
     * </ul>
     * Increasing pitch values the equivalent of looking down.
     *
     * @return the incline's pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Gets a unit-vector pointing in the direction that this Location is
     * facing.
     *
     * @return a vector pointing the direction of this location's {@link
     *     #getPitch() pitch} and {@link #getYaw() yaw}
     */
    public @NotNull Vector getDirection() { // PulseSpigot
        Vector vector = new Vector();

        double rotX = this.getYaw();
        double rotY = this.getPitch();

        vector.setY(-Math.sin(Math.toRadians(rotY)));

        double xz = Math.cos(Math.toRadians(rotY));

        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));

        return vector;
    }

    /**
     * Sets the {@link #getYaw() yaw} and {@link #getPitch() pitch} to point
     * in the direction of the vector.
     *
     * @param vector the direction vector
     * @return the same location
     */
    public @NotNull Location setDirection(@NotNull Vector vector) { // PulseSpigot
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();

        if (x == 0 && z == 0) {
            pitch = vector.getY() > 0 ? -90 : 90;
            return this;
        }

        double theta = Math.atan2(-x, z);
        yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);

        double x2 = NumberConversions.square(x);
        double z2 = NumberConversions.square(z);
        double xz = Math.sqrt(x2 + z2);
        pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));

        return this;
    }

    /**
     * Adds the location by another.
     *
     * @see Vector
     * @param vec The other location
     * @return the same location
     * @throws IllegalArgumentException for differing worlds
     */
    public @NotNull Location add(@NotNull Location vec) { // PulseSpigot
        if (vec == null || vec.getWorldSafe() != getWorldSafe()) { // PulseSpigot - use reference to prevent memory leak
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }

        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    /**
     * Adds the location by a vector.
     *
     * @see Vector
     * @param vec Vector to use
     * @return the same location
     */
    public @NotNull Location add(@NotNull Vector vec) { // PulseSpigot
        this.x += vec.getX();
        this.y += vec.getY();
        this.z += vec.getZ();
        return this;
    }

    /**
     * Adds the location by another. Not world-aware.
     *
     * @see Vector
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return the same location
     */
    public @NotNull Location add(double x, double y, double z) { // PulseSpigot
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Subtracts the location by another.
     *
     * @see Vector
     * @param vec The other location
     * @return the same location
     * @throws IllegalArgumentException for differing worlds
     */
    public @NotNull Location subtract(@NotNull Location vec) { // PulseSpigot
        if (vec == null || vec.getWorldSafe() != getWorldSafe()) { // PulseSpigot - use reference to prevent memory leak
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }

        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    /**
     * Subtracts the location by a vector.
     *
     * @see Vector
     * @param vec The vector to use
     * @return the same location
     */
    public @NotNull Location subtract(@NotNull Vector vec) { // PulseSpigot
        this.x -= vec.getX();
        this.y -= vec.getY();
        this.z -= vec.getZ();
        return this;
    }

    /**
     * Subtracts the location by another. Not world-aware and
     * orientation independent.
     *
     * @see Vector
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return the same location
     */
    public @NotNull Location subtract(double x, double y, double z) { // PulseSpigot
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Gets the magnitude of the location, defined as sqrt(x^2+y^2+z^2). The
     * value of this method is not cached and uses a costly square-root
     * function, so do not repeatedly call this method to get the location's
     * magnitude. NaN will be returned if the inner result of the sqrt()
     * function overflows, which will be caused if the length is too long. Not
     * world-aware and orientation independent.
     *
     * @see Vector
     * @return the magnitude
     */
    public double length() {
        return Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));
    }

    /**
     * Gets the magnitude of the location squared. Not world-aware and
     * orientation independent.
     *
     * @see Vector
     * @return the magnitude
     */
    public double lengthSquared() {
        return NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z);
    }

    /**
     * Get the distance between this location and another. The value of this
     * method is not cached and uses a costly square-root function, so do not
     * repeatedly call this method to get the location's magnitude. NaN will
     * be returned if the inner result of the sqrt() function overflows, which
     * will be caused if the distance is too long.
     *
     * @see Vector
     * @param o The other location
     * @return the distance
     * @throws IllegalArgumentException for differing worlds
     */
    public double distance(@NotNull Location o) { // PulseSpigot
        return Math.sqrt(distanceSquared(o));
    }

    /**
     * Get the squared distance between this location and another.
     *
     * @see Vector
     * @param o The other location
     * @return the distance
     * @throws IllegalArgumentException for differing worlds
     */
    public double distanceSquared(@NotNull Location o) { // PulseSpigot
        if (o == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null location");
        // PulseSpigot start - use reference to prevent memory leak
        }
        World world = this.getWorldSafe();
        World otherWorld = o.getWorldSafe();
        if (otherWorld == null || world == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null world");
        } else if (world != otherWorld) {
            throw new IllegalArgumentException("Cannot measure distance between " + world.getName() + " and " + otherWorld.getName());
        }
        // PulseSpigot end

        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y) + NumberConversions.square(z - o.z);
    }

    /**
     * Performs scalar multiplication, multiplying all components with a
     * scalar. Not world-aware.
     *
     * @param m The factor
     * @see Vector
     * @return the same location
     */
    public @NotNull Location multiply(double m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    /**
     * Zero this location's components. Not world-aware.
     *
     * @see Vector
     * @return the same location
     */
    public @NotNull Location zero() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    // Paper start
    /**
     * Sets the position of this Location and returns itself
     *
     * This mutates this object, clone first.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self (not cloned)
     */
    public @NotNull Location set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Takes the x/y/z from base and adds the specified x/y/z to it and returns self
     *
     * This mutates this object, clone first.
     * @param base The base coordinate to modify
     * @param x X coordinate to add to base
     * @param y Y coordinate to add to base
     * @param z Z coordinate to add to base
     * @return self (not cloned)
     */
    public @NotNull Location add(@NotNull Location base, double x, double y, double z) {
        return this.set(base.x + x, base.y + y, base.z + z);
    }

    /**
     * Takes the x/y/z from base and subtracts the specified x/y/z to it and returns self
     *
     * This mutates this object, clone first.
     * @param base The base coordinate to modify
     * @param x X coordinate to subtract from base
     * @param y Y coordinate to subtract from base
     * @param z Z coordinate to subtract from base
     * @return self (not cloned)
     */
    public @NotNull Location subtract(@NotNull Location base, double x, double y, double z) {
        return this.set(base.x - x, base.y - y, base.z - z);
    }

    /**
     * @return A new location where X/Y/Z are on the Block location (integer value of X/Y/Z)
     */
    public @NotNull Location toBlockLocation() {
        Location blockLoc = this.clone();
        blockLoc.setX(this.getBlockX());
        blockLoc.setY(this.getBlockY());
        blockLoc.setZ(this.getBlockZ());
        return blockLoc;
    }

    /**
     * @return A new location where X/Y/Z are the center of the block
     */
    public @NotNull Location toCenterLocation() {
        Location centerLoc = clone();
        centerLoc.setX(getBlockX() + 0.5);
        centerLoc.setY(getBlockY() + 0.5);
        centerLoc.setZ(getBlockZ() + 0.5);
        return centerLoc;
    }

    /**
     * Returns a copy of this location except with y = getWorld().getHighestBlockYAt(this.getBlockX(), this.getBlockZ())
     * @return A copy of this location except with y = getWorld().getHighestBlockYAt(this.getBlockX(), this.getBlockZ())
     * @throws NullPointerException if {{@link #getWorld()}} is {@code null}
     */
    public @NotNull Location toHighestLocation() {
        Location ret = this.clone();
        ret.setY(this.getWorldSafe().getHighestBlockYAt(this));
        return ret;
    }

    /**
     * Returns a list of entities within a bounding box centered around a Location.
     *
     * Some implementations may impose artificial restrictions on the size of the search bounding box.
     *
     * @param x 1/2 the size of the box along x axis
     * @param y 1/2 the size of the box along y axis
     * @param z 1/2 the size of the box along z axis
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Entity> getNearbyEntities(double x, double y, double z) {
        World world = this.getWorldSafe();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world.getNearbyEntities(this, x, y, z);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param radius X Radius
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double radius) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, radius, radius, radius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param radius Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double radius, @Nullable Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius, @Nullable Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z radius
     * @param predicate a predicate used to filter results
     * @return the collection of living entities near location. This will always be a non-null collection.
     */
    public @NotNull Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius, @Nullable Predicate<LivingEntity> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param radius X/Y/Z Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double radius) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, radius, radius, radius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, xzRadius, yRadius, xzRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, xRadius, yRadius, zRadius);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param radius X/Y/Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double radius, @Nullable Predicate<Player> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, radius, radius, radius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xzRadius X/Z Radius
     * @param yRadius Y Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double xzRadius, double yRadius, @Nullable Predicate<Player> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets nearby players within the specified radius (bounding box)
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param predicate a predicate used to filter results
     * @return the collection of players near location. This will always be a non-null collection.
     */
    public @NotNull Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius, @Nullable Predicate<Player> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, xRadius, yRadius, zRadius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param radius X/Y/Z radius to search within
     * @param <T> the entity type
     * @return the collection of entities of type clazz near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends T> clazz, double radius) {
        return getNearbyEntitiesByType(clazz, radius, radius, radius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends T> clazz, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends T> clazz, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(clazz, xRadius, yRadius, zRadius, null);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param radius X/Y/Z radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends T> clazz, double radius, @Nullable Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, radius, radius, radius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius, with x and x radius matching (bounding box)
     * @param clazz Type to filter by
     * @param xzRadius X/Z radius to search within
     * @param yRadius Y radius to search within
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends T> clazz, double xzRadius, double yRadius, @Nullable Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, predicate);
    }

    /**
     * Gets all nearby entities of the specified type, within the specified radius (bounding box)
     * @param clazz Type to filter by
     * @param xRadius X Radius
     * @param yRadius Y Radius
     * @param zRadius Z Radius
     * @param predicate a predicate used to filter results
     * @param <T> the entity type
     * @return the collection of entities near location. This will always be a non-null collection.
     */
    public <T extends Entity> @NotNull Collection<T> getNearbyEntitiesByType(@Nullable Class<? extends Entity> clazz, double xRadius, double yRadius, double zRadius, @Nullable Predicate<T> predicate) {
        World world = this.getWorldSafe();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world.getNearbyEntitiesByType(clazz, this, xRadius, yRadius, zRadius, predicate);
    }
    // Paper end

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;

        // PulseSpigot start - use reference to prevent memory leak
        World world = this.getWorld();
        World otherWorld = this.getWorld();
        if (!Objects.equals(world, otherWorld)) {
        // PulseSpigot end
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(other.pitch)) {
            return false;
        }
        if (Float.floatToIntBits(this.yaw) != Float.floatToIntBits(other.yaw)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        // PulseSpigot start - use reference to prevent memory leak
        World world = this.getWorld();
        hash = 19 * hash + (world != null ? world.hashCode() : 0);
        // PulseSpigot end
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        hash = 19 * hash + Float.floatToIntBits(this.pitch);
        hash = 19 * hash + Float.floatToIntBits(this.yaw);
        return hash;
    }

    @Override
    public String toString() {
        return "Location{" + "world=" + this.getWorld() + ",x=" + x + ",y=" + y + ",z=" + z + ",pitch=" + pitch + ",yaw=" + yaw + '}'; // PulseSpigot - use reference to prevent memory leak
    }

    /**
     * Constructs a new {@link Vector} based on this Location
     *
     * @return New Vector containing the coordinates represented by this
     *     Location
     */
    public @NotNull Vector toVector() { // PulseSpigot
        return new Vector(x, y, z);
    }

    @Override
    public @NotNull Location clone() { // PulseSpigot
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    // PulseSpigot start - fast clone
    /**
     * Constructs a new {@link Location} based on this Location.
     * Should be faster than {@link #clone()}.
     *
     * @return New Location containing the data represented by this Location
     */
    public @NotNull Location fastClone() {
        return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
    }
    // PulseSpigot end

    // PulseSpigot start - use reference to prevent memory leak
    @Contract("null -> null")
    private Reference<World> wrapWorld(@Nullable World world) {
        return world == null ? null : new WeakReference<>(world);
    }
    // PulseSpigot end

    /**
     * Safely converts a double (location coordinate) to an int (block
     * coordinate)
     *
     * @param loc Precise coordinate
     * @return Block coordinate
     */
    public static int locToBlock(double loc) {
        return NumberConversions.floor(loc);
    }

	@Utility
	public @NotNull Map<String, Object> serialize() { // PulseSpigot
		Map<String, Object> data = new HashMap<String, Object>();

        // PulseSpigot start - use reference to prevent memory leak
        if (this.world != null) {
            data.put("world", getWorld().getName());
        }
        // PulseSpigot end

		data.put("x", this.x);
		data.put("y", this.y);
		data.put("z", this.z);

		data.put("yaw", this.yaw);
		data.put("pitch", this.pitch);

		return data;
	}
	
	 /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized location
     * @throws IllegalArgumentException if the world don't exists
     * @see ConfigurationSerializable
     */
	public static @NotNull Location deserialize(Map<String, Object> args) { // PulseSpigot
		World world = Bukkit.getWorld((String) args.get("world"));
		if (world == null) {
			throw new IllegalArgumentException("unknown world");
		}

		return new Location(world, NumberConversions.toDouble(args.get("x")), NumberConversions.toDouble(args.get("y")), NumberConversions.toDouble(args.get("z")), NumberConversions.toFloat(args.get("yaw")), NumberConversions.toFloat(args.get("pitch")));
	}
}
