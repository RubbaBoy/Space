package com.uddernetworks.space.guis;

import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public enum GUIItems {
    WORKBENCH(new GUIItem(45, false, Material.DIAMOND_HOE, 4)),
    ALLOY_MIXER_MAIN(new GUIItem(45, false, Material.DIAMOND_HOE, 5));


    private GUIItem[] guiItems;

    GUIItems(GUIItem... guiItems) {

        this.guiItems = guiItems;
    }

    public GUIItem[] getGUIItems() {
        return guiItems;
    }

    public static class GUIItem {
        private int slot;
        private boolean isMovable;
        private ItemStack item;

        public GUIItem(int slot, boolean isMovable, Material material, int damage) {
            this.slot = slot;
            this.isMovable = isMovable;

            this.item = ItemBuilder.from(material).setDamage(damage).setUnbreakable(true).setDisplayName(null).addFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES).build();
        }

        public int getSlot() {
            return slot;
        }

        public boolean isMovable() {
            return isMovable;
        }

        public ItemStack getItem() {
            return item;
        }
    }

}
