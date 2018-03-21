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
    private short[][] damages;
    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public DirectionalBlock(Main main, int id, Material material, short[][] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages[0], particle, name, customGUISupplier);
        this.damages = damages;
    }

    @Override
    boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
        return true;
    }

    @Override
    void onPlace(Block block, Player player) {
        int direction = Math.round(player.getLocation().getYaw() / 90f) & 0x3;

        main.getBlockDataManager().setData(block, "direction", direction, () -> {});

//        System.out.println("raw dir = " + direction);
        System.out.println("Direction = " + axis[direction]);

        System.out.println("damages = " + Arrays.deepToString(damages));

//        System.out.println();
        switch (axis[direction].getOppositeFace()) {
            case NORTH:
                System.out.println("NORTH");
                setDamages(block, damages[0]);
                break;
            case SOUTH:
                System.out.println("SOUTH");
                setDamages(block, damages[1]);
                break;
            case EAST:
                System.out.println("EAST");
                setDamages(block, damages[2]);
                break;
            case WEST:
                System.out.println("WEST");
                setDamages(block, damages[3]);
                break;
        }

//        System.out.println(Arrays.toString(getBlockDamages(block)));

        getBlockDamages(block, damages -> {
            setTypeTo(block, damages[0]);
        });
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
