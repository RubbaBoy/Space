package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.items.IDHolder;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.meta.EnhancedMetadata;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class CustomBlock extends IDHolder {

    Main main;
    private Material material;
    private short damage;
    private boolean electrical;
    private boolean wantPower = false;
    private int defaultInputPower;
    private Material particle;
    private String name;
    private ItemStack staticDrop;
    private Supplier<CustomGUI> customGUISupplier;

    public CustomBlock(Main main, int id, Material material, int damage, boolean electrical, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(id);
        this.main = main;
        this.material = material;
        this.damage = (short) damage;
        this.electrical = electrical;
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

    public Supplier<CustomGUI> getCustomGUISupplier() {
        return customGUISupplier;
    }

    public void setElectrical(boolean electrical) {
        this.electrical = electrical;
    }

    public boolean isElectrical() {
        return electrical;
    }

    public boolean wantPower() {
        return wantPower;
    }

    public void setWantPower(boolean wantPower) {
        this.wantPower = wantPower;
    }

    public void setDefaultInputPower(int defaultInputPower) {
        this.defaultInputPower = defaultInputPower;
    }

    /**
     * The power required for the block
     */
    public int getPower() {
        return this.defaultInputPower;
    }

    /**
     * The power that the block is currently receiving
     */
    public int getPower(Block blockInstance) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        return (int) enhancedMetadata.getData("currentPower", this.defaultInputPower);
    }

    /**
     * Sets the power the block is currently receiving
     */
    public void setPower(Block blockInstance, int power) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        enhancedMetadata.setData("currentPower", power);
    }

    /**
     * The power the block outputs
     */
    public int getOutputPower(Block blockInstance) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        return (int) enhancedMetadata.getData("outputPower", -1);
    }

    /**
     * Sets the amount of power the block outputs
     */
    public boolean setOutputPower(Block blockInstance, int power) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        int currentPower = (int) enhancedMetadata.getData("outputPower", -1);

        if (currentPower != power) {
            enhancedMetadata.setData("outputPower", power);
            return true;
        }

        return false;
    }

    @Override
    public ItemStack toItemStack() {
        return staticDrop.clone();
    }

    abstract boolean onBreak(Block block, Player player);

    abstract boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace);

    abstract void onPlace(Block block, Player player);

    abstract void onClick(PlayerInteractEvent event);

    public void spawnParticles(Block block) {
        block.getWorld().playEffect(block.getLocation().add(0, 0.5D, 0), Effect.STEP_SOUND, particle.getId());
    }

    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        System.out.println("getGUI BLOCk = " + blockInstance);
        main.getBlockDataManager().getData(blockInstance, "inventoryID", inventoryID -> {
            if (inventoryID == null || main.getGUIManager().getGUI(UUID.fromString(inventoryID)) == null) {
                CustomGUI customGUI = customGUISupplier == null ? null : main.getGUIManager().addGUI(customGUISupplier.get());
                if (customGUI == null) return;
                customGUI.setParentBlock(blockInstance);
                main.getBlockDataManager().setData(blockInstance, "inventoryID", customGUI.getUUID(), () -> customGUIConsumer.accept(customGUI));
            } else {
                CustomGUI customGUI = main.getGUIManager().getGUI(UUID.fromString(inventoryID));
                if (customGUI != null) customGUI.setParentBlock(blockInstance);
                customGUIConsumer.accept(customGUI);
            }
        });
    }

    abstract boolean hasGUI();
}
