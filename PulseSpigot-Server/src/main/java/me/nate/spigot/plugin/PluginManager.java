package me.nate.spigot.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import me.nate.spigot.CC;
/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class PluginManager {

    private final org.bukkit.plugin.PluginManager pluginManager;
    private final String separator = "§8§m-=-------------------------=-";

    public PluginManager() {
        this.pluginManager = Bukkit.getPluginManager();
    }

    public void listPlugins(CommandSender sender, int page) {
        Plugin[] plugins = pluginManager.getPlugins();
        int totalPlugins = plugins.length;
        int totalPages = (int) Math.ceil(totalPlugins / 10.0);

        if (page > totalPages || page <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid page number! Valid range is 1-" + totalPages);
            return;
        }

        sender.sendMessage("");
        sender.sendMessage(CC.translate(separator));
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Plugin List (Page " + page + " of " + totalPages + "):");

        int start = (page - 1) * 10;
        int end = Math.min(start + 10, totalPlugins);

        for (int i = start; i < end; i++) {
            Plugin plugin = plugins[i];
            String status = plugin.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
            sender.sendMessage(ChatColor.RED + plugin.getName() + ": " + status);
        }

        sender.sendMessage(CC.translate(separator));
        sender.sendMessage("");
    }

    public void enablePlugin(CommandSender sender, String pluginName) {
        Plugin pluginToEnable = pluginManager.getPlugin(pluginName);
        if (pluginToEnable == null) {
            sender.sendMessage(ChatColor.RED + "Plugin not found: " + pluginName);
            return;
        }
        if (pluginToEnable.isEnabled()) {
            sender.sendMessage(ChatColor.RED + pluginToEnable.getName() + " is already enabled.");
            return;
        }
        pluginManager.enablePlugin(pluginToEnable);
        sender.sendMessage(ChatColor.GREEN + "Enabled plugin: " + pluginToEnable.getName());
    }

    public void disablePlugin(CommandSender sender, String pluginName) {
        Plugin pluginToDisable = pluginManager.getPlugin(pluginName);
        if (pluginToDisable == null) {
            sender.sendMessage(ChatColor.RED + "Plugin not found: " + pluginName);
            return;
        }
        if (!pluginToDisable.isEnabled()) {
            sender.sendMessage(ChatColor.RED + pluginToDisable.getName() + " is already disabled.");
            return;
        }
        pluginManager.disablePlugin(pluginToDisable);
        sender.sendMessage(ChatColor.RED + "Disabled plugin: " + pluginToDisable.getName());
    }

    public void reloadPlugin(CommandSender sender, String pluginName) {
        long startTime = System.currentTimeMillis();

        Plugin pluginToReload = pluginManager.getPlugin(pluginName);
        if (pluginToReload == null) {
            sender.sendMessage(ChatColor.RED + "Plugin not found: " + pluginName);
            return;
        }

        if (pluginToReload.isEnabled()) {
            pluginManager.disablePlugin(pluginToReload);
            sender.sendMessage(ChatColor.RED + "Disabled plugin: " + pluginToReload.getName());
        }

        pluginManager.enablePlugin(pluginToReload);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        sender.sendMessage(ChatColor.GRAY + "Reloaded " + ChatColor.GREEN + pluginToReload.getName() + ChatColor.YELLOW + " in " + duration + " ms.");
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(CC.translate(separator));
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Plugin Manager Help:");
        sender.sendMessage(ChatColor.RED + "/plugin list" + ChatColor.WHITE + " - List all plugins with their status.");
        sender.sendMessage(ChatColor.RED + "/plugin enable <pluginName>" + ChatColor.WHITE + " - Enable the specified plugin.");
        sender.sendMessage(ChatColor.RED + "/plugin disable <pluginName>" + ChatColor.WHITE + " - Disable the specified plugin.");
        sender.sendMessage(ChatColor.RED + "/plugin reload <pluginName>" + ChatColor.WHITE + " - Reload the specified plugin.");
        sender.sendMessage(CC.translate(separator));
        sender.sendMessage("");
    }
}
