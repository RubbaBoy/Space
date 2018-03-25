package com.uddernetworks.space.meta;

import com.uddernetworks.space.main.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnhancedMetadataManager implements Listener {

    private Main main;
    public final EnhancedMetadata EMPTY = new EnhancedMetadata();
    private Map<PersistantBlock, EnhancedMetadata> metadataHashMap = new HashMap<>();

    public EnhancedMetadataManager(Main main) {
        this.main = main;
    }

    public EnhancedMetadata getMetadata(Block block) {
        return getMetadata(block, true);
    }

    public EnhancedMetadata getMetadata(Block block, boolean defaultMutable) {
        PersistantBlock currentBlock = new PersistantBlock(block);
        System.out.println("defaultMutable = " + defaultMutable);
        if (defaultMutable) {
            EnhancedMetadata enhancedMetadata = getRaw(block, null);

            System.out.println("enhancedMetadata = " + enhancedMetadata);

            if (enhancedMetadata == null) {
                enhancedMetadata = new EnhancedMetadata();
                this.metadataHashMap.put(currentBlock, enhancedMetadata);
                return enhancedMetadata;
            } else {
                return enhancedMetadata;
            }
        } else {
            return getRaw(block);
        }
    }

    private EnhancedMetadata getRaw(Block block) {
        return getRaw(block, EMPTY);
    }

    private EnhancedMetadata getRaw(Block block, EnhancedMetadata def) {
        for (PersistantBlock persistantBlock : this.metadataHashMap.keySet()) {
            if (persistantBlock.equals(block)) {
                System.out.println("Retting not default");
                return this.metadataHashMap.get(persistantBlock);
            }
        }

        System.out.println("Retting default");
        return def;
    }

    public boolean containsBlock(Block block) {
        for (PersistantBlock persistantBlock : this.metadataHashMap.keySet()) {
            if (persistantBlock.equals(block)) return true;
        }

        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        PersistantBlock persistantBlock = new PersistantBlock(block);

        metadataHashMap.remove(persistantBlock);
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent event) {
        getMetadatad(event.getBlocks()).forEach(persistantBlock -> {
            Location location = persistantBlock.getLocation();
            location.add(event.getDirection().getModX(), event.getDirection().getModY(), event.getDirection().getModZ());
            persistantBlock.setLocation(location);
        });
    }

    private List<PersistantBlock> getMetadatad(List<Block> blockList) {
        return blockList
                .stream()
                .flatMap(block -> metadataHashMap.keySet()
                        .stream()
                        .filter(persistantBlock -> persistantBlock.equals(block)))
                .collect(Collectors.toList());
    }

}
