package com.uddernetworks.space.electricity;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class CircuitMapManager {

    private Main main;
    private List<CircuitMap> circuitMaps = new ArrayList<>();


    public CircuitMapManager(Main main) {
        this.main = main;
    }

    public void printStatus() {
        System.out.println("----------===============[START PRINT STATUS]===============----------");
        for (CircuitMap circuitMap : circuitMaps) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("\n\nCircuit Map =").append(circuitMap.toString());

            for (Block block : circuitMap.getBlocks()) {
                CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
                if (customBlock == null) continue;
                stringBuilder.append("\n").append(customBlock.getName()).append("(\t").append(block.getX()).append(", ").append(block.getY()).append(", ").append(block.getZ()).append(")");
            }

            System.out.println(stringBuilder.append("\n\n\n"));
        }

        System.out.println("----------===============[END PRINT STATUS]===============----------");

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
        if (getCircuitMap(block) != null) return;

        for (BlockFace blockFace : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block nearBlock = block.getRelative(blockFace);

            CircuitMap circuitMap = getCircuitMap(nearBlock);

            if (circuitMap == null) continue;

            System.out.println("=========== FOUND CIRCUIT MAP ON " + blockFace.name() + " SIDEEE!!!!!");

            circuitMap.addBlock(block);
            return;
        }

        System.out.println("Making new circuit map!!!!!!!!!!!!!! ------------------------------------");

        CircuitMap circuitMap = new CircuitMap(main, block);
        this.circuitMaps.add(circuitMap);

        circuitMap.updatePower();

        printStatus();
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

    public void removeCircuit(CircuitMap circuitMap) {
        this.circuitMaps.remove(circuitMap);
    }

    public void addCircuit(CircuitMap circuitMap) {
        this.circuitMaps.add(circuitMap);
    }

    public void clearDebugText() {
        this.circuitMaps.forEach(circuitMap -> circuitMap.getArmorStands().forEach(ArmorStand::remove));
    }
}
