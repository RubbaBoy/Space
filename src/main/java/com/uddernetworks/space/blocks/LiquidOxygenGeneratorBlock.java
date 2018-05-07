package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.LiquidOxygenGeneratorGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class LiquidOxygenGeneratorBlock extends CustomBlock {

    public LiquidOxygenGeneratorBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, false, particle, name, () -> main.getGUIManager().addGUI(new LiquidOxygenGeneratorGUI(main, "Liquid Oxygen Generator", 54, UUID.randomUUID())));
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
        return true;
    }
}
