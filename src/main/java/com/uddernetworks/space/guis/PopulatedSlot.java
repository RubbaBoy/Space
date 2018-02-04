package com.uddernetworks.space.guis;

import org.bukkit.inventory.ItemStack;

public class PopulatedSlot implements Slot {
    private int index;
    private boolean movable;
    private ItemStack itemStack;
    private SlotAction slotAction;

    public PopulatedSlot(int index, boolean movable, ItemStack itemStack) {
        this.index = index;
        this.movable = movable;
        this.itemStack = itemStack;

        this.slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                return false;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                return false;
            }
        };
    }

    public int getIndex() {
        return index;
    }

    public boolean isMovable() {
        return movable;
    }

    @Override
    public SlotAction getSlotAction() {
        return slotAction;
    }

    @Override
    public boolean hasDefaultItem() {
        return true;
    }

    @Override
    public ItemStack getDefaultItem() {
        return itemStack;
    }


}
