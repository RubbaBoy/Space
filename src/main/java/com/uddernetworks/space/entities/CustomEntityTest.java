package com.uddernetworks.space.entities;

import com.google.common.collect.ImmutableMap;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import com.uddernetworks.space.utils.ItemBuilder;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class CustomEntityTest extends EntityZombie {

    private Main main;
    private CraftArmorStand armorStand;
    private CraftArmorStand armorStandPassenger1;

    public CustomEntityTest(World world) {
        super(world);
    }

    public CustomEntityTest(Main main, Location location) {
        super(((CraftWorld) location.getWorld()).getHandle());
        this.main = main;

        this.setPosition(location.getX(), location.getY(), location.getZ());

        armorStand = (CraftArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStandPassenger1 = (CraftArmorStand) location.getWorld().spawnEntity(location.subtract(0, 0.6875, 0), EntityType.ARMOR_STAND);

        armorStand.setGravity(false);
        armorStandPassenger1.setGravity(false);

        armorStandPassenger1.setMarker(true);

        org.bukkit.inventory.ItemStack arm = ItemBuilder.from(org.bukkit.Material.DIAMOND_HOE).setDamage(1001).setUnbreakable(true).build();
        org.bukkit.inventory.ItemStack leftLeg = ItemBuilder.from(org.bukkit.Material.DIAMOND_HOE).setDamage(1002).setUnbreakable(true).build();
        org.bukkit.inventory.ItemStack rightLeg = ItemBuilder.from(org.bukkit.Material.DIAMOND_HOE).setDamage(1003).setUnbreakable(true).build();
        org.bukkit.inventory.ItemStack body = ItemBuilder.from(org.bukkit.Material.DIAMOND_HOE).setDamage(1004).setUnbreakable(true).build();
        ItemStack head = ItemBuilder.from(org.bukkit.Material.DIAMOND_HOE).setDamage(1005).setUnbreakable(true).build();

        armorStand.setArms(true);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);


        armorStandPassenger1.setArms(true);
        armorStandPassenger1.setVisible(false);
        armorStandPassenger1.setBasePlate(false);

        armorStandPassenger1.setLeftLegPose(new EulerAngle(toRadians(180), 0, 0));
        armorStandPassenger1.setRightLegPose(new EulerAngle(toRadians(180), 0, 0));

        armorStand.getEquipment().setItemInMainHand(arm);
        armorStand.getEquipment().setItemInOffHand(arm);
        armorStand.getEquipment().setHelmet(head);

        armorStand.setLeftArmPose(new EulerAngle(toRadians(270), 0, 0));
        armorStand.setRightArmPose(new EulerAngle(toRadians(270), 0, 0));

        armorStandPassenger1.setLeftArmPose(new EulerAngle(toRadians(25), 0, 0));
        armorStandPassenger1.setRightArmPose(new EulerAngle(toRadians(-25), 0, 0));

        armorStandPassenger1.getEquipment().setItemInOffHand(leftLeg);
        armorStandPassenger1.getEquipment().setItemInMainHand(rightLeg);
        armorStandPassenger1.getEquipment().setHelmet(body);

        new FastTask(main).runRepeatingTask(true, () -> {
            if (this.moving) {
                legMovement += adding;

                if ((adding > 0 && Math.abs(legMovement) > 25) || (adding < 0 && Math.abs(legMovement) > 25)) {
                    adding *= -1;
                    legMovement += adding;
                }
            } else {
                legMovement = 0;
            }

            if (this.moving || !this.sendStoppedPacket) {
                sendStoppedPacket = true;
                EntityArmorStand entityArmorStand = armorStandPassenger1.getHandle();

                /*
                    b   headPose
                    c   bodyPose
                    d   leftArmPose
                    e   rightArmPose
                    f   leftLegPose
                    g   rightLegPose
                */

                PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(),
                        new DataWriterInjector(ImmutableMap.builder()
                                .put(14, new DataWatcher.Item<>(new DataWatcherObject<>(14, EntityArmorStand.d.b()), new Vector3f(legMovement, 0.0F, 0.0F)))
                                .put(15, new DataWatcher.Item<>(new DataWatcherObject<>(15, EntityArmorStand.e.b()), new Vector3f(-legMovement, 0.0F, 0.0F))).build()), true);

                Bukkit.getOnlinePlayers().forEach(forPlayer -> ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata));
            }
        }, 0, 0.01);
    }

    public void runAttackAnimation() {
        new FastTask(main).runRepeatingTask(true, () -> {

        }, 0, 0.01);
    }

    private int legMovement = 0;

    private int adding = 1;

    private boolean sendStoppedPacket = false;
    private boolean moving = false;

    public void setStartPath() {
        moving = true;
    }

    public void setEndPath() {
        moving = false;
        sendStoppedPacket = false;
    }

    // Tick
    @Override
    public void Y() {
        CraftEntity craftEntity = getBukkitEntity();
        Location location = craftEntity.getLocation();

        armorStand.teleport(location);
        armorStandPassenger1.teleport(location.clone().subtract(0, 0.6875, 0));

//        CraftEntity craftEntity = getBukkitEntity();
//        Location entityLocation = craftEntity.getLocation();
        this.armorStand.setHeadPose(new EulerAngle(toRadians(location.getPitch()), 0, 0));
        super.Y();
    }

    // Register goals
    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalAttackStuff(this, 1, false));
//        this.goalSelector.a(0, new PathfinderGoalAttackStuff<EntityHuman>(this, EntityHuman.class, false));
//        this.goalSelector.a(0, new PathfinderGoal(this, EntityHuman.class, true));
//        this.goalSelector.a(0, new PathfinderGoalWalkToTile(this, new Location(world.getWorld(), 47.5, 91, 311.5)));
//        this.goalSelector.a(0, new PathfinderGoalPersistantLookAtPlayer(this, new Location(world.getWorld(), 47.5, 91, 311.5), 10D));
//        this.goalSelector.a(8, new PathfinderGoalPersistantLookAtPlayer(this, EntityHuman.class, 20.0F));

        this.goalSelector.a(1, new PathfinderGoalFloat(this));
//        this.goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false));
//        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
//        this.goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.do_();
    }

    @Override
    public boolean B(Entity entity) {
        Bukkit.getPlayer("RubbaBoy").sendMessage("Attacked player: " + entity);



        return super.B(entity);
    }


    private double toRadians(float degrese) {
        return Math.toRadians(degrese % 360);
    }


    private class DataWriterInjector extends DataWatcher {
        private ImmutableMap<Object, Object> data;

        public DataWriterInjector(ImmutableMap<Object, Object> data) {
            super(null);
            this.data = data;
        }

        @Override
        public List<DataWatcher.Item<?>> c() {
            List<DataWatcher.Item<?>> list = new ArrayList<>();

            for (Object value : data.values()) {
                list.add((Item<?>) value);
            }

            return list;
        }

        @Override
        public void e() {}
    }
}