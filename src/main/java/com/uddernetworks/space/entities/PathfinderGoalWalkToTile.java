package com.uddernetworks.space.entities;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import org.bukkit.Location;

public class PathfinderGoalWalkToTile extends PathfinderGoal {
    private EntityInsentient entity;

    private PathEntity path;

    private Location toLocation;
    private Location fromLocation;

    public PathfinderGoalWalkToTile(EntityInsentient entitycreature, Location l) {
        this.entity = entitycreature;

        this.toLocation = l;
        this.fromLocation = new Location(toLocation.getWorld(), entitycreature.getChunkCoordinates().getX(), entitycreature.getChunkCoordinates().getY(), entitycreature.getChunkCoordinates().getZ());
    }

    @Override
    public boolean a() {
        this.entity.getNavigation();

        this.path = this.entity.getNavigation().a(toLocation.getX(), toLocation.getY(), toLocation.getZ());

        this.entity.getNavigation();

        if (this.path != null) {
            this.c();
        }

        return this.path != null;
    }

    @Override
    public void c() {
        this.entity.getNavigation().a(this.path, 1D);
        System.out.println(path);
    }

    // on end path goal
    @Override
    public void d() {

    }

    @Override
    public void e() {
        System.out.println("PathfinderGoalPersistantLookAtPlayer.e");
    }
}