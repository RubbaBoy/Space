package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.ElectricFurnaceGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.UUID;

public class ElectricFurnaceBlock extends DirectionalBlock {

    public ElectricFurnaceBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(new ElectricFurnaceGUI(main, "Electric Furnace", 27, UUID.randomUUID())));

        setSpeed(2);
        setSkip(2);
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
            ElectricFurnaceGUI electricFurnaceGUI = (ElectricFurnaceGUI) customGUI;
            electricFurnaceGUI.setSupply(newAmount);
        });
    }

    @Override
    public void onDemandChange(Block block, double newAmount) {
        getGUI(block, customGUI -> {
            ElectricFurnaceGUI electricFurnaceGUI = (ElectricFurnaceGUI) customGUI;
            electricFurnaceGUI.setDemand(newAmount);
        });
    }

    @Override
    public boolean hasGUI() {
        return true;
    }
}
