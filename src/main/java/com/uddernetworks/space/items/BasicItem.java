package com.uddernetworks.space.items;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BasicItem extends CustomItem {

    public BasicItem(int id, Material material, int damage, String name) {
        super(id, material, (short) damage, name);
    }

    @Override
    void onClick(PlayerInteractEvent event) {

    }

    @Override
    void onDrop(PlayerDropItemEvent event) {

    }

    @Override
    void onClickEntity(PlayerInteractAtEntityEvent event) {

    }
}
