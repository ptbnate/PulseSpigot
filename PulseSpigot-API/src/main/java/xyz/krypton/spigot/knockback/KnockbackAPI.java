package xyz.krypton.spigot.knockback;

import java.util.Set;
import java.util.UUID;

/**
 * Public API for interacting with the knockback system.
 */
public class KnockbackAPI {

    private static KnockbackAPIDelegate delegate;

    /**
     * Registers the delegate implementation from the server module.
     *
     * @param delegate The delegate to handle API calls.
     */
    public static void registerDelegate(KnockbackAPIDelegate delegate) {
        if (KnockbackAPI.delegate != null) {
            throw new IllegalStateException("Delegate already registered!");
        }
        KnockbackAPI.delegate = delegate;
    }

    /**
     * Gets the active knockback profile name.
     *
     * @return The active profile name.
     */
    public static String getActiveProfileName() {
        if (delegate == null) {
            throw new IllegalStateException("KnockbackAPI delegate is not registered!");
        }
        return delegate.getActiveProfileName();
    }

    /**
     * Retrieves all available knockback profile names.
     *
     * @return A list of profile names.
     */
    public static Set<String> getAvailableProfileNames() {
        if (delegate == null) {
            throw new IllegalStateException("KnockbackAPI delegate is not registered!");
        }
        return delegate.getAvailableProfileNames();
    }

    /**
     * Sets the knockback profile for a specific player.
     *
     * @param playerUUID The UUID of the player.
     * @param profileName The name of the profile to assign.
     * @return True if the profile was successfully set, false otherwise.
     */
    public static boolean setPlayerProfile(UUID playerUUID, String profileName) {
        if (delegate == null) {
            throw new IllegalStateException("KnockbackAPI delegate is not registered!");
        }
        return delegate.setPlayerProfile(playerUUID, profileName);
    }
}