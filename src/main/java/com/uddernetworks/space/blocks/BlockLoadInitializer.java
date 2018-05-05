package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.stream.Stream;

public class BlockLoadInitializer implements Listener {

    private Main main;

    public BlockLoadInitializer(Main main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        loadDataChunk(event.getChunk());
    }

    private void loadDataChunk(Chunk chunk) {
        System.out.println("chunk = " + chunk);
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        main.getBlockDataManager().getCustomBlockCache().entrySet()
                .parallelStream()
                .filter(entry ->
                        entry.getKey().getX() >> 4 == chunkX
                                && entry.getKey().getZ() >> 4 == chunkZ)
                .filter(entry -> entry.getValue().hasGUI())
                .forEach((entry -> entry.getValue().getGUI(entry.getKey(), null)));
    }

    public void init() {
        System.out.println(main.getBlockDataManager().getCustomBlockCache());
        Bukkit.getWorlds().stream().flatMap(world -> Stream.of(world.getLoadedChunks())).forEach(this::loadDataChunk);
    }
}
