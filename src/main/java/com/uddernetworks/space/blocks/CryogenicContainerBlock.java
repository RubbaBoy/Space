package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CryogenicContainerGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.nbt.NBTItem;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class CryogenicContainerBlock extends CustomBlock {

    public CryogenicContainerBlock(Main main, int id, Material material, int damage, Material particle, String name) {
        super(main, id, material, damage, false, particle, name, () -> main.getGUIManager().addGUI(new CryogenicContainerGUI(main, "Cryogenic Container", 54, UUID.randomUUID())));
    }

    @Override
    public boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    public boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
//        blockPrePlace.setUsingCallback(true);

        int cryogenicContainer = 0;
        int liquidType = 0; // 0 for oxygen, 1 for hydrogen

        NBTTagCompound tag = new NBTItem(blockPrePlace.getItemStack()).getTag();
        System.out.println("tag = " + tag);
        for (String key : tag.c()) {
            System.out.println("key = " + key);
        }

        if (tag.hasKey("cryogenicContainer")) cryogenicContainer = tag.getInt("cryogenicContainer");
        if (tag.hasKey("liquidType")) liquidType = tag.getInt("liquidType");

        System.out.println("cryogenicContainer = " + cryogenicContainer);
        System.out.println("liquidType = " + liquidType);

        int finalLiquidType = liquidType;
        main.getBlockDataManager().setData(block, "cryogenicContainer", cryogenicContainer, () -> {
            main.getBlockDataManager().setData(block, "liquidType", finalLiquidType, () -> {
//            getGUI(block, customGUI -> blockPrePlace.getCallback().run());
//            getGUI(block, customGUI -> {});
//            blockPrePlace.getCallback().run();
            });
        });

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
        return true;
    }

    @Override
    public void toItemStack(Block blockInstance, Consumer<ItemStack> itemStackConsumer) {
        getGUI(blockInstance, customGUI -> {
            CryogenicContainerGUI cryogenicContainerGUI = (CryogenicContainerGUI) customGUI;

            ItemStack itemStack = toItemStack();

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.getTag().setInt("cryogenicContainer", cryogenicContainerGUI.getCurrentlyFilled());
            nbtItem.getTag().setInt("liquidType", cryogenicContainerGUI.getLiquidType());
            // TODO: Add 'type' variable for oxygen/hydrogen
            itemStack = nbtItem.toItemStack();

            itemStackConsumer.accept(itemStack);
        });
    }

    public void addFill(Block block) {
        main.getBlockDataManager().increment(block, "cryogenicContainer", 1, newValue -> getGUI(block, customGUI -> {
            CryogenicContainerGUI cryogenicContainerGUI = (CryogenicContainerGUI) customGUI;
            cryogenicContainerGUI.setFills(newValue);
            cryogenicContainerGUI.updateFills();
        }));
    }
}
