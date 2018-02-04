package com.uddernetworks.space.guis;

import org.bukkit.inventory.ItemStack;

public class OpenSlot implements Slot {

    private int index;
    private SlotAction slotAction;

    public OpenSlot(int index, SlotAction slotAction) {
        this.index = index;
        this.slotAction = slotAction;
    }

    public int getIndex() {
        return index;
    }

    public SlotAction getSlotAction() {
        return slotAction;
    }

    @Override
    public boolean hasDefaultItem() {
        return false;
    }

    @Override
    public ItemStack getDefaultItem() {
        return null;
    }
}
