package me.nate.spigot.knockback;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

/**
 * Server-side implementation of the KnockbackAPIDelegate (someone said ninja trick).
 */
public class KnockbackAPIServerDelegate implements KnockbackAPIDelegate {

    private final KnockbackHandler knockbackHandler;

    public KnockbackAPIServerDelegate(KnockbackHandler knockbackHandler) {
        this.knockbackHandler = knockbackHandler;
    }

    @Override
    public boolean setPlayerProfile(UUID playerUUID, String profileName) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return false;
        }

        KnockbackProfile profile = knockbackHandler.getKnockbackProfileByName(profileName, true);
        if (profile == null) {
            return false;
        }

        knockbackHandler.assignProfileToPlayer(player, profile);
        return true;
    }

    @Override
    public String getActiveProfileName() {
        return knockbackHandler.getActiveProfile().getName();
    }

    @Override
    public Set<String> getAvailableProfileNames() {
        return knockbackHandler.getKnockbackProfiles()
                .stream()
                .map(KnockbackProfile::getName)
                .collect(Collectors.toSet());
    }
}
