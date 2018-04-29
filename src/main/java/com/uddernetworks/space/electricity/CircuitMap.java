package com.uddernetworks.space.electricity;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CircuitMap {

    private Main main;
    private int basePower = 0;
    private List<Block> blocks = new ArrayList<>();
    private List<Block> powerHungryBlocks = new ArrayList<>();
    private List<Block> generatingBlocks = new ArrayList<>();
    private List<Block> wireBlocks = new ArrayList<>();

    private List<ArmorStand> armorStands = new ArrayList<>();

    public CircuitMap(Main main, Block block) {
        this.main = main;
        this.blocks.add(block);
        updatePower();
    }

    public void updatePower() {
        updatePower(null);
    }

    public void updatePower(Block exclude) {
        powerHungryBlocks.clear();
        generatingBlocks.clear();
        wireBlocks.clear();

        basePower = 0;

        armorStands.forEach(ArmorStand::remove);
        armorStands.clear();

        for (Block block : new ArrayList<>(this.blocks)) {
            addBlocksNear(block);
        }

        for (Block block : new ArrayList<>(this.blocks)) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
            if (customBlock == null || !customBlock.isElectrical() || block.equals(exclude)) {
                this.blocks.remove(block);
                continue;
            }

            int blockOutputPower = customBlock.getMaxLoad(block);

            if (blockOutputPower != -1) {
                generatingBlocks.add(block);
                basePower += blockOutputPower;
            } else if (customBlock.wantPower()) {
                powerHungryBlocks.add(block);
            } else {
                wireBlocks.add(block);
            }
        }

//        int[] powers = splitIntoParts(basePower, powerHungryBlocks.size());
//
//        System.out.println("generatingBlocks = " + generatingBlocks);
//
//        System.out.println("powerHungryBlocks = " + powerHungryBlocks);
//
//        for (int i = 0; i < powers.length; i++) {
//            Block block = powerHungryBlocks.get(i);
//            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
//
//            customBlock.setOutputPower(block, powers[i]);
//        }

        List<CachedBlock> blockList = powerHungryBlocks.stream().map(block -> {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
            return new CachedBlock(block, customBlock, customBlock.getDemand(block));
        }).collect(Collectors.toList());

        int remaining = basePower;

        System.out.println("(FIRST) Remaining = " + remaining);

        System.out.println("blockList = " + blockList);

        while (remaining > 0 && !blockList.isEmpty()) {
            remaining = distributeWithRemaining(remaining, true, blockList);
            System.out.println("remaining = " + remaining);
        }

        if (!blockList.isEmpty()) blockList.forEach(cachedBlock -> cachedBlock.apply(true));


        System.out.println("generatingBlocks = " + generatingBlocks);

        blockList = generatingBlocks.stream().map(block -> {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
            return new CachedBlock(block, customBlock, customBlock.getMaxLoad(block));
        }).collect(Collectors.toList());

        remaining = basePower - remaining;

        System.out.println("(FIRST) Total load (REMAINING) = " + remaining);

        while (remaining > 0 && !blockList.isEmpty()) {
            remaining = distributeWithRemaining(remaining, false, blockList);
            System.out.println("remaining = " + remaining);
        }

        if (!blockList.isEmpty()) blockList.forEach(cachedBlock -> cachedBlock.apply(false));


        System.out.println("wireBlocks = " + wireBlocks);

        for (Block wireBlock : wireBlocks) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(wireBlock);

            customBlock.setSupply(wireBlock, basePower - remaining);
        }

        for (Block block : this.blocks) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);
            if (customBlock.getName().contains("Wire")) continue;

            if (customBlock.getOutputPower(block) > -1) {
                ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -0.6, 0.5), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setCustomName(ChatColor.GOLD + "Output: " + customBlock.getOutputPower(block));

                armorStands.add(armorStand);
            }

            if (customBlock.wantPower()) {
                ArmorStand armorStand2 = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -0.9, 0.5), EntityType.ARMOR_STAND);
                armorStand2.setVisible(false);
                armorStand2.setGravity(false);
                armorStand2.setCustomNameVisible(true);
                armorStand2.setCustomName(ChatColor.RED + "Demand: " + customBlock.getDemand(block)); // How much power it NEEDS

                armorStands.add(armorStand2);
            }

            if (customBlock.getSupply(block) > -1) {
                ArmorStand armorStand2 = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -1.2, 0.5), EntityType.ARMOR_STAND);
                armorStand2.setVisible(false);
                armorStand2.setGravity(false);
                armorStand2.setCustomNameVisible(true);
                armorStand2.setCustomName(ChatColor.RED + "Supply: " + customBlock.getSupply(block)); // How much power it is GETTING

                armorStands.add(armorStand2);
            }
        }
    }

    private void addBlocksNear(Block block) {
        for (BlockFace blockFace : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block nearBlock = block.getRelative(blockFace);

            if (!this.blocks.contains(nearBlock)) {
                if (isBlockElectrical(nearBlock)) {
                    this.blocks.add(nearBlock);
                    addBlocksNear(nearBlock);
                }
            }
        }
    }

    public void addBlock(Block block) {
        System.out.println("block = " + block);
        this.blocks.add(block);
        updatePower();
    }

    public void addAll(List<Block> blocks) {
        this.blocks.addAll(blocks);
        updatePower();
    }

    public void removeBlock(Block block) {
        this.blocks.remove(block);
        armorStands.forEach(ArmorStand::remove);
        armorStands.clear();
        if (this.blocks.size() > 0) updatePower(block);
    }

    private static int distributeWithRemaining(int amount, boolean supply, List<CachedBlock> blocks) {
        int[] parts = splitIntoParts(amount, blocks.size());
        int remaining = 0;

        System.out.println(Arrays.toString(parts));

        for (int i = blocks.size() - 1; i >= 0; i--) {
            int part = parts[i];
            CachedBlock cachedBlock = blocks.get(i);

            if (cachedBlock.getValue() + part >= cachedBlock.getMaxValue()) {
                remaining += part + cachedBlock.getValue() - cachedBlock.getMaxValue();
                cachedBlock.setValue(cachedBlock.getMaxValue());
                cachedBlock.apply(supply);
                blocks.remove(cachedBlock);
            } else {
                cachedBlock.setValue(cachedBlock.getValue() + part);
            }
        }

        return remaining;
    }

    private static int[] splitIntoParts(int whole, int parts) {
        int[] arr = new int[parts];
        for (int i = 0; i < arr.length; i++)
            whole -= arr[i] = (whole + parts - i - 1) / (parts - i);
        return arr;
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }

    private boolean isBlockElectrical(Block block) {
        return main.getBlockDataManager().getCustomBlock(block) != null && main.getBlockDataManager().getCustomBlock(block).isElectrical();
    }
}

class CachedBlock {
    private final Block block;
    private final CustomBlock customBlock;
    private final int maxValue;
    private int value;

    public CachedBlock(Block block, CustomBlock customBlock, int maxValue) {
        this.block = block;
        this.customBlock = customBlock;
        this.maxValue = maxValue;
    }

    public Block getBlock() {
        return block;
    }

    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void apply(boolean supply) {
        if (supply) {
            this.customBlock.setSupply(this.block, this.value);
        } else {
            this.customBlock.setOutputPower(this.block, this.value);
        }
    }
}