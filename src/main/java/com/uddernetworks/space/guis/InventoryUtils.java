package com.uddernetworks.space.guis;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryUtils {

    public static String serializeInventory(ItemStack[] itemStacks) {
//        itemStacks = Arrays.stream(itemStacks)
//                .filter(itemStack -> (itemStack != null && itemStack.getType() != Material.AIR))
//                .toArray(ItemStack[]::new);

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("items", itemStacks);

        String saved = yamlConfiguration.saveToString();
        System.out.println("saved = \n" + saved);

        return saved;
    }

    public static ItemStack[] deserializeInventory(String string) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        try {
            yamlConfiguration.loadFromString(string);

            List<ItemStack> itemStackList = (List<ItemStack>) yamlConfiguration.getList("items");

            System.out.println("itemStackList = " + itemStackList);

            return itemStackList.toArray(new ItemStack[0]);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }
}
