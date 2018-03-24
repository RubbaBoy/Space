package com.uddernetworks.space.meta;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class PersistantBlock {

    private Material material;
    private byte data;
    private Location location;

    public PersistantBlock(Block block) {
        this.material = block.getType();
        this.data = block.getData();
        this.location = block.getLocation();
    }

    public PersistantBlock(Material material, byte data, Location location) {
        this.material = material;
        this.data = data;
        this.location = location;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public byte getData() {
        return data;
    }

    public void setData(byte data) {
        this.data = data;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean equals(Block block) {
        return this.material == block.getType() && this.data == block.getData() && this.location.equals(block.getLocation());
    }

    public boolean equals(PersistantBlock persistantBlock) {
        return this.material == persistantBlock.material && this.data == persistantBlock.data && this.location.equals(persistantBlock.getLocation());
    }
}
