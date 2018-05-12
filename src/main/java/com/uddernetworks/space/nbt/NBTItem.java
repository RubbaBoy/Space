package com.uddernetworks.space.nbt;

import com.uddernetworks.space.utils.Reflect;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class NBTItem {
    private NBTTagCompound tag;
    private ItemStack itemStack;
    private net.minecraft.server.v1_12_R1.ItemStack nmsItemStack;

    public NBTItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.nmsItemStack = CraftItemStack.asNMSCopy(itemStack);

        this.tag = (NBTTagCompound) Reflect.getField(this.nmsItemStack, "tag", false);

        if (this.tag == null) this.tag = new NBTTagCompound();
    }

    public ItemStack toItemStack() {
        Reflect.setField(this.nmsItemStack, "tag", this.tag, false);
        ItemStack itemStack = CraftItemStack.asBukkitCopy(nmsItemStack);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = new ArrayList<>();

        for (String key : this.tag.c()) {
            String adding = null;
            if (this.tag.hasKeyOfType(key, 8)) {
                adding = this.tag.getString(key);
            } else if (this.tag.hasKeyOfType(key, 99)) {
                adding = String.valueOf(this.tag.getInt(key));
            }

            if (adding == null) continue;
            lore.add(ChatColor.RESET + "" + ChatColor.AQUA + key + " = " + adding);
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public void updateItem() {
        Reflect.setField(this.nmsItemStack, "tag", this.tag, false);

        try {
            ItemMeta itemMeta = this.itemStack.getItemMeta();

            List<String> lore = new ArrayList<>();

            for (String key : this.tag.c()) {
                String adding = null;
                if (this.tag.hasKeyOfType(key, 8)) {
                    adding = this.tag.getString(key);
                } else if (this.tag.hasKeyOfType(key, 99)) {
                    adding = String.valueOf(this.tag.getInt(key));
                }

                if (adding == null) continue;
                lore.add(ChatColor.RESET + "" + ChatColor.AQUA + key + " = " + adding);
            }

            itemMeta.setLore(lore);

            Reflect.setField(itemMeta, Class.forName("org.bukkit.craftbukkit.v1_12_R1.inventory.CraftMetaItem"), "internalTag", this.tag, false);

            this.itemStack.setItemMeta(itemMeta);

//            ItemMeta craftMetaItemInstance = (ItemMeta) Reflect.newInstance(Class.forName("org.bukkit.craftbukkit.v1_12_R1.inventory.CraftMetaItem"), new Class<?>[] { NBTTagCompound.class }, new Object[] { this.tag }, false);

//            this.itemStack.setItemMeta(craftMetaItemInstance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public NBTTagCompound getTag() {
        return this.tag;
    }
}
