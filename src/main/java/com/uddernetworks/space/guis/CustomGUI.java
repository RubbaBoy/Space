package com.uddernetworks.space.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class CustomGUI implements InventoryHolder, Listener {

    private String title;
    private int size;
    private List<Slot> slots = new ArrayList<>();
    private Inventory inventory;

    public CustomGUI(String title, int size, GUIItems guiItems) {
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(this, this.size, this.title);

        for (GUIItems.GUIItem guiItem : guiItems.getGUIItems()) {
            addSlot(new PopulatedSlot(guiItem.getSlot(), false, guiItem.getItem()));
        }

        slots.stream().filter(Slot::hasDefaultItem).forEach(slot -> inventory.setItem(slot.getIndex(), slot.getDefaultItem()));
    }

    public void addSlot(Slot slot) {
        if (slots.stream().noneMatch(openSlot -> openSlot.getIndex() == slot.getIndex())) {
            slots.add(slot);
        }
    }

    public void onClose(HumanEntity closer) {
    }

    public void onOpen(HumanEntity closer) {
    }

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof CustomGUI) {
            for (Slot slot : slots) {
                if (slot.getIndex() == event.getSlot()) {
                    if (event.getCurrentItem().getType() != Material.AIR) {
                        if (!slot.getSlotAction().takeOut(slot.getIndex(), event.getCurrentItem())) {
                            System.out.println("Cancelling");
                            event.setCancelled(true);
                        }
                    } else {
                        if (!slot.getSlotAction().putIn(slot.getIndex(), event.getCursor())) {
                            System.out.println("Cancelling 2");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CustomGUI) {
            onClose(event.getPlayer());
        }
    }

    @EventHandler
    public void onCloseEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof CustomGUI) {
            onOpen(event.getPlayer());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
