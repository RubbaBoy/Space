package com.uddernetworks.space.entities;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftZombie;

public abstract class BaseEntity {

    private CraftArmorStand baseArmorStand;
    private CraftArmorStand lowerArmorStand;
    private CraftZombie hitboxZombie;

    public BaseEntity() {

    }

}
