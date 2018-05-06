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
        guis.add(gui);

        Bukkit.getPluginManager().registerEvents(gui, main);

        return gui;
    }

    public CustomGUI getGUI(UUID uuid) {
        return guis.stream().filter(customGUI -> customGUI.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public void removeGUI(UUID uuid) {
        new ArrayList<>(guis).stream().filter(gui -> gui.getUUID().equals(uuid)).forEach(gui -> guis.remove(gui));
    }

    public void clearGUIs() {
        guis.clear();
    }

    public void saveInventories() {
        this.guis.forEach(gui -> {
            if (gui instanceof LiquidOxygenGeneratorGUI || gui instanceof AlloyMixerGUI) {
                main.getBlockDataManager().setData(gui.getParentBlock(), "inventoryContents", InventoryUtils.serializeInventory(gui.getInventory().getContents()), () -> {});
            }
        });
    }

}
