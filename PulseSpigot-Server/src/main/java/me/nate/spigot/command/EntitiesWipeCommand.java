package me.nate.spigot.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import me.nate.spigot.CC;

import java.util.EnumSet;

public class EntitiesWipeCommand extends Command {

    private static final EnumSet<EntityType> PROTECTED_ENTITIES = EnumSet.of(
            EntityType.PLAYER, EntityType.ARMOR_STAND
    );

    public EntitiesWipeCommand() {
        super("wipe");
        this.setDescription("Wipes all entities from the server.");
        this.setUsage("/wipe entities");
        this.setPermission("pulse.commands.wipe");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0 || !args[0].equalsIgnoreCase("entities")) {
                sender.sendMessage(CC.red + "Usage: /wipe entities");
                return false;
            }

            int removedCount = 0;

            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!PROTECTED_ENTITIES.contains(entity.getType())) {
                        entity.remove();
                        removedCount++;
                    }
                }
            }

            sender.sendMessage(CC.red + "Wiped " + CC.white + removedCount
                    + CC.red + " entities from the server.");
            return true;
        } else {
            if (!testPermission(sender)) return true;

            if (args.length == 0 || !args[0].equalsIgnoreCase("entities")) {
                sender.sendMessage(CC.red + "Usage: /wipe entities");
                return false;
            }

            int removedCount = 0;

            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!PROTECTED_ENTITIES.contains(entity.getType())) {
                        entity.remove();
                        removedCount++;
                    }
                }
            }

            sender.sendMessage(CC.red + "Wiped " + CC.white + removedCount
                    + CC.red + " entities from the server.");
            return true;
        }
    }
}
