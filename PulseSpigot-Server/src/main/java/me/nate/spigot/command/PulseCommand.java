package me.nate.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PulseCommand extends Command {

    private final String separator = "§8§m-=-------------------------=-";

    public PulseCommand() {
        super("pulse");
        this.description = "Displays information related to PulseSpigot.";
        this.setAliases(Arrays.asList("spigot", "pulsespigot"));
        this.usageMessage = "/pulse";
        this.setPermission("pulse.command");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;

        if (!testPermission(sender)) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
        } else {
            player.sendMessage("§cThis command doesn't exist.");
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(separator);
        player.sendMessage("§c§lPulseSpigot Help:");
        player.sendMessage(separator);
        player.sendMessage("");
        player.sendMessage("§c/pulse" + " §7- View this help message.");
        player.sendMessage("§c/knockback" + " §7- Customize knockback settings.");
        player.sendMessage("§c/wipe entities" + " §7- To wipe all the entities from a world.");
        player.sendMessage("§c/motion" + " §7- Modify potion and pearl motion behavior.");
        player.sendMessage("§c/ping" + " §7- Check your latency (configurable in messages.yml).");
        player.sendMessage("§c/version" + " §7- Show PulseSpigot's version (configurable in pulse.yml).");
        player.sendMessage("§c/plugin <enable/disable/load> <pluginName>" + " §7- Manage plugins (configurable in pulse.yml).");
        player.sendMessage("");
        player.sendMessage(separator);
    }


}
