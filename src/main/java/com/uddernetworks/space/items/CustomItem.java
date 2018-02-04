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

public abstract class CustomItem {

    private int id;
    private Material material;
    private short damage;
    private String name;
    private ItemStack staticItemStack;

    public CustomItem(Material material, short damage, String name) {
        this.material = material;
        this.damage = damage;
        this.name = name;

        this.staticItemStack = ItemBuilder.create()
                .setMaterial(material)
                .setDamage(damage)
                .setDisplayName(ChatColor.RESET + name)
                .setUnbreakable(true)
                .addFlag(ItemFlag.HIDE_UNBREAKABLE)
                .addFlag(ItemFlag.HIDE_DESTROYS)
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addFlag(ItemFlag.HIDE_ENCHANTS).build();
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

    public ItemStack toItemStack() {
        return staticItemStack;
    }

    abstract void onClick(PlayerInteractEvent event);

    abstract void onDrop(PlayerDropItemEvent event);

    abstract void onClickEntity(PlayerInteractAtEntityEvent event);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;

        NBTItem nbtItem = new NBTItem(this.staticItemStack);
        nbtItem.getTag().setInt("SpaceItem", id);
        nbtItem.getTag().setInt("random", ThreadLocalRandom.current().nextInt());
        this.staticItemStack = nbtItem.toItemStack();
    }
}
