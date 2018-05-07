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
        super(main, id, material, damage, false, particle, name, customGUISupplier);
    }

    public BasicBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, false, particle, name, null);
    }

    @Override
    public boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    public boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return true;
    }

    @Override
    public void onPlace(Block block, Player player) {

    }

    @Override
    public void onClick(PlayerInteractEvent event) {

    }

    @Override
    public boolean hasGUI() {
        return false;
    }
}
