package com.uddernetworks.space.entities;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class CustomEntityTest extends EntityZombie {

    public CustomEntityTest(World world) {
        super(world);
    }

    public CustomEntityTest(Location loc) {
        super(((CraftWorld) loc.getWorld()).getHandle());
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
    }

    public void moveToLocation() {
//        this.getNavigation().a(47.5, 91, 311.5, 0);
    }

    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalAttackStuff(this, 1, false));
//        this.goalSelector.a(0, new PathfinderGoalAttackStuff<EntityHuman>(this, EntityHuman.class, false));
//        this.goalSelector.a(0, new PathfinderGoal(this, EntityHuman.class, true));
//        this.goalSelector.a(0, new PathfinderGoalWalkToTile(this, new Location(world.getWorld(), 47.5, 91, 311.5)));
//        this.goalSelector.a(0, new PathfinderGoalPersistantLookAtPlayer(this, new Location(world.getWorld(), 47.5, 91, 311.5), 10D));
//        this.goalSelector.a(8, new PathfinderGoalPersistantLookAtPlayer(this, EntityHuman.class, 20.0F));

//        this.goalSelector.a(0, new PathfinderGoalFloat(this));
//        this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
//        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
//        this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
//        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
//        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.do_();
    }
}