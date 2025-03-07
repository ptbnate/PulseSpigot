package me.nate.spigot.command.plugin;

import me.nate.spigot.plugin.PluginManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class PulsePluginsCommand extends Command {

    private final PluginManager handler;

    public PulsePluginsCommand(String name) {
        super(name);
        setPermission("pulse.command.plugin");
        setAliases(Arrays.asList("plugins", "pl"));
        this.handler = new PluginManager();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            handler.sendHelp(sender);
            return true;
        }

        if (!testPermission(sender)) return true;

        switch (args[0].toLowerCase()) {
            case "list":
                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Invalid page number.");
                        return false;
                    }
                }
                handler.listPlugins(sender, page);
                break;

            case "enable":
                if (args.length < 2) {
                    handler.sendHelp(sender);
                    return true;
                }
                handler.enablePlugin(sender, args[1]);
                break;

            case "disable":
                if (args.length < 2) {
                    handler.sendHelp(sender);
                    return true;
                }
                handler.disablePlugin(sender, args[1]);
                break;

            case "reload":
                if (args.length < 2) {
                    handler.sendHelp(sender);
                    return true;
                }
                handler.reloadPlugin(sender, args[1]);
                break;

            default:
                handler.sendHelp(sender);
                break;
        }

        return true;
    }
}
