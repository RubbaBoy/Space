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
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();

        main.getBlockDataManager().getCustomBlockCache().entrySet()
                .parallelStream()
                .filter(entry ->
                        entry.getKey().getX() >> 4 == chunkX
                                && entry.getKey().getZ() >> 4 == chunkZ)
                .filter(entry -> entry.getValue().hasGUI())
                .forEach((entry -> entry.getValue().getGUI(entry.getKey(), null)));
    }
}
