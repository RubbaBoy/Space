package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.Debugger;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class CustomBlock {

    private Main main;
    private Material material;
    private short damage;
    private Material particle;
    private String name;
    private ItemStack staticDrop;
    private Supplier<CustomGUI> customGUISupplier;

    public CustomBlock(Main main, Material material, short damage, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        this.main = main;
        this.material = material;
        this.damage = damage;
        this.particle = particle;
        this.name = name;
        this.customGUISupplier = customGUISupplier;

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

    public CustomGUI getGUI(Block blockInstance) {
//        Debugger debugger = new Debugger();

//        debugger.log("111");
        List<MetadataValue> inventoryIDMeta = blockInstance.getMetadata("inventoryID");

//        debugger.log("222");

        if (inventoryIDMeta.size() == 0) return customGUISupplier == null ? null : main.getGUIManager().addGUI(customGUISupplier.get());

//        debugger.log("333");

        UUID inventoryID = UUID.fromString(inventoryIDMeta.get(0).asString());

//        debugger.log("444");

        CustomGUI customGUI = main.getGUIManager().getGUI(inventoryID);

//        debugger.log("555");

        CustomGUI ret = customGUI == null ? (customGUISupplier == null ? null : main.getGUIManager().addGUI(customGUISupplier.get())) : customGUI;

//        debugger.log("666");

//        debugger.end();

        return ret;
    }

    abstract boolean hasGUI();
}
