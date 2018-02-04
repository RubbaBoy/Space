package com.uddernetworks.space.recipies;

import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class Recipe {
    private ItemStack[][] items;
    private ItemStack[] flatItems;
    private boolean ordered;
    private ItemStack resulting;

    public Recipe(ItemStack[][] items, ItemStack resulting, boolean ordered) {
        this.ordered = ordered;
        this.resulting = resulting;

        if (ordered) {
            this.items = items;
        } else {
            this.flatItems = getAllItems(items);
        }
    }

    public Recipe(char[][] itemChars, Map<Character, ItemStack> representing, ItemStack resulting, boolean ordered) {
        this.ordered = ordered;
        this.resulting = resulting;

        System.out.println("representing = " + representing);

        ItemStack[][] items = new ItemStack[5][];
        for (int i = 0; i < 5; i++) {
            items[i] = new ItemStack[5];
            for (int i2 = 0; i2 < 5; i2++) {
                if (itemChars[i][i2] == ' ') {
                    items[i][i2] = ItemBuilder.AIR;
                } else {
                    System.out.println("Got = " + representing.get(itemChars[i][i2]));
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

    public boolean equals(ItemStack[][] items) {
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
                System.out.println("Adding together index " + ((i * 5) + i2));
                ret[(i * 5) + i2] = row[i2];
            }
        }

        System.out.println("ret = " + ret);

        return ret;
    }

    public ItemStack getResulting() {
        return resulting;
    }
}
