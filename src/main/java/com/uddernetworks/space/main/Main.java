package com.uddernetworks.space.main;

import com.google.common.collect.ImmutableMap;
import com.uddernetworks.command.CommandManager;
import com.uddernetworks.space.blocks.BasicBlock;
import com.uddernetworks.space.blocks.CustomBlockManager;
import com.uddernetworks.space.blocks.WorkbenchBlock;
import com.uddernetworks.space.command.RocketCommand;
import com.uddernetworks.space.command.SpaceCommand;
import com.uddernetworks.space.guis.GUIManager;
import com.uddernetworks.space.guis.Workbench;
import com.uddernetworks.space.items.BasicItem;
import com.uddernetworks.space.items.CustomItemManager;
import com.uddernetworks.space.recipies.Recipe;
import com.uddernetworks.space.recipies.RecipeManager;
import com.uddernetworks.space.stacker.ItemStacker;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.Reflect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.DIRT;

public class Main extends JavaPlugin implements Listener {

    private GUIManager guiManager;
    private RecipeManager recipeManager;
    private CustomItemManager customItemManager;
    private CustomBlockManager customBlockManager;

    private HashMap<UUID, Vector> velocities;
    private HashMap<UUID, Location> positions;
    private HashMap<UUID, Boolean> onGround;

    @Override
    public void onEnable() {
        CommandManager commandManager = new CommandManager();
        commandManager.registerCommand(this, new SpaceCommand());
        commandManager.registerCommand(this, new RocketCommand(this));

        Bukkit.getScheduler().runTaskTimer(this, this::updateVelocities, 0, 1L);
        this.velocities = new HashMap<>();
        this.onGround = new HashMap<>();
        this.positions = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);


        this.guiManager = new GUIManager(this);
        this.recipeManager = new RecipeManager(this);
        this.customItemManager = new CustomItemManager(this);
        this.customBlockManager = new CustomBlockManager(this);

        getServer().getPluginManager().registerEvents(this.customItemManager, this);
        getServer().getPluginManager().registerEvents(this.customBlockManager, this);
        getServer().getPluginManager().registerEvents(new ItemStacker(this), this);

        /* GUIs */

        this.guiManager.addGUI(new Workbench(this, "WorkbenchBlock", 54));

        /* Items */

        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 51, "Carbon"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 52, "Magnesium Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 53, "Raw Silicon"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 54, "Copper Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 55, "Aluminum Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 56, "IC"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 57, "CPU"));

        /* Blocks */

        this.customBlockManager.addCustomBlock(new WorkbenchBlock(Material.DIAMOND_HOE, 21, Material.WOOL, "Spaceship Workbench", Workbench.class));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 22, Material.STONE, "Carbon Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 23, Material.BLACK_SHULKER_BOX, "Carbon Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 24, Material.STONE, "Magnesium Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 25, Material.GRAY_SHULKER_BOX, "Magnesium Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 26, Material.STONE, "Silicon Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 27, Material.GRAY_SHULKER_BOX, "Silicon Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 28, Material.STONE, "Copper Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 29, Material.ORANGE_SHULKER_BOX, "Copper Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 30, Material.STONE, "Aluminum Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(Material.DIAMOND_HOE, 31, Material.WHITE_SHULKER_BOX, "Aluminum Block"));


        /* Recipes */

