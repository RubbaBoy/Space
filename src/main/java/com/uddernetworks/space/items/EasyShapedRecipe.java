package com.uddernetworks.space.items;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.Reflect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    public EasyShapedRecipe(Main main, String recipeName, int resultID, String... shape) {
        this(main, recipeName, main.getCustomIDManager().getByID(resultID), shape);
    }

//    public EasyShapedRecipe(Main main, String recipeName, String customResultName, boolean customItem, String... shape) {
//        this(main, recipeName, customItem ? main.getCustomItemManager().getCustomItem(customResultName).toItemStack() : main.getCustomBlockManager().getCustomBlock(customResultName).toItemStack(), shape);
//    }

    public EasyShapedRecipe(Main main, String recipeName, IDHolder idHolder, String... shape) {
        this.main = main;
        this.recipeName = recipeName;
//        System.out.println("idHolder = " + idHolder);
//        System.out.println("idHolder.id = " + idHolder.id);
//        System.out.println("idHolder.toItemStack() = " + idHolder.toItemStack());
        this.result = idHolder.toItemStack();
        this.shape = shape;
    }

    public void addIngredient(Character cha, int customId) {
        this.ingredients.put(cha, main.getCustomIDManager().getByID(customId).toItemStack());
    }

    public void addIngredient(Character cha, ItemStack itemStack) {
        this.ingredients.put(cha, itemStack);
    }

    public void addIngredient(Character cha, Material material) {
        this.ingredients.put(cha, ItemBuilder.itemFrom(material));
    }

    public void register() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this.main, recipeName), this.result);
        recipe.shape(this.shape);
        Reflect.setField(recipe, "ingredients", ingredients, false);

        Bukkit.addRecipe(recipe);
    }

}
