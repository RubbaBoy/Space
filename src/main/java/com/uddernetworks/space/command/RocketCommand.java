package com.uddernetworks.space.command;

import com.mojang.authlib.GameProfile;
import com.uddernetworks.command.Argument;
import com.uddernetworks.command.ArgumentError;
import com.uddernetworks.command.ArgumentList;
import com.uddernetworks.command.Command;
import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.items.CustomItem;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.nbt.NBTItem;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Command(name = "rocket", consoleAllow = false, minArgs = 1, maxArgs = 2)
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
//        player.getInventory().setHelmet(player.getInventory().getItemInMainHand());

        ItemStack item = main.getCustomItemManager().getCustomItem("Copper Ingot").toItemStack();

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.getTag().setInt("random", ThreadLocalRandom.current().nextInt());

        player.getInventory().setItemInMainHand(nbtItem.toItemStack());
    }


    @Argument(format = "hat")
    public void hat(CommandSender sender, ArgumentList args) {
        Player player = (Player) sender;
        player.getInventory().setHelmet(player.getInventory().getItemInMainHand());
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
