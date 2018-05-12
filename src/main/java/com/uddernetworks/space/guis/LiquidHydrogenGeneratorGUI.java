package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;

import java.util.UUID;

public class LiquidHydrogenGeneratorGUI extends LiquidGeneratorGUI{

    public LiquidHydrogenGeneratorGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, 1, uuid, GUIItems.LIQUID_HYDROGEN_GENERATOR_MAIN);
    }
}
