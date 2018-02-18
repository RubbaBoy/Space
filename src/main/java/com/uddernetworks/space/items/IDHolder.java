package com.uddernetworks.space.items;

import org.bukkit.inventory.ItemStack;

public abstract class IDHolder {
    int id;

    public IDHolder(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public abstract ItemStack toItemStack();
}
