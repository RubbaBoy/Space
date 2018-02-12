package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIManager {

    private Main main;
    private List<CustomGUI> guis = new ArrayList<>();

    public GUIManager(Main main) {
        this.main = main;
    }

    public CustomGUI addGUI(CustomGUI gui) {
//        if (!containsGUI(gui.getClass())) {
            guis.add(gui);

            System.out.println("Registering events for: " + gui);
            Bukkit.getPluginManager().registerEvents(gui, main);

            return gui;
//        }
    }

    public CustomGUI getGUI(UUID uuid) {
        return guis.stream().filter(customGUI -> customGUI.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public void removeGUI(UUID uuid) {
        new ArrayList<>(guis).stream().filter(gui -> gui.getUUID().equals(uuid)).forEach(guis::remove);
    }

    public void clearGUIs() {
        guis.clear();
    }


//    public int getNextId() {
//        return guis.size();
//    }

}