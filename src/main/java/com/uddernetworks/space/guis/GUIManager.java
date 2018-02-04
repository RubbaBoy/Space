package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private Main main;
    private List<CustomGUI> guis = new ArrayList<>();

    public GUIManager(Main main) {
        this.main = main;
    }

    public void addGUI(CustomGUI gui) {
        if (!containsGUI(gui.getClass())) {
            guis.add(gui);

            System.out.println("Registering events for: " + gui);
            Bukkit.getPluginManager().registerEvents(gui, main);
        }
    }

    public CustomGUI getGUI(Class<? extends CustomGUI> gui) {
        if (gui == null) return null;
        for (CustomGUI customGUI : guis) {
            if (customGUI.getClass().equals(gui)) return customGUI;
        }

        return null;
    }

    public boolean containsGUI(Class<? extends CustomGUI> gui) {
        for (CustomGUI customGUI : guis) {
            if (customGUI.getClass().equals(gui)) return true;
        }

        return false;
    }

}
