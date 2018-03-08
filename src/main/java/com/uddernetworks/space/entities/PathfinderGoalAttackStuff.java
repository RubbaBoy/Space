package com.uddernetworks.space.entities;

import net.minecraft.server.v1_12_R1.PathfinderGoalMeleeAttack;

public class PathfinderGoalAttackStuff extends PathfinderGoalMeleeAttack {
    private final CustomEntityTest h;
    private int i;

    public PathfinderGoalAttackStuff(CustomEntityTest var1, double var2, boolean var4) {
        super(var1, var2, var4);
        this.h = var1;
    }

    public void c() {
        super.c();
        this.i = 0;
    }

    public void d() {
        super.d();
        this.h.a(false);
    }

    public void e() {
        super.e();
        ++this.i;
        if (this.i >= 5 && this.c < 10) {
            this.h.a(true);
        } else {
            this.h.a(false);
        }

    }
}
