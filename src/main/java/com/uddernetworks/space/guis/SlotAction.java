package com.uddernetworks.space.guis;

import org.bukkit.inventory.ItemStack;

public interface SlotAction {
    boolean putIn(int position, ItemStack item); // If false, cancel, if true, allow

    boolean takeOut(int position, ItemStack item); // If false, cancel, if true, allow
}
