package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.ElectricFurnaceGUI;
import com.uddernetworks.space.guis.GeneratorGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class ElectricFurnaceBlock extends DirectionalBlock {

    public ElectricFurnaceBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(new ElectricFurnaceGUI(main, "Electric Furnace", 27, UUID.randomUUID())));

        setSpeed(2);
        setWantPower(true);
        setElectrical(true);
        setDefaultDemand(1000);
    }

    @Override
    boolean onBreak(Block block, Player player) {
        System.out.println("BREAKING FURNACEEEEEEEEEEEEEEE");
        main.getCircuitMapManager().removeBlock(block);
        return super.onBreak(block, player);
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return super.onPrePlace(block, player, blockPrePlace);
    }

    @Override
    void onPlace(Block block, Player player) {
        main.getCircuitMapManager().addBlock(block);
    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    boolean hasGUI() {
        return true;
    }
}
