package xyz.krypton.spigot.event.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UnknownCommandEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CommandSender sender;

    private final String fullCommand;
    private final String command;

    private boolean cancelled;

    public UnknownCommandEvent(CommandSender sender, String fullCommand) {
        this.sender = sender;

        this.fullCommand = fullCommand;
        int spaceIndex = fullCommand.indexOf(' ');
        this.command = fullCommand.substring(0, spaceIndex != -1 ? spaceIndex : fullCommand.length());
    }

    /**
     * @return the sender that was trying to use the command
     */
    @NotNull
    public CommandSender getSender() {
        return sender;
    }

    /**
     * @return the full command with arguments for eg. "/test arg1 arg2" will return "test arg1 arg2"
     */
    @NotNull
    public String getFullCommand() {
        return fullCommand;
    }

    /**
     * @return the command name for eg. "/test arg1 arg2" will return "test"
     */
    @NotNull
    public String getCommand() {
        return command;
    }

    /**
     * Determines if this event is cancelled.
     *
     * <p>When this event is cancelled, the default unknown command message won't be send</p>
     *
     * @return {@code true} if this event is cancelled, {@code false} otherwise
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets if this event is cancelled.
     *
     * <p>When this event is cancelled, the default unknown command message won't be send</p>
     *
     * @param cancel {@code true} if this event should be cancelled, {@code false} if not
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
