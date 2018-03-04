package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.AlloyMixerGUI;
import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CryogenicContainerBlock extends CustomBlock {

    public CryogenicContainerBlock(Main main, int id, Material material, short damage, Material particle, String name) {
        super(main, id, material, damage, particle, name, () -> main.getGUIManager().addGUI(new AlloyMixerGUI(main, "Alloy Mixer", 54, UUID.randomUUID())));
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
}
