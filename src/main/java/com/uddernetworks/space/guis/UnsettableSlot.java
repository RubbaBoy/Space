package com.uddernetworks.space.guis;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UnsettableSlot implements Slot {
    private int index;
    private SlotAction slotAction;

    public UnsettableSlot(int index, Runnable takeOut) {
        this.index = index;

        this.slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                return false;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                if (item != null && item.getType() != Material.AIR) {
                    takeOut.run();
                }

                return true;
            }
        };
    }

    public int getIndex() {
        return index;
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
        return null;
    }


}
