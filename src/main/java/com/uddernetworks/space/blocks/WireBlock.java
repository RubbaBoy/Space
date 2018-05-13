package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
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

    private List<WireBlockReference> wireBlockReferences = Arrays.asList(
            new WireBlockReference(117,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(118,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(118,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(118,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(119,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(119,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(119,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(120,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(121,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(122,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(123,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(124, // CROSS
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(125,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(126,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(127,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.WIRE,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
            new WireBlockReference(128,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.WIRE, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.AIR,

                                        /* Bottom */
                                        ReferenceType.BLOCK),
















            new WireBlockReference(129,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(130,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(130,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(130,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(131,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(131,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(131,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(132,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(133,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(134,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(135,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR),
            new WireBlockReference(136, // CROSS
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(137,
                                        /* Top */
                                        ReferenceType.AIR,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(138,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.AIR, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(139,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.AIR,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.WIRE),
            new WireBlockReference(140,
                                        /* Top */
                                        ReferenceType.WIRE,

                                        /* North */
                    /* West */          ReferenceType.AIR, /* East */
                    ReferenceType.WIRE, /* South */            ReferenceType.WIRE,
                                        ReferenceType.BLOCK,

                                        /* Bottom */
                                        ReferenceType.AIR)
            );

    private ReferenceType getReferenceType(Block block, Block imagineDestroyed) {
        if (block.equals(imagineDestroyed) || block.getType() == Material.AIR || !block.getType().isSolid()) return ReferenceType.AIR;
        if (isBlockElectrical(block)) return ReferenceType.WIRE;
        return ReferenceType.BLOCK;
    }

    public void updateState(Block blockInstance, List<Block> updatedBlocks, Block imagineDestroyed) {
        if (!(main.getBlockDataManager().getCustomBlock(blockInstance) instanceof WireBlock)) return;

        Block north = blockInstance.getRelative(BlockFace.NORTH);
        Block south = blockInstance.getRelative(BlockFace.SOUTH);
        Block east = blockInstance.getRelative(BlockFace.EAST);
        Block west = blockInstance.getRelative(BlockFace.WEST);
        Block top = blockInstance.getRelative(BlockFace.UP);
        Block bottom = blockInstance.getRelative(BlockFace.DOWN);

        WireBlockReference wireBlockReference = new WireBlockReference(-1,
                                                        getReferenceType(top, imagineDestroyed),

                                                        getReferenceType(north, imagineDestroyed),
                getReferenceType(west, imagineDestroyed),                                       getReferenceType(east, imagineDestroyed),
                                                        getReferenceType(south, imagineDestroyed),

                                                        getReferenceType(bottom, imagineDestroyed));

        boolean northWire = isBlockElectrical(north) && !north.equals(imagineDestroyed);
        boolean southWire = isBlockElectrical(south) && !south.equals(imagineDestroyed);
        boolean eastWire = isBlockElectrical(east) && !east.equals(imagineDestroyed);
        boolean westWire = isBlockElectrical(west) && !west.equals(imagineDestroyed);
        boolean topWire = isBlockElectrical(top) && !top.equals(imagineDestroyed);
        boolean bottomWire = isBlockElectrical(bottom) && !bottom.equals(imagineDestroyed);

        boolean updated = false;

        for (WireBlockReference blockReference : this.wireBlockReferences) {
            if (blockReference.equals(wireBlockReference)) {
                System.out.println("blockReference.getId() = " + blockReference.getId());
                setTypeTo(blockInstance, blockReference.getId());
                updated = true;
                break;
            }
        }

        if (!updated) {
            System.out.println("Not updated");
            setTypeTo(blockInstance, 136);
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

        if (topWire && !updatedBlocks.contains(top)) {
            updatedBlocks.add(top);
            updateState(top, updatedBlocks, imagineDestroyed);
        }

        if (bottomWire && !updatedBlocks.contains(bottom)) {
            updatedBlocks.add(bottom);
            updateState(bottom, updatedBlocks, imagineDestroyed);
        }
    }

    private void setTypeTo(Block block, int customBlockID) {
        main.getCustomBlockManager().setBlockData(block.getWorld(), block, Material.DIAMOND_AXE, main.getCustomIDManager().getCustomBlockById(customBlockID).getDamage());
    }

    private boolean isBlockElectrical(Block block) {
        return main.getBlockDataManager().getCustomBlock(block) != null && main.getBlockDataManager().getCustomBlock(block).isElectrical();
    }

    private enum ReferenceType {
        AIR("Ａ"), BLOCK("Ｂ"), WIRE("Ｗ");

        private String message;

        ReferenceType(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return this.message;
        }
    }

    private class WireBlockReference {
        private int id;
        // Stores as:         X, Y, Z
        private ReferenceType north;
        private ReferenceType south;
        private ReferenceType east;
        private ReferenceType west;
        private ReferenceType top;
        private ReferenceType bottom;

        public WireBlockReference(int id, ReferenceType... referenceTypes) {
            this.id = id;

            top = referenceTypes[0];
            north = referenceTypes[1];
            west = referenceTypes[2];
            // Middle
            east = referenceTypes[3];
            south = referenceTypes[4];
            bottom = referenceTypes[5];
        }

//        public WireBlockReference(int id, ReferenceType[][]... referenceTypes) {
//            this.referenceTypes = referenceTypes;
//        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("　").append(top).append("\n");
            stringBuilder.append("　").append(north).append("\n");
            stringBuilder.append(west).append("　").append(east).append("\n");
            stringBuilder.append("　").append(south).append("\n");
            stringBuilder.append("　").append(bottom).append("\n");

            return stringBuilder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof WireBlockReference)) return false;
            WireBlockReference wireBlockReference = (WireBlockReference) obj;

            return north == wireBlockReference.north &&
                    south == wireBlockReference.south &&
                    east == wireBlockReference.east &&
                    west == wireBlockReference.west &&
                    top == wireBlockReference.top &&
                    bottom == wireBlockReference.bottom;
        }
    }

}
