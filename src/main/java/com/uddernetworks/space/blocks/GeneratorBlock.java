package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CryogenicContainerGUI;
import com.uddernetworks.space.guis.GeneratorGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class GeneratorBlock extends DirectionalBlock {

    public GeneratorBlock(Main main, int id, Material material, short[][] damages, Material particle, String name) {
        super(main, id, material, damages, particle, name, () -> main.getGUIManager().addGUI(new GeneratorGUI(main, "Generator", 54, UUID.randomUUID())));
    }

    @Override
    boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return true;
    }

    @Override
    void onPlace(Block block, Player player) {

    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    boolean hasGUI() {
        return true;
    }

    public void addFill(Block block) {
        main.getBlockDataManager().increment(block, "cryogenicContainer", 1, newValue -> getGUI(block, gui -> {
            CryogenicContainerGUI cryogenicContainerGUI = (CryogenicContainerGUI) gui;
            cryogenicContainerGUI.setFills(newValue);
            cryogenicContainerGUI.updateFills();
        }));
    }

    public void setPowered(Block block, boolean powered) {
        // Powered status uses MetaData because it's much faster and doesn't matter if it's persistant

    }
}
