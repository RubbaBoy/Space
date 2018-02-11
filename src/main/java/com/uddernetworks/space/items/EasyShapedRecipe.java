package com.uddernetworks.space.items;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.Reflect;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

public class EasyShapedRecipe {

    private Main main;
    private String recipeName;
    private ItemStack result;
    private String[] shape;
    private Map<Character, ItemStack> ingredients = new HashMap<>();

    public EasyShapedRecipe(Main main, String recipeName, String customItemResultName, String... shape) {
        this(main, recipeName, main.getCustomItemManager().getCustomItem(customItemResultName).toItemStack(), shape);
    }

    public EasyShapedRecipe(Main main, String recipeName, ItemStack result, String... shape) {
        this.main = main;
        this.recipeName = recipeName;
        this.result = result;
        this.shape = shape;
    }

    public void addIngredient(Character cha, ItemStack itemStack) {
        this.ingredients.put(cha, itemStack);
    }

    public void addIngredient(Character cha, String customItemName) {
        this.ingredients.put(cha, this.main.getCustomItemManager().getCustomItem(customItemName).toItemStack());
    }

    public void register() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this.main, recipeName), this.result);
        recipe.shape(this.shape);
        Reflect.setField(recipe, "ingredients", ingredients, false);

        Bukkit.addRecipe(recipe);
    }

}
