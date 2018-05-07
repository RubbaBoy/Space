package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class WireBlock extends CustomBlock {

    public WireBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, true, particle, name, null);
    }

    @Override
    public boolean onBreak(Block block, Player player) {
        List<Block> list = new ArrayList<>();
        list.add(block);
        updateState(block, list, block);
        main.getCircuitMapManager().removeBlock(block);
        return true;
    }

    @Override
    public boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return true;
    }

    @Override
    public void onPlace(Block block, Player player) {
        updateState(block, new ArrayList<>(), null);
        main.getCircuitMapManager().addBlock(block);
    }

    @Override
    public void onClick(PlayerInteractEvent event) {

    }

    @Override
    public boolean hasGUI() {
        return false;
    }

    public void updateState(Block blockInstance, List<Block> updatedBlocks, Block imagineDestroyed) {
        if (!(main.getBlockDataManager().getCustomBlock(blockInstance) instanceof WireBlock)) return;

        Block north = blockInstance.getRelative(BlockFace.NORTH);
        Block south = blockInstance.getRelative(BlockFace.SOUTH);
        Block east = blockInstance.getRelative(BlockFace.EAST);
        Block west = blockInstance.getRelative(BlockFace.WEST);

        boolean northWire = isBlockElectrical(north) && !north.equals(imagineDestroyed);
        boolean southWire = isBlockElectrical(south) && !south.equals(imagineDestroyed);
        boolean eastWire = isBlockElectrical(east) && !east.equals(imagineDestroyed);
        boolean westWire = isBlockElectrical(west) && !west.equals(imagineDestroyed);

        if ((northWire || southWire) && !eastWire && !westWire) {
            setTypeTo(blockInstance, 119);
        } else if ((eastWire || westWire) && !northWire && !southWire) {
            setTypeTo(blockInstance, 118);
        } else if ((southWire && eastWire) && !northWire && !westWire) {
            setTypeTo(blockInstance, 120);
        } else if ((southWire && westWire) && !northWire && !eastWire) {
            setTypeTo(blockInstance, 121);
        } else if ((northWire && westWire) && !southWire && !eastWire) {
            setTypeTo(blockInstance, 122);
        } else if ((northWire && eastWire) && !southWire && !westWire) {
            setTypeTo(blockInstance, 123);
        } else if (!northWire && !southWire) {
            setTypeTo(blockInstance, 117);
        } else {
            setTypeTo(blockInstance, 124);
        }

        if (northWire && !updatedBlocks.contains(north)) {
            updatedBlocks.add(north);
            updateState(north, updatedBlocks, imagineDestroyed);
        }

        if (southWire && !updatedBlocks.contains(south)) {
            updatedBlocks.add(south);
            updateState(south, updatedBlocks, imagineDestroyed);
        }

        if (eastWire && !updatedBlocks.contains(east)) {
            updatedBlocks.add(east);
            updateState(east, updatedBlocks, imagineDestroyed);
        }

        if (westWire && !updatedBlocks.contains(west)) {
            updatedBlocks.add(west);
            updateState(west, updatedBlocks, imagineDestroyed);
        }
    }

    private void setTypeTo(Block block, int customBlockID) {
        main.getCustomBlockManager().setBlockData(block.getWorld(), block, Material.DIAMOND_AXE, main.getCustomIDManager().getCustomBlockById(customBlockID).getDamage());
    }

    private boolean isBlockElectrical(Block block) {
        return main.getBlockDataManager().getCustomBlock(block) != null && main.getBlockDataManager().getCustomBlock(block).isElectrical();
    }

}
