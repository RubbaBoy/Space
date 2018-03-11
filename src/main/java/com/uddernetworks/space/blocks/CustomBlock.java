package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.items.IDHolder;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CustomBlock extends IDHolder {

    Main main;
    private Material material;
    private short damage;
    private Material particle;
    private String name;
    private ItemStack staticDrop;
    private Supplier<CustomGUI> customGUISupplier;

    public CustomBlock(Main main, int id, Material material, int damage, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(id);
        this.main = main;
        this.material = material;
        this.damage = (short) damage;
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

    @Override
    public ItemStack toItemStack() {
        return staticDrop.clone();
    }

    abstract boolean onBreak(Block block, Player player);

    abstract boolean onPrePlace(Block block, Player player);

    abstract void onPlace(Block block, Player player);

    abstract void onClick(PlayerInteractEvent event);

    public void spawnParticles(Block block) {
        block.getWorld().playEffect(block.getLocation().add(0, 0.5D, 0), Effect.STEP_SOUND, particle.getId());
    }

    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        main.getBlockDataManager().getData(blockInstance, "inventoryID", inventoryID -> {
            System.out.println("inventoryID = " + inventoryID);
            if (inventoryID == null || main.getGUIManager().getGUI(UUID.fromString(inventoryID)) == null) {
                CustomGUI ret = customGUISupplier == null ? null : main.getGUIManager().addGUI(customGUISupplier.get());
                System.out.println("ret = " + ret);
                if (ret == null) return;
                main.getBlockDataManager().setData(blockInstance, "inventoryID", ret.getUUID(), () -> customGUIConsumer.accept(ret));
            } else {
                customGUIConsumer.accept(main.getGUIManager().getGUI(UUID.fromString(inventoryID)));
            }
        });
    }

    abstract boolean hasGUI();
}
