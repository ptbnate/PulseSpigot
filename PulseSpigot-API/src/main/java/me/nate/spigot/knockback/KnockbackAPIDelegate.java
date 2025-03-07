package me.nate.spigot.knockback;

import java.util.Set;
import java.util.UUID;

/**
 * Ninja trick for server side implementation.
 */
public interface KnockbackAPIDelegate {

    boolean setPlayerProfile(UUID playerUUID, String profileName);

    String getActiveProfileName();

    Set<String> getAvailableProfileNames();
}
