package com.uddernetworks.space.electricity;

import com.uddernetworks.space.main.Main;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class CircuitMapManager {

    private Main main;
    private List<CircuitMap> circuitMaps = new ArrayList<>();

    public CircuitMapManager(Main main) {
        this.main = main;
    }

    public CircuitMap getCircuitMap(Block block) {
        for (CircuitMap circuitMap : circuitMaps) {
            if (circuitMap.getBlocks().contains(block)) {
                return circuitMap;
            }
        }

        return null;
    }

    public void addBlock(Block block) {
        for (BlockFace blockFace : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block nearBlock = block.getRelative(blockFace);

            CircuitMap circuitMap = getCircuitMap(nearBlock);

            if (circuitMap == null) continue;

            System.out.println("=========== FOUND CIRCUIT MAP ON " + blockFace.name() + " SIDEEE!!!!!");

            circuitMap.addBlock(block);
            return;
        }

        System.out.println("Making new circuit map!!!!!!!!!!!!!! ------------------------------------");

        this.circuitMaps.add(new CircuitMap(main, block));
    }

    public void removeBlock(Block block) {
        CircuitMap circuitMap = getCircuitMap(block);

        if (circuitMap == null) return;

        circuitMap.removeBlock(block);

        if (circuitMap.getBlocks().size() == 0) this.circuitMaps.remove(circuitMap);
    }

//    public void combineCircuits(CircuitMap circuitMap1, CircuitMap circuitMap2) {
//
//    }

    public void updateCircuit(Block block) {
        getCircuitMap(block).updatePower();
    }
}
