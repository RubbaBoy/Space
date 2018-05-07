package com.uddernetworks.space.recipies;

import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class WorkbenchRecipe extends Recipe {
    private ItemStack[][] items;
    private ItemStack[] flatItems;
    private boolean ordered;
    private ItemStack resulting;

    public WorkbenchRecipe(ItemStack[][] items, ItemStack resulting, boolean ordered) {
        super(resulting, RecipeType.WORKBENCH);
        this.ordered = ordered;
        this.resulting = resulting;

        if (ordered) {
            this.items = items;
        } else {
            this.flatItems = getAllItems(items);
        }
    }

    public WorkbenchRecipe(char[][] itemChars, Map<Character, ItemStack> representing, ItemStack resulting, boolean ordered) {
        super(resulting, RecipeType.WORKBENCH);
        this.ordered = ordered;
        this.resulting = resulting;

        ItemStack[][] items = new ItemStack[5][];
        for (int i = 0; i < 5; i++) {
            items[i] = new ItemStack[5];
            for (int i2 = 0; i2 < 5; i2++) {
                if (itemChars[i][i2] == ' ') {
                    items[i][i2] = ItemBuilder.AIR;
                } else {
                    items[i][i2] = representing.get(itemChars[i][i2]).clone();
                }
            }
        }

        if (ordered) {
            this.items = items;
        } else {
            this.flatItems = getAllItems(items);
        }
    }

    public ItemStack[][] getItems() {
        return items;
    }

    public ItemStack[] getFlatItems() {
        return flatItems;
    }

    public boolean isOrdered() {
        return ordered;
    }

    private boolean equals(ItemStack[][] items) {
        if (ordered) {
//            System.out.println("RECIPE items = " + Arrays.deepToString(this.items));
            return Arrays.deepEquals(items, this.items);
        } else {
            return Arrays.deepEquals(this.flatItems, getAllItems(items));
        }
    }

    private ItemStack[] getAllItems(ItemStack[][] itemGrid) {
        ItemStack[] ret = new ItemStack[25];

        for (int i = 0; i < 5; i++) {
            ItemStack[] row = itemGrid[i];

            for (int i2 = 0; i2 < 5; i2++) {
                ret[(i * 5) + i2] = row[i2];
            }
        }

        return ret;
    }

    @Override
    public boolean getResultingItem(ItemStack[][] grid) {
        return equals(grid);
    }

    public ItemStack getResulting() {
        return resulting;
    }
}
