package com.uddernetworks.space.items;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;

public class CustomIDManager {

    private Main main;

    public CustomIDManager(Main main) {
        this.main = main;
    }

    public IDHolder getByID(int id) {
        return this.main.getCustomItemManager().getCustomItems().stream()
                .map(IDHolder.class::cast)
                .filter(customItem -> id == customItem.getID())
                .findFirst()
                .orElse(this.main.getCustomBlockManager().getCustomBlocks().stream()
                        .filter(customBlock -> customBlock.getID() == id)
                        .findFirst()
                        .orElse(null));
    }

    public CustomItem getCustomItemById(int id) {
        return this.main.getCustomItemManager().getCustomItems().stream()
                .filter(customItem -> id == customItem.getID())
                .findFirst().orElse(null);
    }

    public CustomBlock getCustomBlockById(int id) {
        return this.main.getCustomBlockManager().getCustomBlocks().stream()
                .filter(customItem -> id == customItem.getID())
                .findFirst().orElse(null);
    }

}
