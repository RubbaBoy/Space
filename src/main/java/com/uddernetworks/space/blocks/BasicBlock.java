package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.function.Supplier;

public class BasicBlock extends CustomBlock {

    public BasicBlock(Main main, int id, Material material, int damage, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damage, particle, name, customGUISupplier);
    }

    public BasicBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, particle, name, null);
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
