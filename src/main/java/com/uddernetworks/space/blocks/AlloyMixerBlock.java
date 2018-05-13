package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.AlloyMixerGUI;
import com.uddernetworks.space.guis.ElectricFurnaceGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class AlloyMixerBlock extends AnimatedBlock {

    public AlloyMixerBlock(Main main, int id, Material material, short[] damages, Material particle, String name) {
        this(main, id, material, new short[][] {damages}, particle, name);
    }

    public AlloyMixerBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(main.getGUIManager().addGUI(new AlloyMixerGUI(main, "Alloy Mixer", 54, UUID.randomUUID()))));

//        setSpeed(2);
//        setSkip(1);
        setWantPower(true);
        setElectrical(true);
        setDefaultDemand(1000);
    }

    @Override
    public boolean onBreak(Block block, Player player) {
        main.getCircuitMapManager().removeBlock(block);
        return super.onBreak(block, player);
    }

    @Override
    public boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return super.onPrePlace(block, player, blockPrePlace);
    }

    @Override
    public void onPlace(Block block, Player player) {
        main.getCircuitMapManager().addBlock(block);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

    }

    @Override
    public void onSupplyChange(Block block, double newAmount) {
        getGUI(block, customGUI -> {
            AlloyMixerGUI alloyMixerGUI = (AlloyMixerGUI) customGUI;
            alloyMixerGUI.setSupply(newAmount);
        });
    }

    @Override
    public void onDemandChange(Block block, double newAmount) {
        getGUI(block, customGUI -> {
            AlloyMixerGUI alloyMixerGUI = (AlloyMixerGUI) customGUI;
            alloyMixerGUI.setDemand(newAmount);
        });
    }

    @Override
    public boolean hasGUI() {
        return true;
    }
}
