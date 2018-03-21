package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public class DirectionalBlock extends AnimatedBlock {

    // N S E W
//    private short[][] damages;
    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public DirectionalBlock(Main main, int id, Material material, short[][] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages, particle, name, customGUISupplier);
    }

    @Override
    boolean onBreak(Block block, Player player) {
        super.onBreak(block, player);
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        int direction = Math.round(player.getLocation().getYaw() / 90f) & 0x3;

        BlockFace blockFace = axis[direction].getOppositeFace();

        System.out.println("Direction = " + axis[direction]);

        System.out.println("damages = " + Arrays.deepToString(damages));

        int index = 0;

        switch (blockFace) {
            case NORTH:
                index = 0;
                break;
            case SOUTH:
                index = 1;
                break;
            case EAST:
                index = 2;
                break;
            case WEST:
                index = 3;
                break;
        }

        setDamages(block, damages[index]);

        main.getBlockDataManager().setData(block, "direction", index, () -> {});

        blockPrePlace.setUsingCallback(true);

        getBlockDamages(block, damages -> {
            setTypeTo(block, damages[0]);
            blockPrePlace.setDamage(damages[0]);
            blockPrePlace.getCallback().run();
        });

        return true;
    }

    @Override
    void onPlace(Block block, Player player) {

    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    boolean hasGUI() {
        return getCustomGUISupplier() != null;
    }

    private void setTypeTo(Block block, short damage) {
        System.out.println("damage = " + damage);
//        main.getCustomBlockManager().setBlockData(block.getWorld(), block, Material.DIAMOND_AXE, main.getCustomIDManager().getCustomBlockById(customBlockID).getDamage());
        main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), damage);
    }
}
