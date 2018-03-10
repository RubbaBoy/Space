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
        return false;
    }

    @Override
    boolean onPrePlace(Block block, Player player) {
        return false;
    }

    @Override
    void onPlace(Block block, Player player) {

    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    boolean hasGUI() {
        return false;
    }

    public void addFill(Block block) {
        CryogenicContainerGUI cryogenicContainerGUI = (CryogenicContainerGUI) getGUI(block);
        cryogenicContainerGUI.updateFills();
    }
}
