package com.uddernetworks.space.blocks;

import net.minecraft.server.v1_12_R1.*;

public enum Tool {
    PICKAXE(ItemPickaxe.class),
    SHOVEL(ItemSpade.class),
    AXE(ItemAxe.class);
//    HOE(ItemHoe.class),
//    SHEARS,
//    SWORD(ItemSword.class);

    private Class<? extends ItemTool> toolClass;

    Tool(Class<? extends ItemTool> clazz) {
        this.toolClass = toolClass;

        ItemTool tool = null;

    }

    public Class<? extends ItemTool> getToolClass() {
        return toolClass;
    }
}
