package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
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
import java.util.UUID;

public class CustomGUI implements InventoryHolder, Listener {

    Main main;
    private String title;
    private int size;
    private List<Slot> slots = new ArrayList<>();
    private Inventory inventory;
    private UUID uuid;

    public CustomGUI(Main main, String title, int size, UUID uuid, GUIItems guiItems) {
        this.main = main;
        this.title = title;
        this.size = size;
        this.uuid = uuid;
        this.inventory = Bukkit.createInventory(this, this.size, this.title);

        if (guiItems != null) {
            for (GUIItems.GUIItem guiItem : guiItems.getGUIItems()) {
                addSlot(new PopulatedSlot(guiItem.getSlot(), false, guiItem.getItem()));
            }
        }

        slots.stream().filter(Slot::hasDefaultItem).forEach(slot -> inventory.setItem(slot.getIndex(), slot.getDefaultItem()));
    }

    public void addSlot(Slot slot) {
        if (slots.stream().noneMatch(openSlot -> openSlot.getIndex() == slot.getIndex())) {
            slots.add(slot);
        }
    }

    public void updateSlots() {
        slots.forEach(slot -> inventory.setItem(slot.getIndex(), slot.getDefaultItem()));
    }

    public void onClose(HumanEntity closer) {
    }

    public void onOpen(HumanEntity closer) {
    }

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getInventory().getHolder().getClass().equals(getClass())) {
            for (Slot slot : slots) {
                if (slot.getIndex() == event.getSlot()) {
                    if (event.getCurrentItem().getType() != Material.AIR) {
                        if (!slot.getSlotAction().takeOut(slot.getIndex(), event.getCurrentItem())) {
                            event.setCancelled(true);
                        }
                    } else {
                        if (!slot.getSlotAction().putIn(slot.getIndex(), event.getCursor())) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory().getHolder().getClass().equals(getClass())) {
            onClose(event.getPlayer());

            if (event.getInventory().getViewers().size() - 1 == 0) {
                main.getGUIManager().removeGUI(getUUID());
            }
        }
    }

    @EventHandler
    public void onOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder().getClass().equals(getClass())) {
            onOpen(event.getPlayer());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID getUUID() {
        return uuid;
    }
}
