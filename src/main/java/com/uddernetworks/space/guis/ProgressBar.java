package com.uddernetworks.space.guis;

import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class ProgressBar {

    private String name;
    private Material material;
    private int[] damages;

    public ProgressBar(String name, Material material, int... damages) {
        this.name = name;
        this.material = material;
        this.damages = damages;
    }

    public ItemStack getItemStack(double percentage) {
        percentage = Math.min(1, percentage / 100);
        percentage = percentage * (damages.length - 1);

        return ItemBuilder.from(material).setDamage(damages[(int) Math.round(percentage)]).setUnbreakable(true).addFlag(ItemFlag.HIDE_UNBREAKABLE).build();
    }

    public String getName() {
        return this.name;
    }
}
