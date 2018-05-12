package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.UUID;

public class CryogenicContainerGUI extends CustomGUI {

    private ProgressBar progressBar;
    private ProgressBar liquidTypeStatus;
    private final double maxFills = 150;
    private double currentlyFilled = 0;
    private int liquidType = 0;
//    private double currentlyFilled = 0;

    public CryogenicContainerGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.CRYOGENIC_CONTAINER_MAIN);

        this.progressBar = main.getProgressBarManager().getProgressBar("CryogenicContainerBar");
        this.liquidTypeStatus = main.getProgressBarManager().getProgressBar("CryogenicStatus");

        addSlot(new PopulatedSlot(46, false, progressBar.getItemStack(0)));
        addSlot(new PopulatedSlot(47, false, liquidTypeStatus.getItemStack(0)));

        updateSlots();
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        super.setParentBlock(parentBlock);

        Bukkit.getPlayer("RubbaBoy").sendMessage("Parent");

        System.out.println("Set parent block................................");

        Debugger debugger = new Debugger();

        main.getBlockDataManager().increment(parentBlock, "cryogenicContainer", 0, newAmount -> {
            System.out.println("newAmount = " + newAmount);
            debugger.log("cryogenicContainer");

            setFills(newAmount);
            updateFills();
        });

        main.getBlockDataManager().increment(parentBlock, "liquidType", 0, liquidType -> {
            System.out.println("liquidType = " + liquidType);
            debugger.log("liquidType");

            this.liquidType = liquidType;
            setPacketItem(47, liquidTypeStatus.getItemStack(liquidType == 0 ? 0 : 100));
        });
    }

    public int getCurrentlyFilled() {
        return Double.valueOf(currentlyFilled).intValue();
    }

    public void setFills(int currentlyFilled) {
        this.currentlyFilled = currentlyFilled;
    }

    public void updateFills() {
        getInventory().setItem(46, progressBar.getItemStack(currentlyFilled / maxFills * 100D));
    }

    public void setLiquidType(int liquidType) {
        this.liquidType = liquidType;
        setPacketItem(47, liquidTypeStatus.getItemStack(liquidType == 0 ? 0 : 100));

        main.getBlockDataManager().setData(getParentBlock(), "liquidType", liquidType, () -> {});
    }

    public int getLiquidType() {
        return this.liquidType;
    }
}
