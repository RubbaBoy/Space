package com.uddernetworks.space.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private Material material = Material.AIR;
    private int amount = 1;
    private short damage = (short) 0;
    private MaterialData materialData = new MaterialData(Material.AIR);

    private String name = null;
    private List<String> lore = null;
    private boolean unbreakable = false;
    private List<ItemFlag> flags = new ArrayList<>();
    private List<LeveledEnchant> enchantments = new ArrayList<>();

    public static ItemBuilder create() {
        return new ItemBuilder();
    }

    public static ItemStack itemFrom(Material material) {
        return material == null ? null : new ItemStack(material);
    }

    public static ItemBuilder from(Material material) {
        return itemFrom(itemFrom(material));
    }

    public static ItemBuilder itemFrom(ItemStack itemStack) {
        ItemBuilder builder = ItemBuilder.create();
        builder.material = itemStack.getType();
        builder.amount = itemStack.getAmount();
        builder.damage = itemStack.getDurability();
        builder.materialData = itemStack.getData();
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            builder.name = meta.getDisplayName();
            builder.lore = meta.getLore();
            builder.unbreakable = meta.isUnbreakable();
            builder.flags = new ArrayList<>(meta.getItemFlags());
            builder.enchantments = LeveledEnchant.fromList(meta.getEnchants());
        }

        return builder;
    }

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(material, amount, damage);
        itemStack.setData(materialData);

        ItemMeta meta = itemStack.getItemMeta();
        if (name != null) meta.setDisplayName(name);
        meta.setLore(lore);
        meta.setUnbreakable(unbreakable);

        flags.forEach(meta::addItemFlags);
        enchantments.forEach(enchantment -> meta.addEnchant(enchantment.getEnchantment(), enchantment.getLevel(), enchantment.ignoresLevelRestriction()));

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static boolean itemsEquals(net.minecraft.server.v1_12_R1.ItemStack item1, net.minecraft.server.v1_12_R1.ItemStack item2) {
        return itemsEquals(CraftItemStack.asBukkitCopy(item1), CraftItemStack.asBukkitCopy(item2));
    }

    public static boolean itemsEquals(ItemStack item1, ItemStack item2) {
        if (item1 == null) {
            return false;
        } else if (item1 == item2) {
            return true;
        } else {
            if (item1.getTypeId() == item2.getTypeId()
                    && item1.getDurability() == item2.getDurability()
                    && item1.hasItemMeta() == item2.hasItemMeta()) {
                if (item1.hasItemMeta()) {
                    if (item1.getItemMeta().getDisplayName().equals(item2.getItemMeta().getDisplayName())) {
                        if (item1.getItemMeta().getLore() != null) {
                            return item1.getItemMeta().getLore().equals(item2.getItemMeta().getLore());
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ItemBuilder setDamage(short damage) {
        this.damage = damage;
        return this;
    }

    public short getDamage() {
        return damage;
    }

    public ItemBuilder setDamage(int damage) {
        this.damage = (short) damage;
        return this;
    }

    public ItemBuilder setMaterialData(MaterialData materialData) {
        this.materialData = materialData;
        return this;
    }

    public MaterialData getMaterialData() {
        return materialData;
    }

    public ItemBuilder setDisplayName(String name) {
        this.name = ChatColor.RESET + name;
        return this;
    }

    public String getDisplayName() {
        return name;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public ItemBuilder addFlag(ItemFlag flag) {
        this.flags.add(flag);
        return this;
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public List<ItemFlag> getFlags() {
        return flags;
    }

    public ItemBuilder removeFlag(ItemFlag flag) {
        if (this.flags.contains(flag)) this.flags.remove(flag);
        return this;
    }

    public ItemBuilder addEnchant(LeveledEnchant enchantment) {
        this.enchantments.add(enchantment);
        return this;
    }

    public ItemBuilder addEnchants(List<LeveledEnchant> enchantments) {
        this.enchantments.addAll(enchantments);
        return this;
    }

    public List<LeveledEnchant> getEnchantments() {
        return enchantments;
    }

    public ItemBuilder removeEnchant(LeveledEnchant enchantment) {
        if (this.enchantments.contains(enchantment)) this.enchantments.remove(enchantment);
        return this;
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        for (LeveledEnchant leveledEnchant : new ArrayList<>(this.enchantments)) {
            if (leveledEnchant.getEnchantment() == enchantment) this.enchantments.remove(leveledEnchant);
        }
        return this;
    }

    public ItemBuilder clearEnchantments() {
        this.enchantments.clear();
        return this;
    }
}
