package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockLoadInitializer implements Listener {

    private Main main;

    public BlockLoadInitializer(Main main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        List<Map.Entry<Block, CustomBlock>> blocks = getBlocksInChunk(event.getChunk());

        loadGUIsForChunk(blocks);
        loadCircuitsForChunk(blocks);
    }

    private List<Map.Entry<Block, CustomBlock>> getBlocksInChunk(Chunk chunk) {
        System.out.println("chunk = " + chunk);
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        return main.getBlockDataManager().getCustomBlockCache().entrySet()
                .parallelStream()
                .filter(entry ->
                        entry.getKey().getX() >> 4 == chunkX
                                && entry.getKey().getZ() >> 4 == chunkZ).collect(Collectors.toList());
    }

    private void loadGUIsForChunk(List<Map.Entry<Block, CustomBlock>> blocks) {
        blocks.stream().filter(entry -> entry.getValue().hasGUI())
                .forEach(entry -> entry.getValue().getGUI(entry.getKey(), null));
    }

    private void loadCircuitsForChunk(List<Map.Entry<Block, CustomBlock>> blocks) {
        blocks.stream().filter(entry -> entry.getValue().isElectrical())
                .forEach(entry -> main.getCircuitMapManager().addBlock(entry.getKey()));
    }

    public void init() {
        List<List<Map.Entry<Block, CustomBlock>>> blocksList = new ArrayList<>();

        Bukkit.getWorlds().stream().flatMap(world -> Stream.of(world.getLoadedChunks())).forEach(chunk -> {
            List<Map.Entry<Block, CustomBlock>> blocks = getBlocksInChunk(chunk);

            blocksList.add(blocks);

            loadGUIsForChunk(blocks);
        });

        System.out.println("Finished GUIs");

        Bukkit.getScheduler().runTaskLater(main, () -> {
            System.out.println("Getting circuits...");
            blocksList.forEach(this::loadCircuitsForChunk);
        }, 40L);
    }
}
