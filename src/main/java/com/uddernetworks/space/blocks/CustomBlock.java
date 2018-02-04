package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public abstract class CustomBlock {

    private Material material;
    private short damage;
    private Material particle;
    private String name;
    private ItemStack staticDrop;
    private Class<? extends CustomGUI> gui;

    public CustomBlock(Material material, short damage, Material particle, String name, Class<? extends CustomGUI> gui) {
        this.material = material;
        this.damage = damage;
        this.particle = particle;
        this.name = name;
        this.gui = gui;

        this.staticDrop = ItemBuilder.create().setMaterial(material).setDamage(damage).setDisplayName(ChatColor.RESET + name).setUnbreakable(true).addFlag(ItemFlag.HIDE_UNBREAKABLE).build();
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

    public Class<? extends CustomGUI> getGUI() {
        return gui;
    }

    public ItemStack getDrop() {
        return staticDrop.clone();
    }

    abstract boolean onBreak(Block block, Player player);

    abstract boolean onPrePlace(Block block, Player player);

    abstract void onPlace(Block block, Player player);

    abstract void onClick(PlayerInteractEvent event);

    public void spawnParticles(Block block) {
        block.getWorld().playEffect(block.getLocation().add(0, 0.5D, 0), Effect.STEP_SOUND, particle.getId());
    }
}
