//package com.uddernetworks.space.blocks;
//
//import com.uddernetworks.space.guis.CustomGUI;
//import com.uddernetworks.space.guis.WorkbenchBlock;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.inventory.ItemFlag;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//
//public enum CustomBlockTexture {
//    WORKBENCH(Material.DIAMOND_HOE, (short) 18, "Spaceship WorkbenchBlock", WorkbenchBlock.class);
//
//    private Material material;
//    private short damage;
//    private String name;
//    private Class<? extends CustomGUI> gui;
//
//    CustomBlockTexture(Material material, short damage, String name, Class<? extends CustomGUI> gui) {
//        this.material = material;
//        this.damage = damage;
//        this.name = name;
//        this.gui = gui;
//    }
//
//    public Material getMaterial() {
//        return material;
//    }
//
//    public short getDamage() {
//        return damage;
//    }
//
//    public Class<? extends CustomGUI> getGUI() {
//        return gui;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public ItemStack getItemStack() {
//        ItemStack itemStack = new ItemStack(material, 1, damage);
//
//        ItemMeta meta = itemStack.getItemMeta();
//        meta.setDisplayName(ChatColor.RESET + name);
//        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
//        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
//        meta.spigot().setUnbreakable(true);
//
//        itemStack.setItemMeta(meta);
//
//        return itemStack;
//    }
//}
