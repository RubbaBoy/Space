package com.uddernetworks.space.recipies;

import org.bukkit.inventory.ItemStack;

public abstract class Recipe {

    ItemStack resulting;
    private final RecipeType type;

    public Recipe(ItemStack resulting, RecipeType type) {
        this.resulting = resulting;
        this.type = type;
    }

    public abstract boolean getResultingItem(ItemStack[][] grid);

    public ItemStack getResulting() {
        return resulting;
    }

    public RecipeType getType() {
        return type;
    }
}
