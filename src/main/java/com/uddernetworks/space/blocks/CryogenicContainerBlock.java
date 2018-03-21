package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CryogenicContainerGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class CryogenicContainerBlock extends CustomBlock {

    public CryogenicContainerBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, particle, name, () -> main.getGUIManager().addGUI(new CryogenicContainerGUI(main, "Cryogenic Container", 54, UUID.randomUUID())));
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
}
