package com.uddernetworks.space.items;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.nbt.NBTItem;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomItemManager implements Listener {

    private Main main;
    private List<CustomItem> customItems = new ArrayList<>();

    public CustomItemManager(Main main) {
        this.main = main;
    }

    public int getCount() {
        return this.customItems.size();
    }

    public boolean itemsSimilar(net.minecraft.server.v1_12_R1.ItemStack itemStack1, net.minecraft.server.v1_12_R1.ItemStack itemStack2) {
        return itemsSimilar(CraftItemStack.asBukkitCopy(itemStack1), CraftItemStack.asBukkitCopy(itemStack2));
    }

    public boolean itemsSimilar(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1.isSimilar(itemStack2)) return true;
        CustomItem customItem1 = getCustomItem(itemStack1);
        CustomItem customItem2 = getCustomItem(itemStack2);
        if (customItem1 == null || customItem2 == null) return false;
        return customItem1.equals(customItem2)
                || (itemStack1.getItemMeta().getDisplayName().equals(itemStack2.getItemMeta().getDisplayName())
                && itemStack1.getItemMeta().getLore().equals(itemStack2.getItemMeta().getLore()));
    }

    public int getMaxStackSize(net.minecraft.server.v1_12_R1.ItemStack itemStack) {
        return getMaxStackSize(CraftItemStack.asBukkitCopy(itemStack));
    }

    public int getMaxStackSize(ItemStack itemStack) {
        if (!isCustomItem(itemStack)) return itemStack.getMaxStackSize();
        return 64;
    }

    public boolean isItemStackable(net.minecraft.server.v1_12_R1.ItemStack itemStack) {
        CustomItem customItem = getCustomItem(CraftItemStack.asBukkitCopy(itemStack));
        if (customItem == null) return itemStack.isStackable();
        return customItem.isStackable();
    }

    public void addCustomItem(CustomItem customItem) {
        if (!customItems.contains(customItem)) {
//            customItem.setID(getCount() + 1);
            customItems.add(customItem);
        }
    }

    public CustomItem getCustomItem(Material material, short data) {
        for (CustomItem customItem : customItems) {
            if (customItem.getMaterial() == material && customItem.getDamage() == data) return customItem;
        }

        return null;
    }

//    public CustomItem getCustomItem(int id) {
//        for (CustomItem customItem : customItems) {
//            if (customItem.getID() == id) return customItem;
//        }
//
//        return null;
//    }

    public CustomItem getCustomItem(String name) {
        for (CustomItem customItem : customItems) {
            if (customItem.getName().equalsIgnoreCase(name)) return customItem;
        }

        return null;
    }

    public List<CustomItem> getCustomItems() {
        return new ArrayList<>(customItems);
    }

    public boolean isCustomItem(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getTag().hasKey("SpaceItem");
    }

    public CustomItem getCustomItem(ItemStack itemStack) {
        if (itemStack == null) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getTag() != null && nbtItem.getTag().hasKey("SpaceItem")) {
            return main.getCustomIDManager().getCustomItemById(nbtItem.getTag().getInt("SpaceItem"));
        }

        return null;
    }

    public static void randomizeItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.getTag().setInt("random", ThreadLocalRandom.current().nextInt());
        nbtItem.updateItem();
    }

    @EventHandler
    public void onClickEvent(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (isCustomItem(event.getItem())) {
            getCustomItem(event.getItem()).onClick(event);
        }
    }

    @EventHandler
    public void onDropEvent(PlayerDropItemEvent event) {
        if (isCustomItem(event.getItemDrop().getItemStack())) {
            getCustomItem(event.getItemDrop().getItemStack()).onDrop(event);
        }
    }

    @EventHandler
    public void onClickEntityEvent(PlayerInteractAtEntityEvent event) {
        ItemStack item = event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand();
        if (item == null) return;
        if (isCustomItem(item)) {
            getCustomItem(item).onClickEntity(event);
        }
    }
}
