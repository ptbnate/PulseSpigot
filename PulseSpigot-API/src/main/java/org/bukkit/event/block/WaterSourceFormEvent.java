package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Represents events where a water source block is formed from two adjacent sources
 * <p>
 * If this event is cancelled, the water source will not form.
 */
public class WaterSourceFormEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected boolean cancel;

    public WaterSourceFormEvent(final Block block) {
        super(block);
    }

    public boolean isCancelled() {
        return cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}