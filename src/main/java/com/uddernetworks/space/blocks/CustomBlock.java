package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.items.IDHolder;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.meta.EnhancedMetadata;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.ChatColor;
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
    private int defaultDemand;
    private int defaultMaxLoad = -1;
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

    public void setDefaultDemand(int defaultDemand) {
        this.defaultDemand = defaultDemand;
    }

    public void setDefaultMaxLoad(int defaultMaxLoad) {
        this.defaultMaxLoad = defaultMaxLoad;
    }

    /**
     * The power that the block is currently receiving
     */
    public int getSupply(Block blockInstance) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        return (int) enhancedMetadata.getData("currentPower", this.defaultInputPower);
    }

    /**
     * Sets the power the block is currently receiving
     */
    public void setSupply(Block blockInstance, int power) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        enhancedMetadata.setData("currentPower", power);
        onSupplyChange(blockInstance, power);
    }

    /**
     * Gets the power that the block is demanding in order to work
     */
    public int getDemand(Block blockInstance) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        return (int) enhancedMetadata.getData("demand", this.defaultDemand);
    }

    /**
     * Sets the power that the block is demanding in order to work
     */
    public void setDemand(Block blockInstance, int power) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        enhancedMetadata.setData("demand", power);
        onDemandChange(blockInstance, power);
    }

    /**
     * Gets the maximum load the block may output
     */
    public int getMaxLoad(Block blockInstance) {
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        return (int) enhancedMetadata.getData("maxLoad", this.defaultMaxLoad);
    }

    /**
     * Sets the maximum load the block may output
     */
    public void setMaxLoad(Block blockInstance, int load) {
        System.out.println("blockInstance = [" + blockInstance + "], load = [" + load + "]");
        EnhancedMetadata enhancedMetadata = main.getEnhancedMetadataManager().getMetadata(blockInstance);

        enhancedMetadata.setData("maxLoad", load);
        onMaxLoadChange(blockInstance, load);
    }

    /**
     * The power the block outputs, this is its load
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
            onOutputChange(blockInstance, power);
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

    public void onSupplyChange(Block block, double newAmount) {}

    public void onDemandChange(Block block, double newAmount) {}

    public void onMaxLoadChange(Block block, double newAmount) {}

    public void onOutputChange(Block block, double newAmount) {}

    public void spawnParticles(Block block) {
        block.getWorld().playEffect(block.getLocation().add(0, 0.5D, 0), Effect.STEP_SOUND, particle.getId());
    }

    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        main.getBlockDataManager().getData(blockInstance, "inventoryID", inventoryID -> {
            if (inventoryID == null || main.getGUIManager().getGUI(UUID.fromString(inventoryID)) == null) {
                CustomGUI customGUI = customGUISupplier == null ? null : main.getGUIManager().addGUI(customGUISupplier.get());
                if (customGUI == null) return;
                customGUI.setParentBlock(blockInstance);
                main.getBlockDataManager().setData(blockInstance, "inventoryID", customGUI.getUUID(), () -> {
                    if (isElectrical()) main.getCircuitMapManager().addBlock(blockInstance);
                    if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
                });
            } else {
                CustomGUI customGUI = main.getGUIManager().getGUI(UUID.fromString(inventoryID));
                if (customGUI != null) customGUI.setParentBlock(blockInstance);
                if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
            }
        });
    }

    public void setTypeTo(Block block, short damage) {
        main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), damage);
    }

    abstract boolean hasGUI();
}
