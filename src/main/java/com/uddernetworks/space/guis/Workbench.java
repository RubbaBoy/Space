package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Workbench extends CustomGUI {

    private Main main;

    public Workbench(Main main, String title, int size) {
        super(title, size, GUIItems.WORKBENCH);

        this.main = main;

        SlotAction slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                System.out.println("Item has been put IN the slot: " + item);
                updateResult();
                return true;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                System.out.println("Item has been taken OUT");
                updateResult();
                return true;
            }
        };

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int slot = 10 + (y * 9) + x;
                addSlot(new OpenSlot(slot, slotAction));
            }
        }

        addSlot(new UnsettableSlot(34, this::clearCraftingGrid));
    }

    private void updateResult() {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            ItemStack[][] itemGrid = getCraftingGrid();

            ItemStack result = main.getRecipeManager().getResultingItem(itemGrid);

            getInventory().setItem(34, result);

        }, 1L);

    }

    private void clearCraftingGrid() {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    int slot = 10 + (y * 9) + x;
                    getInventory().setItem(slot, ItemBuilder.AIR);
                }
            }
        }, 1L);
    }

    private void dropCraftingGrid(HumanEntity lastViewer) {
        System.out.println("Dropping crafting grid!");
        if (getInventory().getViewers().size() == 1) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    int slot = 10 + (y * 9) + x;
                    ItemStack itemStack = getInventory().getItem(slot);
                    if (itemStack == null) continue;
                    Item item = lastViewer.getWorld().dropItem(lastViewer.getLocation(), itemStack);
                    item.setVelocity(lastViewer.getLocation().getDirection().normalize().multiply(0.5));
                }
            }
        }
    }

    private ItemStack[][] getCraftingGrid() {
        ItemStack[][] itemGrid = new ItemStack[5][];
        for (int y = 0; y < 5; y++) {
            itemGrid[y] = new ItemStack[5];
            for (int x = 0; x < 5; x++) {
                int slot = 10 + (y * 9) + x;
                ItemStack item = getInventory().getItem(slot);
                itemGrid[y][x] = item == null ? ItemBuilder.AIR : item;
            }
        }

        return itemGrid;
    }

    @Override
    public void onClose(HumanEntity closer) {
        dropCraftingGrid(closer);
    }

    @Override
    public void onOpen(HumanEntity closer) {

    }
}
