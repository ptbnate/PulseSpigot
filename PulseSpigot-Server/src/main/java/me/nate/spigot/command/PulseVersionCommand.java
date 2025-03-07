package me.nate.spigot.command;

import me.nate.spigot.PulseSpigot;
import me.nate.spigot.config.PulseConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.nate.spigot.CC;

import java.util.Arrays;
import java.util.List;

/*
 *
 *  * Copyright (c) 2024 Krypton Development Services. All rights reserved.
 *  * Author: Nate
 *  * This code is proprietary and not to be used or shared without permission.
 *  * Unauthorized use may result in appropriate actions being taken.
 *
 */

public class PulseVersionCommand extends Command {

    public PulseVersionCommand(String name) {
        super(name);
        setAliases(Arrays.asList("ver", "icanhasbukkit"));
        setPermission("pulse.command.version");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        PulseConfig config = PulseConfig.get();
        List<String> versionInfo = config.commands.versionInfo;
        String version = PulseSpigot.get().getVersion();

        for (String message : versionInfo) {
            String formattedMessage = message.replace("{version}", version);
            sender.sendMessage(CC.translate(formattedMessage));
        }

        return true;
    }
}
