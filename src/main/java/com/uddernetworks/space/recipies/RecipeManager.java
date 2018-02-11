package com.uddernetworks.space.recipies;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RecipeManager {

    private Main main;

    private List<Recipe> recipes = new ArrayList<>();

    public RecipeManager(Main main) {
        this.main = main;
    }

    public void addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
    }

    public ItemStack getResultingItem(ItemStack[][] itemGrid, RecipeType type) {
//        if (itemGrid.length != 5 || itemGrid[0].length != 5) {
//            return null;
//        }

        AtomicReference<ItemStack> result = new AtomicReference<>();

        result.set(ItemBuilder.itemFrom(Material.AIR));

        recipes.stream().filter(recipe -> recipe.getType() == type)
                        .filter(recipe -> recipe.getResultingItem(itemGrid))
                        .findFirst()
                        .ifPresent(recipe -> result.set(recipe.getResulting()));

        return result.get();
    }
}
