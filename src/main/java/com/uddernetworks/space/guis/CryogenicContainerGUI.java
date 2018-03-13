package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import org.bukkit.block.Block;

import java.util.UUID;

public class CryogenicContainerGUI extends CustomGUI {

    private ProgressBar progressBar;
    private final double maxFills = 150;
    private double currentlyFilled = 0;

    public CryogenicContainerGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.CRYOGENIC_CONTAINER_MAIN);

        this.progressBar = main.getProgressBarManager().getProgressBar("CryogenicContainerBar");

        addSlot(new PopulatedSlot(46, false, progressBar.getItemStack(0)));

        updateSlots();
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        super.setParentBlock(parentBlock);

        System.out.println("PARENT = " + getParentBlock());
        main.getBlockDataManager().increment(getParentBlock(), "cryogenicContainer", 0, newAmount -> {
            setFills(newAmount);
            updateFills();
        });
    }

    public void setFills(int currentlyFilled) {
        this.currentlyFilled = currentlyFilled;
    }

    public void updateFills() {
        getInventory().setItem(46, progressBar.getItemStack(currentlyFilled / maxFills * 100D));
    }
}
