package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class BlockLoadInitializer implements Listener {

    private Main main;

    public BlockLoadInitializer(Main main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {

    }
}
