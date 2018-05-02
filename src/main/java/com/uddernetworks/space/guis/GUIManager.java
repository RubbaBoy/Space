package com.uddernetworks.space.guis;

import com.uddernetworks.space.main.Main;
import net.minecraft.server.v1_12_R1.PacketPlayOutWindowData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
//        System.out.println("uuid = " + uuid);
//        for (CustomGUI gui : guis) {
//            System.out.println("gui.getUUID() = " + gui.getUUID());
//            if (gui.getUUID().equals(uuid)) return gui;
//        }
//
//        return null;
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
                main.getBlockDataManager().setData(gui.getParentBlock(), "inventoryContents", InventoryUtils.serializeInventory(gui.getInventory().getContents()), () -> {
                    System.out.println("Saved GUI: " + gui.getUUID());
                });
            }
        });
    }

}
