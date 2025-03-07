package me.nate.spigot.command;

import me.nate.spigot.CC;
import me.nate.spigot.PulseSpigot;
import me.nate.spigot.knockback.KnockbackHandler;
import me.nate.spigot.knockback.KnockbackProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import me.nate.spigot.knockback.KnockbackModifier;
import me.nate.spigot.util.ClickableBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class KnockbackCommand extends Command {

    private final String separator = "§8§m-=-------------------------=-";

    private final List<String> SUB_COMMANDS = Arrays.asList(
            "create",
            "delete",
            "implementations",
            "info",
            "viewprojectiles",
            "modify",
            "modifiers",
            "profiles",
            "setactive",
            "setprofile"
    );
    KnockbackHandler knockbackHandler = PulseSpigot.get().getKnockbackHandler();

    public KnockbackCommand() {
        super("knockback");
        this.description = "Modify knockback";
        this.usageMessage = "/knockback help";
        this.setPermission("pulse.command.knockback");
        this.setAliases(Collections.singletonList("kb"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by a player.");
            return true;
        }

        if (knockbackHandler == null) {
            sender.sendMessage("§cThe knockback handler is not initialized.");
            return true;
        }

        if (!testPermission(sender)) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create": {
                if (args.length != 3) {
                    sendMessage(sender, "&cUsage: /" + label + " create <profile> <implementation>");
                    return true;
                }

                String name = args[1];

                if (knockbackHandler.getKnockbackProfileByName(name, true) != null) {
                    sendMessage(sender, "&cA knockback profile with that name already exists!");
                    return true;
                }

                Class<? extends KnockbackProfile> clazz =
                        knockbackHandler.getImplementationTypeMap().get(args[2]);

                if (clazz == null) {
                    sendMessage(sender, "&cThat implementation doesn't exist!\n" +
                            "&7&oUse /knockback implementations to list all possible implementations!");
                    return true;
                }

                try {
                    KnockbackProfile knockbackProfile = clazz.getConstructor(String.class).newInstance(name);

                    knockbackHandler.getKnockbackProfiles().add(knockbackProfile);
                    knockbackHandler.saveKnockbackProfile(knockbackProfile);
                    sendMessage(sender, "&cSuccessfully created knockback profile " + ChatColor.GRAY + name + "&f!");
                } catch (Exception e) {
                    sendMessage(sender, "&cFailed to create knockback profile, likely due to " +
                            "a broken implementation. Check console!");
                    e.printStackTrace();
                }
                return true;
            }

            case "delete": {
                if (args.length != 2) {
                    sendMessage(sender, "&cUsage: /" + label + " delete <profile>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                knockbackHandler.deleteProfile(knockbackProfile);
                sendMessage(sender, CC.red + "Successfully deleted knockback profile " + CC.gray + knockbackProfile.getName() + CC.white + ".");
                return true;
            }

            case "implementations": {
                sendMessage(sender, "&cImplementations: &7" + knockbackHandler.getImplementationTypeMap()
                        .keySet()
                        .stream()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")));
                return true;
            }

            case "info": {
                if (args.length != 2) {
                    sendMessage(sender, "&cUsage: /" + label + " info <profile>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                if (sender instanceof Player) {
                    knockbackCommandView((Player) sender, knockbackProfile);
                }
                return true;
            }

            case "viewprojectiles": {
                if (args.length != 2) {
                    sendMessage(sender, "&cUsage: /" + label + " viewprojectiles <profile>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                if (sender instanceof Player) {
                    displayProjectiles((Player) sender, knockbackProfile);
                }

                return true;
            }

            case "modify": {
                if (args.length != 4) {
                    sendMessage(sender, "&cUsage: /" + label + " modify <profile> <modifier> <value>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                KnockbackModifier<?> knockbackModifier = knockbackProfile.getKnockbackModifier(args[2], true);

                if (knockbackModifier == null) {
                    sendMessage(sender, "&cA knockback modifier with that name doesn't exist!");
                    return true;
                }

                if (knockbackModifier.getType() == Boolean.class || knockbackModifier.getType() == boolean.class) {
                    if (!isBoolean(args[3])) {
                        sendMessage(sender, "&cThat is not a valid value! Make sure the value is a **boolean**.");
                        return true;
                    }

                    knockbackProfile.modify(args[2], Boolean.parseBoolean(args[3]));
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                    return true;
                } else if (knockbackModifier.getType() == Double.class || knockbackModifier.getType() == double.class) {
                    if (!isDouble(args[3])) {
                        sendMessage(sender, "&cThat is not a valid value! Make sure the value is a **double**.");
                        return true;
                    }

                    knockbackProfile.modify(args[2], Double.parseDouble(args[3]));
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                    return true;
                } else if (knockbackModifier.getType() == Integer.class || knockbackModifier.getType() == int.class) {
                    if (!isInteger(args[3])) {
                        sendMessage(sender, "&cThat is not a valid value! Make sure the value is a **int**.");
                        return true;
                    }

                    knockbackProfile.modify(args[2], Integer.parseInt(args[3]));
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                    return true;
                } else if (knockbackModifier.getType() == Long.class || knockbackModifier.getType() == long.class) {
                    if (!isLong(args[3])) {
                        sendMessage(sender, "&cThat is not a valid value! Make sure the value is a **long**.");
                        return true;
                    }

                    knockbackProfile.modify(args[2], Long.parseLong(args[3]));
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                    return true;

                } else if (knockbackModifier.getType() == Float.class || knockbackModifier.getType() == float.class) {
                    if (!isFloat(args[3])) {
                        sendMessage(sender, "&cThat is not a valid value! Make sure the value is a **float**.");
                        return true;
                    }

                    knockbackProfile.modify(args[2], Float.parseFloat(args[3]));
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                    return true;
                }

                try {
                    knockbackProfile.modify(args[2], args[3]);
                    knockbackModifier.writeToConfig(knockbackProfile, PulseSpigot.get().getKnockbackConfig(), true);
                    sendMessage(sender, "&cSuccessfully updated modifier &7" + knockbackModifier.getLabel() + " &cto " + args[3] + ".");
                } catch (Exception e) {
                    sendMessage(sender, "&cFailed to update knockback modifier due to the value not being able to be parsed.");
                }
                return true;
            }

            case "modifiers": {
                if (args.length != 2) {
                    sendMessage(sender, "&cUsage: /" + label + " modifiers <profile>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                sendMessage(sender, "&cModifiers: &7" + knockbackProfile.getModifiers()
                        .stream()
                        .map(KnockbackModifier::getLabel)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")));
                return true;
            }

            case "profiles": {
                sendMessage(sender, "&cKnockback Profiles: &7" + knockbackHandler.getKnockbackProfiles()
                        .stream()
                        .map(profile -> knockbackHandler.getActiveProfile() == profile ?
                                profile.getName() + " &a[active]&7" : profile.getName())
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")));
                return true;
            }

            case "setactive": {
                if (args.length != 2) {
                    sendMessage(sender, "&cUsage: /" + label + " setactive <profile>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                if (knockbackHandler.getActiveProfile() == knockbackProfile) {
                    sendMessage(sender, "&cThat knockback profile is already active!");
                    return true;
                }

                knockbackHandler.setActiveProfile(knockbackProfile);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                    if (onlinePlayer instanceof CraftPlayer)
                        ((CraftPlayer) onlinePlayer).getHandle().knockbackProfile = knockbackProfile;

                sendMessage(sender, "&cSuccessfully set &7" + knockbackProfile.getName() + "&c to the &ccurrent knockback profile.");
                return true;
            }

            case "setprofile": {
                if (args.length != 3) {
                    sendMessage(sender, "&cUsage: /" + label + " setprofile <profile> <player>");
                    return true;
                }

                KnockbackProfile knockbackProfile = knockbackHandler.getKnockbackProfileByName(args[1], true);

                if (knockbackProfile == null) {
                    sendMessage(sender, "&cA knockback profile with that name doesn't exist!");
                    return true;
                }

                Player player = Bukkit.getPlayer(args[2]);

                if (player == null) {
                    sendMessage(sender, "&cCouldn't find an online player matching the username " + args[2]);
                    return true;
                }

                if (((CraftPlayer) player).getHandle().knockbackProfile == knockbackProfile) {
                    sendMessage(sender, "&cThat profile is already active for player " + player.getName());
                    return true;
                }

                ((CraftPlayer) player).getHandle().knockbackProfile = knockbackProfile;
                sendMessage(sender, "&cSuccessfully set &7" + player.getName() + "'s &cknockback profile.");
                return true;
            }

            default: {
                sendHelp(sender);
                return true;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length > 0
                && SUB_COMMANDS.contains(args[0].toLowerCase())) {
            if (args.length == 2) {
                return knockbackHandler.getKnockbackProfiles()
                        .stream()
                        .sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()))
                        .map(KnockbackProfile::getName)
                        .collect(Collectors.toList());
            }
        } else {
            return SUB_COMMANDS;
        }

        return super.tabComplete(sender, alias, args);
    }

    private void sendHelp(CommandSender player) {
        player.sendMessage(separator + "\n" + "§c§lKnockback profile list:\n");

        player.sendMessage(separator);
        player.sendMessage("§c/knockback create <name> <implementation> | §7Create a profile.");
        clickable((Player) player, "", "§c/knockback create <name> <implementation> | §7Create a profile.", "/kb create");
        player.sendMessage("§c/knockback delete <name> | §7Delete a profile.");
        player.sendMessage("§c/knockback profiles | §7Displays all the available knockback profiles.");
        player.sendMessage("§c/knockback setactive <name> | §7Load a profile.");
        player.sendMessage("§c/knockback implementations | §7Displays all the knockback implementations.");
        player.sendMessage("§c/knockback info <name> | §7View a profile.");
        player.sendMessage("§c/knockback viewprojectiles <name> | §7View a profile's projectiles.");
        player.sendMessage("§c/knockback modifiers <name> | §7Displays all the knockback values of a profile.");
        player.sendMessage("§c/knockback setprofile <profile> <player> | §7Set a profile to a player.");
        player.sendMessage("§c/knockback modify <profile> <modifier> <value> | §7Modify a knockback value.");
        player.sendMessage(separator);
    }


    private void knockbackCommandView(Player player, KnockbackProfile profile) {
        player.sendMessage(separator + "\n" + "§c§lProfile: §7" + profile.getName() + "\n");
        KnockbackModifier noDamageTicksModifier = profile.getModifiers().stream()
                .filter(modifier -> modifier.getLabel().equalsIgnoreCase("no-damage-ticks"))
                .findFirst()
                .orElse(null);

        profile.getModifiers().stream()
                .filter(modifier -> !modifier.getLabel().equalsIgnoreCase("no-damage-ticks") && !isProjectile(modifier.getLabel()))
                .forEach(modifier -> clickable(player,
                        "• " + modifier.getLabel() + ": §c" + modifier.getValue(),
                        "§7Click to edit " + modifier.getLabel(),
                        "/kb modify " + profile.getName() + " " + modifier.getLabel().toLowerCase().replace(" ", "-") + " " + modifier.getValue()));

        if (noDamageTicksModifier != null) {
            player.sendMessage("\n§c§lDamage-Ticks:");
            clickable(player,
                    "• " + noDamageTicksModifier.getLabel() + ": §c" + noDamageTicksModifier.getValue(),
                    "§7Click to edit " + noDamageTicksModifier.getLabel(),
                    "/kb modify " + profile.getName() + " " + noDamageTicksModifier.getLabel().toLowerCase().replace(" ", "-") + " " + noDamageTicksModifier.getValue());
        }
        player.sendMessage("");
        clickableRun(player,
                "§7 [§c§lProjectiles§7]",
                "§7Click to view or edit projectile modifiers",
                "/kb viewprojectiles " + profile.getName());

        player.sendMessage(separator);
    }

    private void displayProjectiles(Player player, KnockbackProfile profile) {
        player.sendMessage("§c§lProjectiles:");
        profile.getModifiers().stream()
                .filter(modifier -> isProjectile(modifier.getLabel()))
                .forEach(modifier -> clickable(player,
                        modifier.getLabel() + ": §c" + modifier.getValue(),
                        "§7Click to edit " + modifier.getLabel(),
                        "/kb modify " + profile.getName() + " " + modifier.getLabel().toLowerCase().replace(" ", "-") + " " + modifier.getValue()));
    }

    private boolean isProjectile(String label) {
        return label.matches("(arrow|egg|pearl|snowball|rod)-(horizontal|vertical|speed)");
    }

    private void clickable(Player player, String message, String hoverText, String command) {
        ClickableBuilder clickableBuilder = new ClickableBuilder(message)
                .setHover(hoverText)
                .setClick(command, ClickEvent.Action.SUGGEST_COMMAND);

        player.spigot().sendMessage(clickableBuilder.build());
    }

    private void clickableRun(Player player, String message, String hoverText, String command) {
        ClickableBuilder clickableBuilder = new ClickableBuilder(message)
                .setHover(hoverText)
                .setClick(command, ClickEvent.Action.RUN_COMMAND);

        player.spigot().sendMessage(clickableBuilder.build());
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(translate(message));
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    private boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isBoolean(String s) {
        try {
            Boolean.parseBoolean(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String translate(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
