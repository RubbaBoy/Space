package com.uddernetworks.space.entities;

import net.minecraft.server.v1_12_R1.PathfinderGoalMeleeAttack;

public class PathfinderGoalAttackStuff extends PathfinderGoalMeleeAttack {
    private final CustomEntityTest customEntityTest;
    private int i;

    public PathfinderGoalAttackStuff(CustomEntityTest customEntityTest, double speed, boolean var4) {
        super(customEntityTest, speed, var4);
        this.customEntityTest = customEntityTest;
    }

    public void c() {
        super.c();
        this.i = 0;

        this.customEntityTest.setStartPath();
    }

    public void d() {
        super.d();
        this.customEntityTest.a(false);

        this.customEntityTest.setEndPath();
    }

    public void e() {
        super.e();
        ++this.i;
        if (this.i >= 5 && this.c < 10) {
            this.customEntityTest.a(true);
        } else {
            this.customEntityTest.a(false);
        }

    }
}
