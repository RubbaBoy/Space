package com.uddernetworks.space.recipies;

import org.bukkit.inventory.ItemStack;

public class AlloyMixerRecipe extends Recipe {

    private ItemStack first;
    private ItemStack second;

    public AlloyMixerRecipe(ItemStack first, ItemStack second, ItemStack resulting) {
        super(resulting, RecipeType.ALLOY_MIXER);
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean getResultingItem(ItemStack[][] grid) {
        return (first.isSimilar(grid[0][0]) && second.isSimilar(grid[0][1]))
                || (second.isSimilar(grid[0][0]) && first.isSimilar(grid[0][1]));
    }
}
