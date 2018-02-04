package com.uddernetworks.space.guis;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum GUIItems {
    WORKBENCH(new GUIItem(45, false, new ItemStack(Material.DIAMOND_HOE, 1, (short) 4)));


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

        public GUIItem(int slot, boolean isMovable, ItemStack item) {
            this.slot = slot;
            this.isMovable = isMovable;
            this.item = item;

            ItemMeta meta = this.item.getItemMeta();

            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.spigot().setUnbreakable(true);
            meta.setDisplayName(null);
            meta.setLore(null);

            this.item.setItemMeta(meta);
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
