package com.uddernetworks.space.stacker;

import com.uddernetworks.space.items.CustomItem;
import com.uddernetworks.space.items.CustomItemManager;
import com.uddernetworks.space.main.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
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
                if (event.getAction() == InventoryAction.CLONE_STACK) {
                    event.getCursor().setAmount(64);
                }
                break;
            case DOUBLE_CLICK:
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
    public void onItemPickup(InventoryPickupItemEvent event) {

    }

}
