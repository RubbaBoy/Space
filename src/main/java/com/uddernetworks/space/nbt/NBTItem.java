package com.uddernetworks.space.nbt;

import com.uddernetworks.space.utils.Reflect;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public void updateItem() {
        Reflect.setField(this.nmsItemStack, "tag", this.tag, false);

        try {
            ItemMeta itemMeta = this.itemStack.getItemMeta();

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
