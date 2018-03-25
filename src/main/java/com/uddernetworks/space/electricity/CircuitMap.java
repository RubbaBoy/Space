package com.uddernetworks.space.electricity;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CircuitMap {

    private Main main;
    private int basePower = 0;
    private List<Block> blocks = new ArrayList<>();
    private List<Block> powerHungryBlocks = new ArrayList<>();
    private List<Block> generatingBlocks = new ArrayList<>();

    public CircuitMap(Main main, Block block) {
        this.main = main;
        this.blocks.add(block);
    }

    public void updatePower() {
        powerHungryBlocks.clear();
        generatingBlocks.clear();

        for (Block block : new ArrayList<>(this.blocks)) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
            if (customBlock == null || !customBlock.isElectrical()) {
                this.blocks.remove(block);
                return;
            }

            int blockInputPower = customBlock.getPower(block);
            int blockOutputPower = customBlock.getOutputPower(block);

            if (blockOutputPower != -1) {
                generatingBlocks.add(block);
                basePower += blockOutputPower;
            } else if (blockInputPower > 0) {
                powerHungryBlocks.add(block);
            }
        }

        int[] powers = splitIntoParts(basePower, powerHungryBlocks.size());

        for (int i = 0; i < powers.length; i++) {
            Block block = powerHungryBlocks.get(i);
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);

            customBlock.setOutputPower(block, powers[i]);
        }
    }

    public void addBlock(Block block) {
        this.blocks.add(block);
        updatePower();
    }

    public void addAll(List<Block> blocks) {
        this.blocks.addAll(blocks);
        updatePower();
    }

    public void removeBlock(Block block) {
        this.blocks.remove(block);
        if (this.blocks.size() > 0) updatePower();
    }

    private int[] splitIntoParts(int whole, int parts) {
        int[] arr = new int[parts];
        for (int i = 0; i < arr.length; i++)
            whole -= arr[i] = (whole + parts - i - 1) / (parts - i);
        return arr;
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }
}
