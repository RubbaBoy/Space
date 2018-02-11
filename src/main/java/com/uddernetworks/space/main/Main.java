package com.uddernetworks.space.main;

import com.google.common.collect.ImmutableMap;
import com.uddernetworks.command.CommandManager;
import com.uddernetworks.space.blocks.AnimatedBlock;
import com.uddernetworks.space.blocks.BasicBlock;
import com.uddernetworks.space.blocks.CustomBlockManager;
import com.uddernetworks.space.blocks.WorkbenchBlock;
import com.uddernetworks.space.command.RocketCommand;
import com.uddernetworks.space.command.SpaceCommand;
import com.uddernetworks.space.guis.AlloyMixerGUI;
import com.uddernetworks.space.guis.GUIManager;
import com.uddernetworks.space.guis.ProgressBar;
import com.uddernetworks.space.guis.ProgressBarManager;
import com.uddernetworks.space.items.BasicItem;
import com.uddernetworks.space.items.CustomItemManager;
import com.uddernetworks.space.items.EasyShapedRecipe;
import com.uddernetworks.space.recipies.AlloyMixerRecipe;
import com.uddernetworks.space.recipies.Recipe;
import com.uddernetworks.space.recipies.RecipeManager;
import com.uddernetworks.space.recipies.WorkbenchRecipe;
import com.uddernetworks.space.stacker.ItemStacker;
import com.uddernetworks.space.utils.FastTaskTracker;
import com.uddernetworks.space.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.Material.AIR;

public class Main extends JavaPlugin implements Listener {

    private GUIManager guiManager;
    private ProgressBarManager progressBarManager;
    private RecipeManager recipeManager;
    private CustomItemManager customItemManager;
    private CustomBlockManager customBlockManager;

    private HashMap<UUID, Vector> velocities;
    private HashMap<UUID, Location> positions;
    private HashMap<UUID, Boolean> onGround;
    private FastTaskTracker fastTaskTracker;

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
        this.progressBarManager = new ProgressBarManager(this);
        this.recipeManager = new RecipeManager(this);
        this.customItemManager = new CustomItemManager(this);
        this.customBlockManager = new CustomBlockManager(this);
        this.fastTaskTracker = new FastTaskTracker(this);

        getServer().getPluginManager().registerEvents(this.customItemManager, this);
        getServer().getPluginManager().registerEvents(this.customBlockManager, this);
        getServer().getPluginManager().registerEvents(new ItemStacker(this), this);

        /* Progress Bars */

        int[] damages = new int[122];

        for (int i = 0; i < 122; i++) damages[i] = i + 111;

        this.progressBarManager.addProgressBar(new ProgressBar("AlloyMixerBar", Material.DIAMOND_HOE, damages));

        /* GUIs */

//        this.guiManager.addGUI(new Workbench(this, "WorkbenchBlock", 54));
//        this.guiManager.addGUI(new AlloyMixerGUI(this, "AlloyMixer", 54));

        /* Items */

        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 51, "Carbon"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 52, "Magnesium Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 53, "Raw Silicon"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 54, "Copper Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 55, "Aluminum Ingot"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 56, "IC"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 57, "CPU"));
        this.customItemManager.addCustomItem(new BasicItem(Material.DIAMOND_HOE, 58, "Steel"));

        /* Blocks */

        this.customBlockManager.addCustomBlock(new WorkbenchBlock(this, Material.DIAMOND_HOE, 21, Material.WOOL, "Spaceship Workbench"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 22, Material.STONE, "Carbon Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 23, Material.BLACK_SHULKER_BOX, "Carbon Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 24, Material.STONE, "Magnesium Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 25, Material.GRAY_SHULKER_BOX, "Magnesium Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 26, Material.STONE, "Silicon Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 27, Material.GRAY_SHULKER_BOX, "Silicon Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 28, Material.STONE, "Copper Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 29, Material.ORANGE_SHULKER_BOX, "Copper Block"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 30, Material.STONE, "Aluminum Ore"));
        this.customBlockManager.addCustomBlock(new BasicBlock(this, Material.DIAMOND_HOE, 31, Material.WHITE_SHULKER_BOX, "Aluminum Block"));
        this.customBlockManager.addCustomBlock(new AnimatedBlock(this, Material.DIAMOND_HOE, new short[] {32}, Material.WHITE_SHULKER_BOX, "Alloy Mixer", () -> getGUIManager().addGUI(new AlloyMixerGUI(this, "Alloy Mixer", 54, UUID.randomUUID()))));


        /* Recipes */

        /* Workbench Recipes */

        this.recipeManager.addRecipe(new WorkbenchRecipe(new char[][] {
                {'D', ' ', ' ', ' ', 'D'},
                {' ', 'D', ' ', 'D', ' '},
                {' ', ' ', 'D', ' ', ' '},
                {' ', 'D', ' ', 'D', ' '},
                {'D', ' ', ' ', ' ', 'D'}
        }, ImmutableMap.of('D', ItemBuilder.itemFrom(Material.DIRT)), ItemBuilder.create().setMaterial(Material.DIAMOND_BLOCK).setAmount(64).build(), true));

        /* Alloy Mixer Recipes */

        this.recipeManager.addRecipe(new AlloyMixerRecipe(this.customItemManager.getCustomItem("Carbon").toItemStack(), ItemBuilder.itemFrom(Material.IRON_INGOT), this.customItemManager.getCustomItem("Steel").toItemStack()));


        EasyShapedRecipe icRecipe = new EasyShapedRecipe(this, "IC", "IC", "   ", "CCC", "SSS");
        icRecipe.addIngredient('C', "Copper Ingot");
        icRecipe.addIngredient('S', "Raw Silicon");
        icRecipe.register();

        EasyShapedRecipe cpuRecipe = new EasyShapedRecipe(this, "CPU", "CPU", "ISI", "CDC", "ISI");
        cpuRecipe.addIngredient('I', "IC");
        cpuRecipe.addIngredient('D', ItemBuilder.itemFrom(Material.DIAMOND));
        cpuRecipe.addIngredient('S', "Raw Silicon");
        cpuRecipe.addIngredient('C', "Copper Ingot");
        cpuRecipe.register();
    }

    @Override
    public void onDisable() {
        this.guiManager.clearGUIs();
    }

    public GUIManager getGUIManager() {
        return guiManager;
    }

    public ProgressBarManager getProgressBarManager() {
        return progressBarManager;
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

    public FastTaskTracker getFastTaskTracker() {
        return fastTaskTracker;
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
