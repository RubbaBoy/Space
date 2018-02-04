package com.uddernetworks.space.guis;

import org.bukkit.inventory.ItemStack;

public interface Slot {
    int getIndex();

    SlotAction getSlotAction();

    boolean hasDefaultItem();

    ItemStack getDefaultItem();
}
