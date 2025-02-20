package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class EquipmentSetEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final HumanEntity humanEntity;
    private final int slot;
    private final ItemStack previousItem;
    private final ItemStack newItem;

    private boolean cancel;

    public EquipmentSetEvent(HumanEntity humanEntity, int slot, ItemStack previousItem, ItemStack newItem) {
        this.humanEntity = humanEntity;
        this.slot = slot;
        this.previousItem = previousItem;
        this.newItem = newItem;
    }

    public HumanEntity getHumanEntity() {
        return this.humanEntity;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getPreviousItem() {
        return this.previousItem;
    }

    public ItemStack getNewItem() {
        return this.newItem;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}