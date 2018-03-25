package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.GeneratorGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class GeneratorBlock extends DirectionalBlock {

    public GeneratorBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(new GeneratorGUI(main, "Generator", 54, UUID.randomUUID())));
        setElectrical(true);
    }

    @Override
    boolean onBreak(Block block, Player player) {
        System.out.println("BREAKING GENERATORRRRRRRRRRRRRRRRRRRRRRRRRRR");
        main.getCircuitMapManager().removeBlock(block);
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return true;
    }

    @Override
    void onPlace(Block block, Player player) {
        setPowered(block, true);
        main.getCircuitMapManager().addBlock(block);

//        Bukkit.getScheduler().runTaskLater(main, () -> {
//            System.out.println("Setting powered");
//            setPowered(block, true);
//        }, 40L);
    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    boolean hasGUI() {
        return true;
    }

    public void setPowered(Block block, boolean powered) {
        // Powered status uses EnhancedMetadata because it's much faster and doesn't matter if it's persistent

        setOutputPower(block, powered ? 7000 : 0);
    }
}
