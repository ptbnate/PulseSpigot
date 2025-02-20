package xyz.krypton.spigot.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.krypton.spigot.PulseSpigot;
import xyz.krypton.spigot.motion.MotionHandler;
import xyz.krypton.spigot.util.ClickableBuilder;


/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *  
 */

public class MotionCommand extends Command {

    private final MotionHandler motionHandler = PulseSpigot.get().getMotionHandler();
    private final String separator = "§8§m-=-------------------------=-";

    public MotionCommand() {
        super("motion");
        this.description = "Modify Motion values";
        this.usageMessage = "/motion help";
        this.setPermission("pulse.command.motion");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!testPermission(sender)) return true;

        if (motionHandler == null) {
            player.sendMessage("§cMotion handler is not initialized.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info": {
                sendMotionInfo(player);
                return true;
            }

            case "modify": {
                if (args.length != 3) {
                    sendMessage(player, ChatColor.RED + "Usage: /" + label + " modify <property> <value>");
                    return true;
                }

                String property = args[1];
                String value = args[2];

                if (!motionHandler.isValidProperty(property)) {
                    sendMessage(player, ChatColor.RED + "Invalid property. Use /" + label + " info to view all properties.");
                    return true;
                }

                if (!motionHandler.setProperty(property, value)) {
                    sendMessage(player, ChatColor.RED + "Failed to set property. Ensure the value is valid.");
                    return true;
                }

                sendMessage(player, ChatColor.RED + "Successfully set " + ChatColor.WHITE + property + ChatColor.RED + " to " + ChatColor.WHITE + value + ChatColor.RED + ".");
                return true;
            }

            default: {
                sendMessage(player, ChatColor.RED + "Unknown subcommand. Use /" + label + " help for a list of commands.");
                return true;
            }
        }
    }

    private void sendHelp(Player player) {
        sendMessage(player, separator);
        sendMessage(player, ChatColor.RED + "§lMotion Command Help:");
        sendClickableMessage(player, ChatColor.RED + "/motion info", ChatColor.WHITE + "View current motion values.", "/motion info");
        sendClickableMessage(player, ChatColor.RED + "/motion modify <property> <value>", ChatColor.WHITE + "Modify an motion property.", "/motion modify ");
        sendMessage(player, separator);
    }

    private void sendMotionInfo(Player player) {
        sendMessage(player, separator);
        sendMessage(player, ChatColor.RED + "§lPearl Properties:");
        motionHandler.getProperties().forEach((key, value) -> {
            if (key.startsWith("pearl")) {
                TextComponent pearlMessage = new ClickableBuilder(ChatColor.WHITE + key + ChatColor.WHITE + ": " + ChatColor.RED + value)
                        .setClick("/motion modify " + key + " ", ClickEvent.Action.SUGGEST_COMMAND)
                        .setHover("Click to modify this property")
                        .build();
                player.spigot().sendMessage(pearlMessage);
            }
        });

        sendMessage(player, "");
        sendMessage(player, ChatColor.RED + "§lPotion Properties:");
        motionHandler.getProperties().forEach((key, value) -> {
            if (key.startsWith("potion")) {
                TextComponent potionMessage = new ClickableBuilder(ChatColor.WHITE + key + ChatColor.WHITE + ": " + ChatColor.RED + value)
                        .setClick("/motion modify " + key + " ", ClickEvent.Action.SUGGEST_COMMAND)
                        .setHover("Click to modify this property")
                        .build();
                player.spigot().sendMessage(potionMessage);
            }
        });
        sendMessage(player, separator);
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }

    private void sendClickableMessage(Player player, String command, String description, String suggestion) {
        TextComponent message = new ClickableBuilder(command)
                .setClick(suggestion, ClickEvent.Action.SUGGEST_COMMAND)
                .setHover(description)
                .build();
        player.spigot().sendMessage(message);
    }
}
