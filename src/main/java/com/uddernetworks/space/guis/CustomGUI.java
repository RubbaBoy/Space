package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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
    private Block parentBlock = null;

    public CustomGUI(Main main, String title, InventoryType inventoryType, UUID uuid, GUIItems guiItems) {
        this.main = main;
        this.title = title;
        this.size = inventoryType.getDefaultSize();
        this.uuid = uuid;
        this.inventory = Bukkit.createInventory(this, inventoryType, this.title);

        if (guiItems != null) {
            for (GUIItems.GUIItem guiItem : guiItems.getGUIItems()) {
                addSlot(new PopulatedSlot(guiItem.getSlot(), false, guiItem.getItem()));
            }
        }

        slots.stream().filter(Slot::hasDefaultItem).forEach(slot -> inventory.setItem(slot.getIndex(), slot.getDefaultItem()));
    }

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
//        slots.stream().filter(openSlot -> openSlot.getIndex() == slot.getIndex() && !openSlot.hasDefaultItem()).findFirst().ifPresent(currentSlot -> slots.remove(currentSlot));

//        if (slots.stream().noneMatch(openSlot -> openSlot.getIndex() == slot.getIndex())) {
            slots.add(slot);
//        }
    }

    public void updateSlots() {
        slots.forEach(slot -> inventory.setItem(slot.getIndex(), slot.getDefaultItem()));
    }

    public void setParentBlock(Block parentBlock) {
        this.parentBlock = parentBlock;
    }

    public Block getParentBlock() {
        return parentBlock;
    }

    public void onClose(HumanEntity closer) {
    }

    public void onOpen(HumanEntity closer) {
    }

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder().getClass().equals(getClass())) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            for (Slot slot : slots) {
                if (slot.getIndex() == event.getSlot()) {
                    if (event.getCurrentItem().getType() != Material.AIR) {
                        if (slot.getSlotAction().takeOut(slot.getIndex(), event.getCurrentItem())) {
                            event.setCancelled(false);
                            event.setResult(Event.Result.ALLOW);

//                            Bukkit.getScheduler().runTaskLater(main, () -> {
                                ItemStack[] itemStacks = inventory.getContents().clone();
                                itemStacks[slot.getIndex()] = event.getCurrentItem().clone();

                                main.getBlockDataManager().setData(getParentBlock(), "inventoryContents", InventoryUtils.serializeInventory(itemStacks), () -> {});
//                            }, 1L);
                        }
                    } else {
                        if (slot.getSlotAction().putIn(slot.getIndex(), event.getCursor())) {
                            event.setCancelled(false);
                            event.setResult(Event.Result.ALLOW);

                            ItemStack[] itemStacks = inventory.getContents().clone();
                            itemStacks[slot.getIndex()] = event.getCursor().clone();
                            main.getBlockDataManager().setData(getParentBlock(), "inventoryContents", InventoryUtils.serializeInventory(itemStacks), () -> {});
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
//                main.getGUIManager().removeGUI(getUUID());
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
