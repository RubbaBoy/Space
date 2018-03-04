package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;

import java.util.UUID;

public class CryogenicContainerGUI extends CustomGUI {

    private ProgressBar progressBar;
    private final double maxFills = 10;
    private double currentlyFilled = 3;

    public CryogenicContainerGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.CRYOGENIC_CONTAINER_MAIN);

        this.progressBar = main.getProgressBarManager().getProgressBar("CryogenicContainerBar");

        System.out.println("progressBar = " + progressBar);

        addSlot(new PopulatedSlot(46, false, progressBar.getItemStack(0)));

        updateSlots();
    }

    public void addFill() {
        if (currentlyFilled < maxFills) {
            currentlyFilled++;

            updateFills();
        }
    }

    public void updateFills() {
        getInventory().setItem(46, progressBar.getItemStack(currentlyFilled / maxFills * 100D));
    }
}
