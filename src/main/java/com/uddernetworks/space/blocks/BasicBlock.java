package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class BasicBlock extends CustomBlock {

    public BasicBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, (short) damage, particle, name, null);
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

    @Override
    boolean hasGUI() {
        return false;
    }
}
