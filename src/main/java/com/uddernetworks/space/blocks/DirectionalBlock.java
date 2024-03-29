package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DirectionalBlock extends AnimatedBlock {

    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public DirectionalBlock(Main main, int id, Material material, short[] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages, particle, name, customGUISupplier);
    }

    public DirectionalBlock(Main main, int id, Material material, short[][] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages, particle, name, customGUISupplier);
    }

    @Override
    public boolean onBreak(Block block, Player player) {
        return super.onBreak(block, player);
    }

    @Override
    public boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
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

        CustomGUI customGUI = getCustomGUISupplier() == null ? null : main.getGUIManager().addGUI(getCustomGUISupplier().get());
        if (customGUI != null) {
            customGUI.setParentBlock(block);
            main.getBlockDataManager().setData(block, "inventoryID", customGUI.getUUID(), () -> {});
        }

        main.getBlockDataManager().setData(block, "direction", index, () -> {});

//        startAnimation(block);

        blockPrePlace.setUsingCallback(true);

        setTypeTo(block, damages[index][0]);
        blockPrePlace.setDamage(damages[index][0]);
        blockPrePlace.getCallback().run();

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
        return getCustomGUISupplier() != null;
    }

    @Override
    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        main.getBlockDataManager().getData(blockInstance, "inventoryID", inventoryID -> {
            if (inventoryID == null || main.getGUIManager().getGUI(UUID.fromString(inventoryID)) == null) {
                CustomGUI customGUI = getCustomGUISupplier() == null ? null : main.getGUIManager().addGUI(getCustomGUISupplier().get());
                if (customGUI == null) return;
                customGUI.setParentBlock(blockInstance);
                main.getBlockDataManager().setData(blockInstance, "inventoryID", customGUI.getUUID(), () -> {
                    main.getBlockDataManager().getData(blockInstance, "direction", data -> {
                        int direction = data == null ? 0 : Integer.valueOf(data);
                        setDamages(blockInstance, damages[direction]);
//                        startAnimation(blockInstance);

//                        setTypeTo(blockInstance, damages[direction][0]); // TODO: EXPERIMENTAL
//                        if (isElectrical()) main.getCircuitMapManager().addBlock(blockInstance);
                        if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
                    });
                });
            } else {
                CustomGUI customGUI = main.getGUIManager().getGUI(UUID.fromString(inventoryID));

                main.getBlockDataManager().getData(blockInstance, "direction", data -> {
                    int direction = data == null ? 0 : Integer.valueOf(data);
                    setDamages(blockInstance, damages[direction]);
//                    setTypeTo(blockInstance, damages[direction][0]); // TODO: EXPERIMENTAL

                    if (customGUI != null) customGUI.setParentBlock(blockInstance);
                    if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
                });
            }
        });
    }
}
