package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorkbenchBlock extends CustomBlock {

    public WorkbenchBlock(Material material, int damage, Material particle, String name, Class<? extends CustomGUI> gui) {
        super(material, (short) damage, particle, name, gui);
    }

    @Override
    boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player) {
        return true;
    }

    @Override
    void onPlace(Block block, Player player) {

    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }
}
