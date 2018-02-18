package com.uddernetworks.space.items;

import com.uddernetworks.space.nbt.NBTItem;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public abstract class CustomItem extends IDHolder {

    private int id;
    private Material material;
    private short damage;
    private String name;
    private ItemStack staticItemStack;
    private boolean stackable = true;

    public CustomItem(int id, Material material, short damage, String name) {
        super(id);
        this.material = material;
        this.damage = damage;
        this.name = name;

        NBTItem nbtItem = new NBTItem(ItemBuilder.create()
                .setMaterial(material)
                .setDamage(damage)
                .setDisplayName(ChatColor.RESET + name)
                .setUnbreakable(true)
                .addFlag(ItemFlag.HIDE_UNBREAKABLE)
                .addFlag(ItemFlag.HIDE_DESTROYS)
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addFlag(ItemFlag.HIDE_ENCHANTS).build());
        nbtItem.getTag().setInt("SpaceItem", id);
        nbtItem.getTag().setInt("random", ThreadLocalRandom.current().nextInt());
        this.staticItemStack = nbtItem.toItemStack();
    }

    public Material getMaterial() {
        return material;
    }

    public short getDamage() {
        return damage;
    }

    public String getName() {
        return name;
    }

    @Override
    public ItemStack toItemStack() {
        return staticItemStack;
    }

    abstract void onClick(PlayerInteractEvent event);

    abstract void onDrop(PlayerDropItemEvent event);

    abstract void onClickEntity(PlayerInteractAtEntityEvent event);

    public boolean isStackable() {
        return stackable;
    }
}
