package me.nate.spigot.item;

import net.minecraft.server.Item;

public interface HasToolMaterial {

    Item.EnumToolMaterial getToolMaterial();

    default String getToolMaterialName() {
        return getToolMaterial().toString();
    }

}
