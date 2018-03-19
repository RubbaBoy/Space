package com.uddernetworks.space.command;

import com.mojang.authlib.GameProfile;
import com.uddernetworks.command.Argument;
import com.uddernetworks.command.ArgumentError;
import com.uddernetworks.command.ArgumentList;
import com.uddernetworks.command.Command;
import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.entities.CustomEntityTest;
import com.uddernetworks.space.items.CustomItem;
import com.uddernetworks.space.items.IDHolder;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.nbt.NBTItem;
import com.uddernetworks.space.utils.ItemBuilder;
import net.minecraft.server.v1_12_R1.BlockStepAbstract;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Command(name = "rocket", consoleAllow = false, minArgs = 1, maxArgs = 4)
public class RocketCommand {

    private Main main;
    private ArmorStand armorStand;

    public RocketCommand(Main main) {
        Random random = new Random();
        this.main = main;
        Bukkit.getScheduler().runTaskTimer(main, () -> {
            Player player = Bukkit.getPlayer("RubbaBoy");
            if (player == null) return;

            if (armorStand != null && !armorStand.isDead()) {
//                System.out.println("Changing!");
//                armorStand.setHeadPose(armorStand.getHeadPose()
//                        .setY(toRadians(player.getLocation().getYaw() - 90)));

                ItemStack item = armorStand.getHelmet();

                short data = (short) (random.nextInt(3) + 1);

                if (item.getDurability() == data) {
                    if (data == 1) {
                        data = 2;
                    } else if (data == 2) {
                        data = 3;
                    } else if (data == 3) {
                        data = 1;
                    }
                }

//                System.out.println("Setting data to " + data);

                item.setDurability(data);

                armorStand.setHelmet(item);
            }

        }, 0, 20 / 5); // 5 times per second
    }

    @Argument(format = "launch")
    public void start(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;

        this.armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.ARMOR_STAND);

        armorStand.setGravity(true);
        armorStand.setPassenger(player);
        armorStand.setVisible(false);
        armorStand.setHelmet(new ItemStack(Material.WOOD_HOE, 1, (short) 0));

        armorStand.setHeadPose(armorStand.getHeadPose()
                .setX(toRadians(0F))
                .setY(toRadians(0F))
                .setZ(toRadians(0F)));

