package com.uddernetworks.space.entities;

import com.google.common.collect.ImmutableMap;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.Reflect;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.stream.Collectors;

public class CustomEntityTest extends EntityZombie {

    private CraftArmorStand armorStand;
    private CraftArmorStand armorStandPassenger1;

    public CustomEntityTest(World world) {
        super(world);
    }

    public CustomEntityTest(Main main, Location location) {
        super(((CraftWorld) location.getWorld()).getHandle());
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


        legMovement += adding;

        new FastTask(main).runRepeatingTask(true, () -> {
//        Bukkit.getScheduler().runTaskTimer(main, () -> {
            try {
//                System.out.println("tttttttttttttttttttttttttttttttttttttttttttttttttt::::::  " + adding);

                legMovement += adding;

                if ((adding > 0 && Math.abs(legMovement) > 25) || (adding < 0 && Math.abs(legMovement) > 25)) {
                    adding *= -1;
                    legMovement += adding;
                }

//                System.out.println("legMovement = " + legMovement + " adding = " + adding);

                EntityArmorStand entityArmorStand = ((CraftArmorStand) armorStandPassenger1).getHandle();

//            getDataWatcher().set(entityArmorStand.b, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));
//            getDataWatcher().set(entityArmorStand.c, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));
//            getDataWatcher().set(entityArmorStand.d, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));
//            getDataWatcher().set(entityArmorStand.e, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));
//                getDataWatcher().set(entityArmorStand.f, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));
//            getDataWatcher().set(entityArmorStand.g, new Vector3f(Double.valueOf(toRadians(legMovement)).floatValue(), 0, 0));

//                DataWatcher dataWatcher = entityArmorStand.getDataWatcher();

//                Map<Integer, DataWatcher.Item<?>> d = (Map<Integer, DataWatcher.Item<?>>) Reflect.getField(dataWatcher, "d", false);

//                d.put(14, new DataWatcher.Item<>(new DataWatcherObject<>(14, EntityArmorStand.d.b()), new Vector3f(legMovement, 0.0F, 0.0F))); //    Left
//                d.put(15, new DataWatcher.Item<>(new DataWatcherObject<>(15, null), new Vector3f(-legMovement, 0.0F, 0.0F))); //   Right

                PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(),
                        new DataWriterInjector(ImmutableMap.builder()
                                .put(14, new DataWatcher.Item<>(new DataWatcherObject<>(14, EntityArmorStand.d.b()), new Vector3f(legMovement, 0.0F, 0.0F)))
                                .put(15, new DataWatcher.Item<>(new DataWatcherObject<>(15, EntityArmorStand.d.b()), new Vector3f(-legMovement, 0.0F, 0.0F))).build()), true);
                Bukkit.getOnlinePlayers().forEach(forPlayer -> ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(packetPlayOutEntityMetadata));
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }, 30L, 10L);


        }, 0, 0.02);
    }

    private int legMovement = 0;

    private int adding = 2;

    @Override
    public void Y() {
        Location location = getBukkitEntity().getLocation().add(0, 0, 0);

        armorStand.teleport(location);
        armorStandPassenger1.teleport(location.clone().subtract(0, 0.6875, 0));

//        armorStandPassenger1.setLeftArmPose(new EulerAngle(toRadians(legMovement), 0, 0));
//        armorStandPassenger1.setRightArmPose(new EulerAngle(toRadians(-legMovement), 0, 0));

        /*
        b   headPose
        c   bodyPose
        d   leftArmPose
        e   rightArmPose
        f   leftLegPose
        g   rightLegPose
         */


//        PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(armorStandPassenger1.getEntityId(), new DataWatcher(), )

        super.Y();
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
        public void e() {
        }
    }
}