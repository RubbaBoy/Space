package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.function.Supplier;

public class AnimatedBlock extends CustomBlock {

//    private Consumer<Player> openInventory;

    public AnimatedBlock(Main main, Material material, short[] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, material, damages[0], particle, name, customGUISupplier);
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
//        openInventory.accept(event.getPlayer());
    }

    @Override
    boolean hasGUI() {
        return true;
    }
}
