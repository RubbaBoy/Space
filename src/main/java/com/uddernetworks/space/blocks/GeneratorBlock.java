package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.guis.GeneratorGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;
import java.util.function.Consumer;

public class GeneratorBlock extends DirectionalBlock {

    public GeneratorBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(new GeneratorGUI(main, "Generator", 54, UUID.randomUUID())));
        setElectrical(true);
        setDefaultMaxLoad(0);
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
        setPowered(block, true);
        main.getCircuitMapManager().addBlock(block);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

    }

    @Override
    public boolean hasGUI() {
        return true;
    }

    @Override
    public void onMaxLoadChange(Block block, double newAmount) {

    }

    @Override
    public void onOutputChange(Block block, double newAmount) {
        getGUI(block, customGUI -> {
            GeneratorGUI generatorGUI = (GeneratorGUI) customGUI;
            generatorGUI.updateOutputMeter(newAmount, getMaxLoad(block));
        });
    }

    @Override
    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        super.getGUI(blockInstance, customGUI -> {
            setPowered(blockInstance, true); // TODO: Remove later
            if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
        });
    }

    public void setPowered(Block block, boolean powered) {
        System.out.println("GeneratorBlock.setPowered");
        System.out.println("block = [" + block + "], powered = [" + powered + "]");
        // Powered status uses EnhancedMetadata because it's much faster and doesn't matter if it's persistent

        setMaxLoad(block, powered ? 7000 : 0);
//        updateCircuit(block);
    }
}
