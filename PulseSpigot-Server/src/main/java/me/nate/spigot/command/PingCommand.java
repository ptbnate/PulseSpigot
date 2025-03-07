package me.nate.spigot.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.nate.spigot.CC;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
        this.description = "Shows ping of a player";
        this.usageMessage = "/ping [player]";
        this.setPermission("pulse.command.ping");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        Player targetPlayer;

        if (args.length >= 1) {
            targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(CC.red + "No player with that name is currently online.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(CC.red + "This command can only be run by a player.");
                return true;
            }

            targetPlayer = (Player) sender;
        }

        int ping = targetPlayer.spigot().getPing();

        if (sender.equals(targetPlayer)) {
            sender.sendMessage(CC.red + "Your ping is " + CC.white + " " + ping + CC.red + "ms.");
        } else {
            sender.sendMessage(CC.red + targetPlayer.getName() + "'s ping is " + CC.white + " " + ping + CC.red + "ms.");
        }

        return true;
    }
}