        /*
        ~~make launchpad~~
        ~~start GUI~~
        crafting shit
        moon effects (breathing and stuff)
        flying ship

        ~~new blocks with SimplySarc's video with mobspawners and stuff~~


        custom entities with textured (with durabilities) items on an armor stand, OR
                            - custom modeled/textured armor on an existing mob, determined by armor data value
         */

    }

    @Argument(format = "give * *")
    public void give(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        int id = args.nextArg().getInt();
        int amount = args.nextArg().getInt();

        IDHolder idHolder = main.getCustomIDManager().getByID(id);

        if (idHolder == null) {
            player.sendMessage(ChatColor.RED + "No item/block found with the given ID.");
            return;
        }

        ItemStack itemStack = idHolder.toItemStack();
        itemStack.setAmount(amount);

        player.getWorld().dropItem(player.getLocation(), itemStack);

        player.sendMessage(ChatColor.GOLD + "Gave you " + ChatColor.RED + "x" + amount + ChatColor.GOLD + " of " + ChatColor.RED + idHolder.getID());
    }

    @Argument(format = "blocks")
    public void blocks(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        for (CustomBlock customBlock : main.getCustomBlockManager().getCustomBlocks()) {
//            player.getInventory().setItemInMainHand(new ItemStack(customBlock.getMaterial(), customBlock.getDamage()));
            player.getWorld().dropItem(player.getLocation(), customBlock.toItemStack());
        }
    }

    @Argument(format = "items")
    public void items(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        for (CustomItem customItem : main.getCustomItemManager().getCustomItems()) {
            player.getWorld().dropItem(player.getLocation(), customItem.toItemStack());
        }
    }

    @Argument(format = "stuff")
    public void stuff(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;




    }


    @Argument(format = "hat")
    public void hat(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.getInventory().setHelmet(player.getInventory().getItemInMainHand());
    }

    @Argument(format = "get *")
    public void get(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.getInventory().setItemInMainHand(ItemBuilder.from(Material.DIAMOND_HOE).setDamage(args.nextArg().getInt()).setUnbreakable(true).build());
    }

    @Argument(format = "spawn")
    public void spawn(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;

        CustomEntityTest customEntityTest = new CustomEntityTest(main, player.getLocation());
        ((CraftWorld) player.getWorld()).getHandle().addEntity(customEntityTest, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Argument(format = "test")
    public void test(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;


        CraftArmorStand armorStand = (CraftArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        CraftArmorStand armorStandPassenger1 = (CraftArmorStand) player.getWorld().spawnEntity(player.getLocation().subtract(0, 0.6875, 0), EntityType.ARMOR_STAND);
//        CraftArmorStand armorStandPassenger2 = (CraftArmorStand) player.getWorld().spawnEntity(player.getLocation().subtract(0, 0.6, 0), EntityType.ARMOR_STAND);

        armorStand.setGravity(false);
        armorStandPassenger1.setGravity(false);

        armorStandPassenger1.setMarker(true);
//        armorStandPassenger2.setGravity(false);

//        armorStand.addPassenger(armorStandPassenger1);
//        armorStand.addPassenger(armorStandPassenger2);

        ItemStack arm = ItemBuilder.from(Material.DIAMOND_HOE).setDamage(1001).setUnbreakable(true).build();
        ItemStack leftLeg = ItemBuilder.from(Material.DIAMOND_HOE).setDamage(1002).setUnbreakable(true).build();
        ItemStack rightLeg = ItemBuilder.from(Material.DIAMOND_HOE).setDamage(1003).setUnbreakable(true).build();
        ItemStack body = ItemBuilder.from(Material.DIAMOND_HOE).setDamage(1004).setUnbreakable(true).build();
        ItemStack head = ItemBuilder.from(Material.DIAMOND_HOE).setDamage(1005).setUnbreakable(true).build();

        armorStand.setArms(true);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);


        armorStandPassenger1.setArms(true);
        armorStandPassenger1.setVisible(false);
        armorStandPassenger1.setBasePlate(false);
//        armorStandPassenger2.setArms(true);

        armorStand.getEquipment().setItemInMainHand(arm);
        armorStand.getEquipment().setItemInOffHand(arm);
        armorStand.getEquipment().setHelmet(head);

        armorStand.setLeftArmPose(new EulerAngle(toRadians(90), 0, 0));
        armorStand.setRightArmPose(new EulerAngle(toRadians(90), 0, 0));

        armorStandPassenger1.setLeftArmPose(new EulerAngle(0, 0, 0));
        armorStandPassenger1.setRightArmPose(new EulerAngle(0, 0, 0));

        armorStandPassenger1.getEquipment().setItemInOffHand(leftLeg);
        armorStandPassenger1.getEquipment().setItemInMainHand(rightLeg);
        armorStandPassenger1.getEquipment().setHelmet(body);

//        armorStandPassenger1.getEquipment().setItemInMainHand(arm);
//        armorStandPassenger1.getEquipment().setItemInOffHand(arm);
    }

    @Argument(format = "shit")
    public void shit(CommandSender sender, ArgumentList args) throws NoSuchFieldException, IllegalAccessException {
        Player player = (Player) sender;

        CraftPlayer craftPlayer = ((CraftPlayer) player);
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        Field field = EntityHuman.class.getDeclaredField("g");
        field.setAccessible(true);

        GameProfile gameProfile = (GameProfile) field.get(entityPlayer);

        Field name = GameProfile.class.getDeclaredField("name");
        name.setAccessible(true);

        name.set(gameProfile, "deadmau5");

        craftPlayer.setDisplayName("RubbaBoy");


        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam("Test");

        team.addPlayer(player);

        team.setDisplayName("RubbaBoy");

    }

    private double toRadians(float degrese) {
        return Math.toRadians(degrese % 360);
    }

    @ArgumentError
    public void argumentError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + "Error while executing command: " + message);
    }

}
