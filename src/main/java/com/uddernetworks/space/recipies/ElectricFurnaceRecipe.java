package com.uddernetworks.space.recipies;

import com.uddernetworks.space.main.Main;
import org.bukkit.inventory.ItemStack;

public class ElectricFurnaceRecipe extends Recipe {

    private Main main;
    private ItemStack input;

    public ElectricFurnaceRecipe(Main main, ItemStack input, ItemStack resulting) {
        super(resulting, RecipeType.ELECTRIC_FURNACE);
        this.main = main;
        this.input = input;
    }

    @Override
    public boolean getResultingItem(ItemStack[][] grid) {
        return main.getCustomItemManager().itemsSimilar(input, grid[0][0]);
    }
}
