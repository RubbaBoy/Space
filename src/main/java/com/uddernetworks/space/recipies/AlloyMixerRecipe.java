package com.uddernetworks.space.recipies;

import com.uddernetworks.space.main.Main;
import org.bukkit.inventory.ItemStack;

public class AlloyMixerRecipe extends Recipe {

    private Main main;
    private ItemStack first;
    private ItemStack second;

    public AlloyMixerRecipe(Main main, ItemStack first, ItemStack second, ItemStack resulting) {
        super(resulting, RecipeType.ALLOY_MIXER);
        this.main = main;
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean getResultingItem(ItemStack[][] grid) {

        if (main.getCustomItemManager().itemsSimilar(first, grid[0][0]) && main.getCustomItemManager().itemsSimilar(second, grid[0][1])) {
            return true;
        }

        return main.getCustomItemManager().itemsSimilar(second, grid[0][0]) && main.getCustomItemManager().itemsSimilar(first, grid[0][1]);
    }
}
