package com.uddernetworks.space.electricity;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

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

//            int blockInputPower = customBlock.getPower(block);
            int blockOutputPower = customBlock.getOutputPower(block);

            if (blockOutputPower != -1) {
                generatingBlocks.add(block);
                basePower += blockOutputPower;
            } else if (customBlock.wantPower()) {
                powerHungryBlocks.add(block);
            } else {
                wireBlocks.add(block);
            }
        }

        int[] powers = splitIntoParts(basePower, powerHungryBlocks.size());

        System.out.println("generatingBlocks = " + generatingBlocks);

        System.out.println("powerHungryBlocks = " + powerHungryBlocks);

        for (int i = 0; i < powers.length; i++) {
            Block block = powerHungryBlocks.get(i);
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);

            customBlock.setOutputPower(block, powers[i]);
        }

        System.out.println("wireBlocks = " + wireBlocks);

        for (Block wireBlock : wireBlocks) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(wireBlock);

            customBlock.setPower(wireBlock, basePower);
        }

        for (Block block : this.blocks) {
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);

            ArmorStand armorStand = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -1, 0.5), EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(ChatColor.GOLD + "Output: " + customBlock.getOutputPower(block));

            armorStands.add(armorStand);

            ArmorStand armorStand2 = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, -1.3, 0.5), EntityType.ARMOR_STAND);
            armorStand2.setVisible(false);
            armorStand2.setGravity(false);
            armorStand2.setCustomNameVisible(true);
            armorStand2.setCustomName(ChatColor.RED + "Power: " + customBlock.getPower(block));

            armorStands.add(armorStand2);
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

    private int[] splitIntoParts(int whole, int parts) {
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
