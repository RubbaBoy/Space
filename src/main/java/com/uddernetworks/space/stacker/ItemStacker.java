package com.uddernetworks.space.stacker;

import com.uddernetworks.space.items.CustomItem;
import com.uddernetworks.space.items.CustomItemManager;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemStacker implements Listener {

    private Main main;

    public ItemStacker(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryEvent(InventoryEvent event) {
        System.out.println("Inventory event");
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        System.out.println("Interacted");
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        CustomItem customCursor = main.getCustomItemManager().getCustomItem(event.getCursor());
        CustomItem customCurrent = main.getCustomItemManager().getCustomItem(event.getCurrentItem());

        switch (event.getClick()) {
            case CREATIVE:
                if (customCurrent == null) return;

                if (event.getAction() == InventoryAction.CLONE_STACK) {
                    event.getCursor().setAmount(64);
                }
                break;
            case DOUBLE_CLICK:
                if (customCurrent == null) return;
                ItemStack newCursorItem = event.getCursor().clone();

                InventoryView view = player.getOpenInventory();

                for (Inventory inventory : Arrays.asList(view.getTopInventory(), view.getBottomInventory())) {
                    for (int i = 0; i < inventory.getContents().length; i++) {
                        ItemStack temp = inventory.getItem(i);

                            CustomItem tempCustomItem = main.getCustomItemManager().getCustomItem(temp);

                            if (tempCustomItem != null && tempCustomItem.equals(customCursor)) {
                                if (temp.getAmount() + newCursorItem.getAmount() > 64) {
                                    temp.setAmount(temp.getAmount() + newCursorItem.getAmount() - 64);
                                    newCursorItem.setAmount(64);
                                } else {
                                    newCursorItem.setAmount(temp.getAmount() + newCursorItem.getAmount());
                                    temp.setAmount(0);
                                }
                            }

                        inventory.setItem(i, temp);
                    }
                }

                CustomItemManager.randomizeItem(newCursorItem);

                event.setCursor(newCursorItem);

                event.setCancelled(true);
                event.setResult(Event.Result.DENY);

                break;
            case RIGHT:
                if (customCurrent != null) {
                    CustomItemManager.randomizeItem(event.getCurrentItem());
                }

                if (customCursor != null) {
                    CustomItemManager.randomizeItem(event.getCursor());
                }
                break;
            case LEFT:
                if (customCursor != null && customCursor.equals(customCurrent)) {
                    int cursor = event.getCursor().getAmount();
                    int slot = event.getCurrentItem().getAmount();

                    int total = cursor + slot;

                    int newCursor;
                    int newCurrent;

                    if (total >= 64) {
                        newCursor = total - 64;
                        newCurrent = 64;
                    } else {
                        newCursor = 0;
                        newCurrent = total;
                    }

                    System.out.println("newCurrent = " + newCurrent);
                    System.out.println("newCursor = " + newCursor);

                    event.getCurrentItem().setAmount(newCurrent);

                    event.getCursor().setAmount(newCursor);

                    CustomItemManager.randomizeItem(event.getCurrentItem());
                    CustomItemManager.randomizeItem(event.getCursor());

                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                } else {
                    if (customCurrent != null) {
                        CustomItemManager.randomizeItem(event.getCurrentItem());
                    }

                    if (customCursor != null) {
                        CustomItemManager.randomizeItem(event.getCursor());
                    }
                }
                break;
            case SHIFT_LEFT:
                if (customCurrent != null) {


                    Inventory topInventory = player.getOpenInventory().getTopInventory();
                    Inventory bottomInventory = player.getOpenInventory().getBottomInventory();

                    boolean clickedTopInventory = event.getClickedInventory().equals(topInventory);

                    ItemStack thinkSlot = clickedTopInventory ? topInventory.getItem(event.getSlot()) : bottomInventory.getItem(event.getSlot());

                    System.out.println("thinkSlot = " + thinkSlot);
                    System.out.println("Actual slot = " + event.getCurrentItem());


                    event.setCurrentItem(ItemBuilder.itemFrom(Material.DIRT));

                    System.out.println("Inventory = " + event.getInventory());
                    System.out.println("Clicked inventory = " + event.getClickedInventory());

//                    event.setCancelled(true);
//                    event.setResult(Event.Result.DENY);
                }
                break;
            default:
                if (customCurrent != null) {
                    CustomItemManager.randomizeItem(event.getCurrentItem());
                }

                if (customCursor != null) {
                    CustomItemManager.randomizeItem(event.getCursor());
                }
                break;
        }

    }

    @EventHandler
    public void onDragItem(InventoryDragEvent event) {

    }

    @EventHandler
    public void onMoveItem(InventoryMoveItemEvent event) {

    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        System.out.println("PICKUP");
        CustomItem customItem = main.getCustomItemManager().getCustomItem(event.getItem().getItemStack());

        if (customItem == null) return;

        Inventory inventory = event.getPlayer().getInventory();
        CraftInventory craftInventory = (CraftInventory) inventory;

        int slot = first(craftInventory, customItem.toItemStack(), false);

        System.out.println("slot = " + slot);

        if (slot == -1) slot = firstEmpty(craftInventory);

        if (slot == -1) {
            System.out.println("Nope");
            event.setCancelled(true);
            return;
        }

        System.out.println("slot = " + slot);

        ItemStack itemInSlot = inventory.getItem(slot);

        System.out.println("itemInSlot = " + itemInSlot);

        if (itemInSlot == null) return;

        int amountToDrop = 0;
        int amountToGive;

        int total = itemInSlot.getAmount() + event.getItem().getItemStack().getAmount();
        if (total > 64) {
            amountToDrop = Math.abs(64 - total);
            amountToGive = 64;
        } else {
            amountToGive = total;
        }

        itemInSlot.setAmount(amountToGive);
        inventory.setItem(slot, itemInSlot);

        ItemStack onGround = event.getItem().getItemStack();
        onGround.setAmount(amountToDrop);

        event.getItem().setItemStack(onGround);

        event.setCancelled(true);
    }

    // See CraftInventory#firstEmpty()
    public int firstEmpty(CraftInventory craftInventory) {
        ItemStack[] inventory = craftInventory.getStorageContents();

        for(int i = 0; i < inventory.length; ++i) {
            if (inventory[i] == null) {
                return i;
            }
        }

        return -1;
    }

    // See CraftInventory#first(itemStack, boolean)
    private int first(CraftInventory craftInventory, ItemStack item, boolean withAmount) {
        if (item == null) {
            System.out.println("item = " + item);
            return -1;
        } else {
            ItemStack[] inventory = craftInventory.getStorageContents();
            System.out.println("inventory = " + Arrays.toString(inventory));
            int i = 0;

            while(true) {
                if (i >= inventory.length) {
                    return -1;
                }

                if (inventory[i] != null && inventory[i].getType() != Material.AIR) {
                    if (withAmount) {
                        if (item.equals(inventory[i])) {
                            break;
                        }
                    } else if (item.getType() == inventory[i].getType() && item.getDurability() == inventory[i].getDurability()) {
                        if (ItemBuilder.itemsEquals(item, inventory[i])) {
                            break;
                        }
                    }
                }

                ++i;
            }

            return i;
        }
    }

}
