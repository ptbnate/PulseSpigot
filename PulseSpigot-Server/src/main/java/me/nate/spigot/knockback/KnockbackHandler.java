/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

package me.nate.spigot.knockback;

import me.nate.spigot.knockback.impl.ClassicKnockbackProfile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import me.nate.spigot.PulseSpigot;
import me.nate.spigot.knockback.impl.NormalKnockbackProfile;
import me.nate.spigot.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KnockbackHandler {

    private final Map<String, Class<? extends KnockbackProfile>> implementationTypeMap = new ConcurrentHashMap<>();
    private final Set<KnockbackProfile> knockbackProfiles = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private KnockbackProfile activeProfile;

    public KnockbackHandler() {
        implementationTypeMap.put("classic", ClassicKnockbackProfile.class);
        implementationTypeMap.put("normal", NormalKnockbackProfile.class);
        initialize();
    }

    private void initialize() {
        YamlConfiguration config = PulseSpigot.get().getKnockbackConfig().getConfig();

        if (config.getConfigurationSection("profiles") != null) {
            config.getConfigurationSection("profiles").getKeys(false).forEach(profile -> {
                String implementation = config.getString("profiles." + profile + ".implementation");

                if (!implementationTypeMap.containsKey(implementation)) {
                    Bukkit.getLogger().severe("Knockback Profile " + profile + " with implementation " +
                            implementation + " couldn't be loaded due to the implementation not being found!");
                    config.set("profiles." + profile, null);
                    PulseSpigot.get().getKnockbackConfig().save();
                    return;
                }

                Class<? extends KnockbackProfile> clazz = implementationTypeMap.get(implementation);
                KnockbackProfile knockbackProfile;

                try {
                    knockbackProfile = clazz.getConstructor(String.class).newInstance(profile);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Knockback Profile " + profile + " with implementation " + implementation
                            + " couldn't be loaded due to the constructor of the implementation not being found!");
                    e.printStackTrace();
                    config.set("profiles." + profile, null);
                    PulseSpigot.get().getKnockbackConfig().save();
                    return;
                }

                config.getConfigurationSection("profiles." + profile + ".modifiers").getKeys(false)
                        .forEach(modifier ->
                                knockbackProfile.modify(modifier, config.get("profiles." + profile + ".modifiers." + modifier)));

                knockbackProfiles.add(knockbackProfile);
            });
        }

        if (!config.contains("active-profile")) {
            config.set("active-profile", activeProfile == null ?
                    (knockbackProfiles.isEmpty() ?
                            createDefault() : new ArrayList<>(knockbackProfiles).get(0)).getName()
                    : activeProfile.getName());
            PulseSpigot.get().getKnockbackConfig().save();
        }

        activeProfile = getKnockbackProfileByName(config.getString("active-profile"), false);

        if (activeProfile == null && !knockbackProfiles.isEmpty()) {
            activeProfile = new ArrayList<>(knockbackProfiles).get(0);
        }

        if (activeProfile != null) {
            saveKnockbackProfile(activeProfile);
        } else {
            LogUtil.logPulse("No active profile found, and no knockback profiles are available!");
        }
    }

    public void deleteProfile(KnockbackProfile knockbackProfile) {
        String profileName = knockbackProfile.getName();
        PulseSpigot.get().getKnockbackConfig().getConfig().set("profiles." + profileName, null);
        knockbackProfiles.remove(knockbackProfile);
        PulseSpigot.get().getKnockbackConfig().saveAsync();
    }

    private KnockbackProfile createDefault() {
        KnockbackProfile knockbackProfile;
        knockbackProfiles.add((knockbackProfile = new NormalKnockbackProfile("default")));

        return knockbackProfile;
    }

    public KnockbackProfile getKnockbackProfileByName(String name, boolean ignoreCase) {
        if (ignoreCase) {
            return knockbackProfiles.stream()
                    .filter(profile -> profile.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        } else {
            return knockbackProfiles.stream()
                    .filter(profile -> profile.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    public void saveKnockbackProfile(KnockbackProfile knockbackProfile) {
        if (knockbackProfile != null) {
            PulseSpigot.get().getKnockbackConfig().getConfig().set("profiles." + knockbackProfile.getName() + ".implementation",
                    knockbackProfile.getImplementationName());
            knockbackProfile.getModifiers().forEach(modifier -> modifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), false));
            PulseSpigot.get().getKnockbackConfig().saveAsync();
        } else {
            Bukkit.getLogger().severe("Cannot save knockback profile: profile is null!");
        }
    }

    public KnockbackProfile getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(KnockbackProfile activeProfile) {
        this.activeProfile = activeProfile;

        PulseSpigot.get().getKnockbackConfig().getConfig().set("active-profile", activeProfile.getName());
        PulseSpigot.get().getKnockbackConfig().saveAsync();
    }

    public void assignProfileToPlayer(Player player, KnockbackProfile profile) {
        if (((CraftPlayer) player).getHandle().knockbackProfile == profile) {
            Bukkit.getLogger().info("That profile is already active for player " + player.getName());
            return;
        }

        ((CraftPlayer) player).getHandle().knockbackProfile = profile;
        Bukkit.getLogger().info("Successfully set " + player.getName() + "'s knockback profile to " + profile.getName());
    }

    public Set<KnockbackProfile> getKnockbackProfiles() {
        return knockbackProfiles;
    }

    public Map<String, Class<? extends KnockbackProfile>> getImplementationTypeMap() {
        return implementationTypeMap;
    }
}