        this.recipeManager.addRecipe(new Recipe(new ItemStack[][] {
                {ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT)},
                {ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR)},
                {ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR)},
                {ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR)},
                {ItemBuilder.itemFrom(DIRT), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(AIR), ItemBuilder.itemFrom(DIRT)}
        }, ItemBuilder.create().setMaterial(Material.DIAMOND_BLOCK).setAmount(64).build(), true));

        this.recipeManager.addRecipe(new Recipe(new char[][]{
                {'D', ' ', ' ', ' ', 'D'},
                {' ', 'D', ' ', 'D', ' '},
                {' ', ' ', 'D', ' ', ' '},
                {' ', 'D', ' ', 'D', ' '},
                {'D', ' ', ' ', ' ', 'D'}
        }, ImmutableMap.of('D', ItemBuilder.itemFrom(Material.DIRT)), ItemBuilder.create().setMaterial(Material.DIAMOND_BLOCK).setAmount(64).build(), true));

        ShapedRecipe ic = new ShapedRecipe(new NamespacedKey(this, "Space"), this.customItemManager.getCustomItem("IC").toItemStack()); // IC
        ic.shape("   ", "CCC", "SSS");

        Map<Character, ItemStack> ingredients = (Map<Character, ItemStack>) Reflect.getField(ic, "ingredients", false);

        ingredients.put('C', this.customItemManager.getCustomItem("Copper Ingot").toItemStack());
        ingredients.put('S', this.customItemManager.getCustomItem("Raw Silicon").toItemStack());

        Reflect.setField(ic, "ingredients", ingredients, false);

        Bukkit.addRecipe(ic);
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    public CustomBlockManager getCustomBlockManager() {
        return customBlockManager;
    }

    public void updateVelocities() {
        World world = Bukkit.getServer().getWorld("moon");
        if (world == null) return;

        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.isFlying()) continue;
            }

            Vector newv = entity.getVelocity().clone();
            UUID uuid = entity.getUniqueId();
            if (this.velocities.containsKey(uuid) && this.onGround.containsKey(uuid) && !entity.isOnGround() && !entity.isInsideVehicle()) {
                Vector oldv = this.velocities.get(uuid);

                if (!this.onGround.get(uuid)) {
                    Vector d = oldv.clone();
                    d.subtract(newv);
                    double dy = d.getY();

                    if (dy > 0.0 && (newv.getY() < -0.01 || newv.getY() > 0.01)) {
                        Location loc = entity.getLocation().clone();
                        double gravity = 1.0;

                        while (loc.getBlockY() >= 0) {
                            final Block block = loc.getBlock();
                            if (block.getType() == Material.CONCRETE_POWDER) {
                                if (block.getData() == 8) {
                                    gravity = 0.2;
                                }
                            }

                            if (block.getType() != AIR) {
                                break;
                            }

                            loc.setY(loc.getY() - 1.0);
                        }

                        newv.setY(oldv.getY() - dy * gravity);


                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) entity;

                            Vector direction = livingEntity.getLocation().getDirection().normalize();

                            newv.setX(direction.getX() * gravity);
                            newv.setZ(direction.getZ() * gravity);

                        }

//                            newv.setX(oldv.getX());
//                            newv.setZ(oldv.getZ());

//                            boolean newxchanged = newv.getX() < -0.001 || newv.getX() > 0.001;
//                            boolean oldxchanged = oldv.getX() < -0.001 || oldv.getX() > 0.001;
//
//                            if (newxchanged && oldxchanged) {
//                                System.out.println("old = " + oldv.getX() + " new = " + newv.getX());
//                                newv.setX(oldv.getX());
//                            }
//
//                            boolean newzchanged = newv.getZ() < -0.001 || newv.getZ() > 0.001;
//                            boolean oldzchanged = oldv.getZ() < -0.001 || oldv.getZ() > 0.001;
//
//                            if (newzchanged && oldzchanged) {
//                                newv.setZ(oldv.getZ());
//                            }

                        entity.setVelocity(newv.clone());
                    }
                } else if (entity instanceof Player && this.positions.containsKey(uuid)) {
                    final Vector pos = entity.getLocation().toVector();
                    final Vector oldpos = this.positions.get(uuid).toVector();
                    final Vector velocity = pos.subtract(oldpos);
                    newv.setX(velocity.getX());
                    newv.setZ(velocity.getZ());
                }

                entity.setVelocity(newv.clone());
            }

            this.velocities.put(uuid, newv.clone());
            this.onGround.put(uuid, entity.isOnGround());
            this.positions.put(uuid, entity.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(final EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }
}
