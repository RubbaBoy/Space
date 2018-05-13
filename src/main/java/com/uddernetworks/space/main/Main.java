package com.uddernetworks.space.main;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.google.common.collect.ImmutableMap;
import com.uddernetworks.command.CommandManager;
import com.uddernetworks.space.blocks.*;
import com.uddernetworks.space.command.RocketCommand;
import com.uddernetworks.space.command.SpaceCommand;
import com.uddernetworks.space.database.DatabaseManager;
import com.uddernetworks.space.electricity.CircuitMapManager;
import com.uddernetworks.space.entities.CustomEntities;
import com.uddernetworks.space.entities.CustomEntityTest;
import com.uddernetworks.space.guis.*;
import com.uddernetworks.space.items.BasicItem;
import com.uddernetworks.space.items.CustomIDManager;
import com.uddernetworks.space.items.CustomItemManager;
import com.uddernetworks.space.items.EasyShapedRecipe;
import com.uddernetworks.space.meta.EnhancedMetadataManager;
import com.uddernetworks.space.recipies.AlloyMixerRecipe;
import com.uddernetworks.space.recipies.RecipeManager;
import com.uddernetworks.space.recipies.WorkbenchRecipe;
import com.uddernetworks.space.utils.FastTaskTracker;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.QuadConsumer;
import com.uddernetworks.space.utils.Reflect;
import io.netty.channel.*;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.Slot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

import static org.bukkit.Material.AIR;

public class Main extends JavaPlugin implements Listener {

    private GUIManager guiManager;
    private ProgressBarManager progressBarManager;
    private RecipeManager recipeManager;
    private CustomItemManager customItemManager;
    private CustomBlockManager customBlockManager;
    private CustomIDManager customIDManager;
    private DatabaseManager databaseManager;
    private BlockDataManager blockDataManager;
    private EnhancedMetadataManager enhancedMetadataManager;
    private CircuitMapManager circuitMapManager;
    private BlockLoadInitializer blockLoadInitializer;

    private TaskChainFactory taskChainFactory;

    private Map<UUID, Vector> velocities;
    private Map<UUID, Location> positions;
    private Map<UUID, Boolean> onGround;
    private FastTaskTracker fastTaskTracker;

    private Map<Class<? extends Container>, QuadConsumer<Container, EntityHuman, Integer, ItemStackHolder>> clickActions = new HashMap<>();

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

        this.taskChainFactory = BukkitTaskChainFactory.create(this);

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect(new File(getDataFolder(), "data.db"));
        this.databaseManager.initialize();

        this.blockDataManager = new BlockDataManager(this);

        this.circuitMapManager = new CircuitMapManager(this);

        clickActions.put(ContainerAnvil.class, this::containerAnvil);
        clickActions.put(ContainerBeacon.class, this::containerBeacon);
        clickActions.put(ContainerBrewingStand.class, this::containerBrewingStand);
        clickActions.put(ContainerChest.class, this::containerChest);
        clickActions.put(ContainerDispenser.class, this::containerDispenser);
        clickActions.put(ContainerFurnace.class, this::containerFurnace);
        clickActions.put(ContainerHopper.class, this::containerHopper);
        clickActions.put(ContainerHorse.class, this::containerHorse);
        clickActions.put(ContainerMerchant.class, this::containerMerchant);
        clickActions.put(ContainerPlayer.class, this::containerPlayer);
        clickActions.put(ContainerShulkerBox.class, this::containerShulkerBox);
        clickActions.put(ContainerWorkbench.class, this::containerWorkbench);
//        clickActions.put(null, this::craftContainer);


        this.guiManager = new GUIManager(this);
        this.progressBarManager = new ProgressBarManager(this);
        this.recipeManager = new RecipeManager(this);
        this.customItemManager = new CustomItemManager(this);
        this.customBlockManager = new CustomBlockManager(this);
        this.fastTaskTracker = new FastTaskTracker(this);
        this.enhancedMetadataManager = new EnhancedMetadataManager(this);
        this.blockLoadInitializer = new BlockLoadInitializer(this);

        getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).forEach(this::create);

        getServer().getPluginManager().registerEvents(this.customItemManager, this);
        getServer().getPluginManager().registerEvents(this.customBlockManager, this);
        getServer().getPluginManager().registerEvents(this.blockLoadInitializer, this);
//        getServer().getPluginManager().registerEvents(new ItemStacker(this), this);

        /* Progress Bars */

        int[] damages = new int[122];

        for (int i = 0; i < 122; i++) damages[i] = i + 111;

        this.progressBarManager.addProgressBar(new ProgressBar("AlloyMixerBar", Material.DIAMOND_HOE, damages));

        int[] damages2 = new int[150];

        for (int i = 0; i < 150; i++) damages2[i] = i + 111 + 122;

        this.progressBarManager.addProgressBar(new ProgressBar("CryogenicContainerBar", Material.DIAMOND_HOE, damages2));

        int[] damages3 = new int[17];

        for (int i = 0; i < 17; i++) damages3[i] = i + 111 + 122 + 151;

        this.progressBarManager.addProgressBar(new ProgressBar("ElectricFurnaceBar", Material.DIAMOND_HOE, damages3));

        int[] damages4 = new int[23];

        for (int i = 0; i < 23; i++) damages4[i] = i + 111 + 122 + 151 + 17;

        this.progressBarManager.addProgressBar(new ProgressBar("FurnaceArrowBar", Material.DIAMOND_HOE, damages4));

        int[] damages5 = new int[16];

        for (int i = 0; i < 16; i++) damages5[i] = i + 111 + 122 + 151 + 17 + 23;

        this.progressBarManager.addProgressBar(new ProgressBar("GeneratorLoad", Material.DIAMOND_HOE, damages5));

        int[] damages6 = new int[2];

        for (int i = 0; i < 2; i++) damages6[i] = i + 111 + 122 + 151 + 17 + 23 + 16;

        this.progressBarManager.addProgressBar(new ProgressBar("CryogenicStatus", Material.DIAMOND_HOE, damages6));

        this.customIDManager = new CustomIDManager(this);

        /* Items */

        this.customItemManager.addCustomItem(new BasicItem(0, Material.DIAMOND_HOE, 80, "Carbon")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(1, Material.DIAMOND_HOE, 81, "Magnesium Ingot")); // WIki
        this.customItemManager.addCustomItem(new BasicItem(2, Material.DIAMOND_HOE, 82, "Raw Silicon")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(3, Material.DIAMOND_HOE, 83, "Copper Ingot")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(4, Material.DIAMOND_HOE, 84, "Aluminum Ingot")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(5, Material.DIAMOND_HOE, 85, "IC")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(6, Material.DIAMOND_HOE, 86, "CPU")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(7, Material.DIAMOND_HOE, 87, "Steel")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(8, Material.DIAMOND_HOE, 88, "Liquid Oxygen Generator Engine")); // Wiki
        this.customItemManager.addCustomItem(new BasicItem(9, Material.DIAMOND_HOE, 89, "Liquid Hydrogen Generator Engine")); // WIki
        this.customItemManager.addCustomItem(new BasicItem(10, Material.DIAMOND_HOE, 90, "Gear")); // Wiki

        /* Blocks */

        this.customBlockManager.addCustomBlock(new DirectionalBlock(this, 100, Material.DIAMOND_HOE, new short[][] {{21}, {22}, {23}, {24}}, Material.WOOL, "Spaceship Workbench", () -> getGUIManager().addGUI(new Workbench(this, "Workbench", 54, UUID.randomUUID())))); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 101, Material.DIAMOND_HOE, 25, Material.STONE, "Carbon Ore")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 102, Material.DIAMOND_HOE, 26, Material.BLACK_SHULKER_BOX, "Carbon Block")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 103, Material.DIAMOND_HOE, 27, Material.STONE, "Magnesium Ore")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 104, Material.DIAMOND_HOE, 28, Material.GRAY_SHULKER_BOX, "Magnesium Block")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 105, Material.DIAMOND_HOE, 29, Material.STONE, "Silicon Ore")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 106, Material.DIAMOND_HOE, 30, Material.GRAY_SHULKER_BOX, "Silicon Block")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 107, Material.DIAMOND_HOE, 31, Material.STONE, "Copper Ore")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 108, Material.DIAMOND_HOE, 32, Material.ORANGE_SHULKER_BOX, "Copper Block")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 109, Material.DIAMOND_HOE, 33, Material.STONE, "Aluminum Ore")); // Wiki
        this.customBlockManager.addCustomBlock(new BasicBlock(this, 110, Material.DIAMOND_HOE, 34, Material.WHITE_SHULKER_BOX, "Aluminum Block")); // Wiki
        this.customBlockManager.addCustomBlock(new AlloyMixerBlock(this, 111, Material.DIAMOND_HOE, new short[] {35, 36, 37}, Material.WHITE_SHULKER_BOX, "Alloy Mixer")); // Wiki
        this.customBlockManager.addCustomBlock(new CryogenicContainerBlock(this, 112, Material.DIAMOND_HOE, 38, Material.GRAY_SHULKER_BOX, "Cryogenic Container")); // Wiki

        this.customBlockManager.addCustomBlock(new LiquidOxygenGeneratorBlock(this, 113, Material.DIAMOND_HOE, 39, Material.GRAY_SHULKER_BOX, "Liquid Oxygen Generator")); // Wiki
        this.customBlockManager.addCustomBlock(new LiquidHydrogenGeneratorBlock(this, 114, Material.DIAMOND_HOE, 40, Material.GRAY_SHULKER_BOX, "Liquid Hydrogen Generator")); // Wiki
        this.customBlockManager.addCustomBlock(new ElectricFurnaceBlock(this, 115, Material.DIAMOND_AXE, new short[][] {
                {61 + 59, 62 + 59, 63 + 59, 64 + 59, 65 + 59, 66 + 59, 67 + 59},
                {68 + 59, 69 + 59, 70 + 59, 71 + 59, 72 + 59, 73 + 59, 74 + 59},
                {75 + 59, 76 + 59, 77 + 59, 78 + 59, 79 + 59, 80 + 59, 81 + 59},
                {82 + 59, 83 + 59, 84 + 59, 85 + 59, 86 + 59, 87 + 59, 88 + 59}}, Material.WHITE_SHULKER_BOX, "Electric Furnace")); // Wiki
        this.customBlockManager.addCustomBlock(new GeneratorBlock(this, 116, Material.DIAMOND_HOE, new short[][] {{41}, {42}, {43}, {44}}, Material.ORANGE_SHULKER_BOX, "Generator")); // NOT ON WIKI, need to fix model


        /* DEBUG ONLY */

        // Ground
        this.customBlockManager.addCustomBlock(new WireBlock(this, 117, Material.DIAMOND_AXE, 1, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 118, Material.DIAMOND_AXE, 2, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 119, Material.DIAMOND_AXE, 3, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 120, Material.DIAMOND_AXE, 4, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 121, Material.DIAMOND_AXE, 5, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 122, Material.DIAMOND_AXE, 6, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 123, Material.DIAMOND_AXE, 7, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 124, Material.DIAMOND_AXE, 8, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 125, Material.DIAMOND_AXE, 9, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 126, Material.DIAMOND_AXE, 10, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 127, Material.DIAMOND_AXE, 11, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 128, Material.DIAMOND_AXE, 12, Material.REDSTONE_BLOCK, "Wire Three SEW"));

        // WALL     SEW
        this.customBlockManager.addCustomBlock(new WireBlock(this, 129, Material.DIAMOND_AXE, 13, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 130, Material.DIAMOND_AXE, 14, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 131, Material.DIAMOND_AXE, 15, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 132, Material.DIAMOND_AXE, 16, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 133, Material.DIAMOND_AXE, 17, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 134, Material.DIAMOND_AXE, 18, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 135, Material.DIAMOND_AXE, 19, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 136, Material.DIAMOND_AXE, 20, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 137, Material.DIAMOND_AXE, 21, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 138, Material.DIAMOND_AXE, 22, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 139, Material.DIAMOND_AXE, 23, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 140, Material.DIAMOND_AXE, 24, Material.REDSTONE_BLOCK, "Wire Three SEW"));

        // WALL     NEW
        this.customBlockManager.addCustomBlock(new WireBlock(this, 141, Material.DIAMOND_AXE, 25, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 142, Material.DIAMOND_AXE, 26, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 143, Material.DIAMOND_AXE, 27, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 144, Material.DIAMOND_AXE, 28, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 145, Material.DIAMOND_AXE, 29, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 146, Material.DIAMOND_AXE, 30, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 147, Material.DIAMOND_AXE, 31, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 148, Material.DIAMOND_AXE, 32, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 149, Material.DIAMOND_AXE, 33, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 150, Material.DIAMOND_AXE, 34, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 151, Material.DIAMOND_AXE, 35, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 152, Material.DIAMOND_AXE, 36, Material.REDSTONE_BLOCK, "Wire Three SEW"));

        // WALL     NSE
        this.customBlockManager.addCustomBlock(new WireBlock(this, 153, Material.DIAMOND_AXE, 37, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 154, Material.DIAMOND_AXE, 38, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 155, Material.DIAMOND_AXE, 39, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 156, Material.DIAMOND_AXE, 40, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 157, Material.DIAMOND_AXE, 41, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 158, Material.DIAMOND_AXE, 42, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 159, Material.DIAMOND_AXE, 43, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 160, Material.DIAMOND_AXE, 44, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 161, Material.DIAMOND_AXE, 45, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 162, Material.DIAMOND_AXE, 46, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 163, Material.DIAMOND_AXE, 47, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 164, Material.DIAMOND_AXE, 48, Material.REDSTONE_BLOCK, "Wire Three SEW"));

        // WALL     NSW
        this.customBlockManager.addCustomBlock(new WireBlock(this, 165, Material.DIAMOND_AXE, 49, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 166, Material.DIAMOND_AXE, 50, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 167, Material.DIAMOND_AXE, 51, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 168, Material.DIAMOND_AXE, 52, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 169, Material.DIAMOND_AXE, 53, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 170, Material.DIAMOND_AXE, 54, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 171, Material.DIAMOND_AXE, 55, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 172, Material.DIAMOND_AXE, 56, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 173, Material.DIAMOND_AXE, 57, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 174, Material.DIAMOND_AXE, 58, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 175, Material.DIAMOND_AXE, 59, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 176, Material.DIAMOND_AXE, 60, Material.REDSTONE_BLOCK, "Wire Three SEW"));

        // WALL     Ceiling
        this.customBlockManager.addCustomBlock(new WireBlock(this, 177, Material.DIAMOND_AXE, 61, Material.REDSTONE_BLOCK, "Wire"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 178, Material.DIAMOND_AXE, 62, Material.REDSTONE_BLOCK, "Wire Line X"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 179, Material.DIAMOND_AXE, 63, Material.REDSTONE_BLOCK, "Wire Line Z"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 180, Material.DIAMOND_AXE, 64, Material.REDSTONE_BLOCK, "Wire Turn SE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 181, Material.DIAMOND_AXE, 65, Material.REDSTONE_BLOCK, "Wire Turn SW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 182, Material.DIAMOND_AXE, 66, Material.REDSTONE_BLOCK, "Wire Turn NW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 183, Material.DIAMOND_AXE, 67, Material.REDSTONE_BLOCK, "Wire Turn NE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 184, Material.DIAMOND_AXE, 68, Material.REDSTONE_BLOCK, "Wire Cross"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 185, Material.DIAMOND_AXE, 69, Material.REDSTONE_BLOCK, "Wire Three NEW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 186, Material.DIAMOND_AXE, 70, Material.REDSTONE_BLOCK, "Wire Three NSE"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 187, Material.DIAMOND_AXE, 71, Material.REDSTONE_BLOCK, "Wire Three NSW"));
        this.customBlockManager.addCustomBlock(new WireBlock(this, 188, Material.DIAMOND_AXE, 72, Material.REDSTONE_BLOCK, "Wire Three SEW"));


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

        this.recipeManager.addRecipe(new AlloyMixerRecipe(this, this.customItemManager.getCustomItem("Carbon").toItemStack(), ItemBuilder.itemFrom(Material.IRON_INGOT), this.customItemManager.getCustomItem("Steel").toItemStack()));

        /* Electric Furnace Recipes */

//        this.recipeManager.addRecipe(new ElectricFurnaceRecipe(this, null, null));


        EasyShapedRecipe icRecipe = new EasyShapedRecipe(this, "IC", 5, "   ", "CCC", "SSS");
        icRecipe.addIngredient('C', 3);
        icRecipe.addIngredient('S', 2);
        icRecipe.register();

        EasyShapedRecipe cpuRecipe = new EasyShapedRecipe(this, "CPU", 6, "ISI", "CDC", "ISI");
        cpuRecipe.addIngredient('I', 5);
        cpuRecipe.addIngredient('D', Material.DIAMOND);
        cpuRecipe.addIngredient('S', 2);
        cpuRecipe.addIngredient('C', 3);
        cpuRecipe.register();

        EasyShapedRecipe alloyMixerRecipe = new EasyShapedRecipe(this, "AlloyMixer", 111, "FCF", "CIC", "CHC");
        alloyMixerRecipe.addIngredient('F', Material.FURNACE);
        alloyMixerRecipe.addIngredient('I', 5);
        alloyMixerRecipe.addIngredient('H', Material.HOPPER);
        alloyMixerRecipe.addIngredient('C', Material.COBBLESTONE);
        alloyMixerRecipe.register();

        EasyShapedRecipe workbenchRecipe = new EasyShapedRecipe(this, "SpaceshipWorkbench", 100, "SSS", "SCS", "SSS");
        workbenchRecipe.addIngredient('C', Material.WORKBENCH);
        workbenchRecipe.addIngredient('S', 7);
        workbenchRecipe.register();

        EasyShapedRecipe gearRecipe = new EasyShapedRecipe(this, "Gear", 10, " I ", "I I", " I ");
        gearRecipe.addIngredient('I', Material.IRON_INGOT);
        gearRecipe.register();

        EasyShapedRecipe liquidOxygenGeneratorEngineRecipe = new EasyShapedRecipe(this, "LiquidOxygenGeneratorEngine", 8, " G ", "GRG", "III");
        liquidOxygenGeneratorEngineRecipe.addIngredient('G', 10);
        liquidOxygenGeneratorEngineRecipe.addIngredient('R', Material.REDSTONE);
        liquidOxygenGeneratorEngineRecipe.addIngredient('I', Material.IRON_INGOT);
        liquidOxygenGeneratorEngineRecipe.register();

        EasyShapedRecipe liquidHydrogenGeneratorEngineRecipe = new EasyShapedRecipe(this, "LiquidHydrogenGeneratorEngine", 9, "   ", "GRG", "III");
        liquidHydrogenGeneratorEngineRecipe.addIngredient('G', 10);
        liquidHydrogenGeneratorEngineRecipe.addIngredient('R', Material.REDSTONE);
        liquidHydrogenGeneratorEngineRecipe.addIngredient('I', Material.IRON_INGOT);
        liquidHydrogenGeneratorEngineRecipe.register();

        EasyShapedRecipe liquidOxygenGenerator = new EasyShapedRecipe(this, "LiquidOxygenGenerator", 113, "SCS", "SMS", "SLS");
        liquidOxygenGenerator.addIngredient('C', 5);
        liquidOxygenGenerator.addIngredient('L', 112);
        liquidOxygenGenerator.addIngredient('M', 8);
        liquidOxygenGenerator.addIngredient('S', 7);
        liquidOxygenGenerator.register();

        EasyShapedRecipe liquidHydrogenGenerator = new EasyShapedRecipe(this, "LiquidHydrogenGenerator", 114, "SCS", "SMS", "SLS");
        liquidHydrogenGenerator.addIngredient('C', 5);
        liquidHydrogenGenerator.addIngredient('L', 112);
        liquidHydrogenGenerator.addIngredient('M', 9);
        liquidHydrogenGenerator.addIngredient('S', 7);
        liquidHydrogenGenerator.register();


        CustomEntities.registerEntities();

        this.blockDataManager.updateCaches(() -> this.blockLoadInitializer.init());
    }

    @Override
    public void onDisable() {
        this.circuitMapManager.clearDebugText();
        this.guiManager.saveInventories();

        this.guiManager.clearGUIs();
        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).forEach(this::remove);

        CustomEntities.unregisterEntities();

        try {
            databaseManager.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public CustomIDManager getCustomIDManager() {
        return customIDManager;
    }

    public FastTaskTracker getFastTaskTracker() {
        return fastTaskTracker;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public EnhancedMetadataManager getEnhancedMetadataManager() {
        return enhancedMetadataManager;
    }

    public CircuitMapManager getCircuitMapManager() {
        return circuitMapManager;
    }

    public BlockLoadInitializer getBlockLoadInitializer() {
        return blockLoadInitializer;
    }

    public <T> TaskChain<T> newChain() {
        return this.taskChainFactory.newChain();
    }

    public <T> TaskChain<T> newSharedChain(String name) {
        return this.taskChainFactory.newSharedChain(name);
    }

//    //    @EventHandler
//    public void onPlayerClick(PlayerInteractEvent event) {
//
//        if (event.getHand() != EquipmentSlot.HAND) return;
//
//        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
//
//        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
//
//            blockDataManager.getData(event.getClickedBlock(), "intKey", result -> {
//                event.getPlayer().sendMessage(ChatColor.GOLD + "Data for block is: " + result);
//            });
//
//        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
//
//            int data = ThreadLocalRandom.current().nextInt(10000);
//
//            blockDataManager.setData(event.getClickedBlock(), "intKey", data, () -> {
//                event.getPlayer().sendMessage(ChatColor.GOLD + "SET data for block: " + ChatColor.RED + data);
//            });
//
//        }
//
//        event.setCancelled(true);
//    }

    private List<Block> blocks = new ArrayList<>();
    private List<ArmorStand> armorStands = new ArrayList<>();

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.STICK) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            blocks.remove(event.getClickedBlock());
            blocks.add(event.getClickedBlock());

            event.getPlayer().sendMessage(ChatColor.GOLD + "Added block");
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Showing power levels");

            armorStands.forEach(ArmorStand::remove);
            armorStands.clear();

            for (Block block : blocks) {
                CustomBlock customBlock = blockDataManager.getCustomBlock(block);

                if (customBlock == null) return;

                ArmorStand armorStand = (ArmorStand) event.getPlayer().getWorld().spawnEntity(block.getLocation().add(0.5, -1, 0.5), EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setMarker(true);
                armorStand.setCustomNameVisible(true);
                armorStand.setCustomName(ChatColor.GOLD + "Output: " + customBlock.getOutputPower(block));

                armorStands.add(armorStand);

                ArmorStand armorStand2 = (ArmorStand) event.getPlayer().getWorld().spawnEntity(block.getLocation().add(0.5, -1.3, 0.5), EntityType.ARMOR_STAND);
                armorStand2.setVisible(false);
                armorStand2.setGravity(false);
                armorStand.setMarker(true);
                armorStand2.setCustomNameVisible(true);
                armorStand2.setCustomName(ChatColor.RED + "Power: " + customBlock.getSupply(block));

                armorStands.add(armorStand2);
            }

        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Cleared ArmorStands");

            armorStands.forEach(ArmorStand::remove);
            armorStands.clear();
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerInteractEvent event) {

//        Location location = event.getClickedBlock().getLocation();

//        System.out.println("pitch = " + event.getPlayer().getLocation().getPitch());
//        System.out.println("yaw = " + event.getPlayer().getLocation().getYaw());

//        this.armorStand.setHeadPose(new EulerAngle(toRadians(event.getPlayer().getLocation().getPitch()), toRadians(event.getPlayer().getLocation().getYaw()), 0));
    }

    private double toRadians(float degrese) {
        return Math.toRadians(degrese % 360);
    }

    public void updateVelocities() {
        org.bukkit.World world = Bukkit.getServer().getWorld("moon");
        if (world == null) return;

        for (org.bukkit.entity.Entity entity : world.getEntities()) {
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


    /**
     * Inject our custom ChannelDuplexHandler when the player joins the server
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        create(event.getPlayer());
    }

    /**
     * Remove our custom ChannelDuplexHandler explicitly, NOTE: If they're quitting
     * the server this data is cleared anyway.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    /**
     * Submits a request to the pipeline to remove our ChannelDuplexHandler
     */
    private void remove(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    /**
     * Creates a custom ChannelDuplexHandler for a player, and this serves
     * to intercept packets before the packet handler
     */
    private void create(Player player) {
        remove(player);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.sendMessage(ChatColor.GOLD + "Now hooked into click packet rerouting.");
            ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
                /**
                 * Reads packets
                 */
                @Override
                public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
                    if (packet instanceof PacketPlayInWindowClick) {
                        PacketPlayInWindowClick packetPlayInWindowClick = (PacketPlayInWindowClick) packet;

                        EntityPlayer playerEntity = ((CraftPlayer) player).getHandle();

                        a(packetPlayInWindowClick, playerEntity);

                        return;
                    }

                    super.channelRead(context, packet);
                }

                /**
                 * Writes packets
                 */
                @Override
                public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {

                    if (packet instanceof PacketPlayOutSpawnEntityLiving) {
                        PacketPlayOutSpawnEntityLiving packetPlayOutSpawnEntity = (PacketPlayOutSpawnEntityLiving) packet;

                        UUID uuid = (UUID) Reflect.getField(packetPlayOutSpawnEntity, "b", false);

                        if (uuid == null) {
                            super.write(context, packet, channelPromise);
                            return;
                        }

                        org.bukkit.entity.Entity entity = Bukkit.getEntity(uuid);

                        CraftEntity craftEntity = (CraftEntity) entity;

                        if (craftEntity == null) {
                            super.write(context, packet, channelPromise);
                            return;
                        }

                        net.minecraft.server.v1_12_R1.Entity NMSEntity = craftEntity.getHandle();

                        if (NMSEntity instanceof CustomEntityTest) return;
                    }

                    super.write(context, packet, channelPromise);
                }
            };

//        remove(player);

//        ((CraftPlayer) player).getHandle().playerConnection.networkManager.setPacketListener(new PacketListener() {
//            @Override
//            public void a(IChatBaseComponent iChatBaseComponent) {
//                System.out.println("iChatBaseComponent = " + iChatBaseComponent);
//            }
//        });

//        DefaultChannelPipeline

            /**
             * Take the player's networking channel and add our custom DuplexHandler
             */
            try {
                ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
                pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler); // "packet_handler", --- beforeeeeeeee
            } catch (IllegalArgumentException ignored) {
                // TODO: Suppress properly later
            }
        }, 60L);

    }


    public void a(PacketPlayInWindowClick packetplayinwindowclick, EntityPlayer player) throws InterruptedException {
//        PlayerConnectionUtils.ensureMainThread(packetplayinwindowclick, player.playerConnection, player.x());
        player.resetIdleTimer();

        if (player.activeContainer.windowId == packetplayinwindowclick.a() && player.activeContainer.c(player) && player.activeContainer.canUse(player)) {
            boolean cancelled = player.isSpectator();
            if (packetplayinwindowclick.b() < -1 && packetplayinwindowclick.b() != -999) {
                return;
            }

            InventoryView inventory = player.activeContainer.getBukkitView();
            InventoryType.SlotType type = CraftInventoryView.getSlotType(inventory, packetplayinwindowclick.b());
            ClickType click = ClickType.UNKNOWN;
            InventoryAction action = InventoryAction.UNKNOWN;
            ItemStack itemstack = ItemStack.a;
            ItemStack clickedItem;
            Slot slot;
            ItemStack cursor;

            switch (packetplayinwindowclick.f()) {
                case PICKUP:
                    if (packetplayinwindowclick.c() == 0) {
                        click = ClickType.LEFT;
                    } else if (packetplayinwindowclick.c() == 1) {
                        click = ClickType.RIGHT;
                    }

                    if (packetplayinwindowclick.c() != 0 && packetplayinwindowclick.c() != 1) {
                        break;
                    }

                    action = InventoryAction.NOTHING;
                    if (packetplayinwindowclick.b() == -999) {
                        if (!player.inventory.getCarried().isEmpty()) {
                            action = packetplayinwindowclick.c() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                        }
                    } else if (packetplayinwindowclick.b() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                        if (slot == null) {
                            break;
                        }

                        clickedItem = slot.getItem();
                        cursor = player.inventory.getCarried();
                        if (clickedItem.isEmpty()) {
                            if (!cursor.isEmpty()) {
                                action = packetplayinwindowclick.c() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                            }
                        } else {
                            if (!slot.isAllowed(player)) {
                                break;
                            }

                            if (cursor.isEmpty()) {
                                action = packetplayinwindowclick.c() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                            } else if (slot.isAllowed(cursor)) {
                                if (getCustomItemManager().itemsSimilar(clickedItem, cursor)) {
                                    int toPlace = packetplayinwindowclick.c() == 0 ? cursor.getCount() : 1;
                                    toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.getCount());
                                    toPlace = Math.min(toPlace, slot.inventory.getMaxStackSize() - clickedItem.getCount());
                                    if (toPlace == 1) {
                                        action = InventoryAction.PLACE_ONE;
                                    } else if (toPlace == cursor.getCount()) {
                                        action = InventoryAction.PLACE_ALL;
                                    } else if (toPlace < 0) {
                                        action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                                    } else if (toPlace != 0) {
                                        action = InventoryAction.PLACE_SOME;
                                    }

                                } else if (cursor.getCount() <= slot.getMaxStackSize()) {
                                    action = InventoryAction.SWAP_WITH_CURSOR;
                                }
//                            } else if (cursor.getItem() == clickedItem.getItem() && (!cursor.usesData() || cursor.getData() == clickedItem.getData()) && ItemStack.equals(cursor, clickedItem) && clickedItem.getCount() >= 0 && clickedItem.getCount() + cursor.getCount() <= cursor.getMaxStackSize()) {
                            } else if (getCustomItemManager().itemsSimilar(clickedItem, cursor) && clickedItem.getCount() >= 0 && clickedItem.getCount() + cursor.getCount() <= cursor.getMaxStackSize()) {
                                action = InventoryAction.PICKUP_ALL;
                            }
                        }
                    }
                    break;
                case QUICK_MOVE:
                    if (packetplayinwindowclick.c() == 0) {
                        click = ClickType.SHIFT_LEFT;
                    } else if (packetplayinwindowclick.c() == 1) {
                        click = ClickType.SHIFT_RIGHT;
                    }

                    if (packetplayinwindowclick.c() != 0 && packetplayinwindowclick.c() != 1) {
                        break;
                    }

                    if (packetplayinwindowclick.b() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                        if (slot != null && slot.isAllowed(player) && slot.hasItem()) {
                            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                            break;
                        }

                        action = InventoryAction.NOTHING;
                    }
                    break;
                case SWAP:
                    if (packetplayinwindowclick.c() < 0 || packetplayinwindowclick.c() >= 9) {
                        break;
                    }

                    click = ClickType.NUMBER_KEY;
                    slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                    if (!slot.isAllowed(player)) {
                        action = InventoryAction.NOTHING;
                    } else {
                        clickedItem = player.inventory.getItem(packetplayinwindowclick.c());
                        boolean canCleanSwap = clickedItem.isEmpty() || slot.inventory == player.inventory && slot.isAllowed(clickedItem);
                        if (slot.hasItem()) {
                            if (canCleanSwap) {
                                action = InventoryAction.HOTBAR_SWAP;
                            } else {
                                action = InventoryAction.HOTBAR_MOVE_AND_READD;
                            }
                        } else {
                            if (!slot.hasItem() && !clickedItem.isEmpty() && slot.isAllowed(clickedItem)) {
                                action = InventoryAction.HOTBAR_SWAP;
                                break;
                            }

                            action = InventoryAction.NOTHING;
                        }
                    }
                    break;
                case CLONE:
                    if (packetplayinwindowclick.c() == 2) {
                        click = ClickType.MIDDLE;
                        if (packetplayinwindowclick.b() == -999) {
                            action = InventoryAction.NOTHING;
                            break;
                        }

                        slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                        if (slot != null && slot.hasItem() && player.abilities.canInstantlyBuild && player.inventory.getCarried().isEmpty()) {
                            action = InventoryAction.CLONE_STACK;
                            break;
                        }

                        action = InventoryAction.NOTHING;
                        break;
                    }

                    click = ClickType.UNKNOWN;
                    action = InventoryAction.UNKNOWN;
                    break;
                case THROW:
                    if (packetplayinwindowclick.b() >= 0) {
                        if (packetplayinwindowclick.c() == 0) {
                            click = ClickType.DROP;
                            slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                            if (slot != null && slot.hasItem() && slot.isAllowed(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                                action = InventoryAction.DROP_ONE_SLOT;
                                break;
                            }

                            action = InventoryAction.NOTHING;
                            break;
                        }

                        if (packetplayinwindowclick.c() != 1) {
                            break;
                        }

                        click = ClickType.CONTROL_DROP;
                        slot = player.activeContainer.getSlot(packetplayinwindowclick.b());
                        if (slot != null && slot.hasItem() && slot.isAllowed(player) && !slot.getItem().isEmpty() && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                            action = InventoryAction.DROP_ALL_SLOT;
                            break;
                        }

                        action = InventoryAction.NOTHING;
                        break;
                    }

                    click = ClickType.LEFT;
                    if (packetplayinwindowclick.c() == 1) {
                        click = ClickType.RIGHT;
                    }

                    action = InventoryAction.NOTHING;
                    break;
                case QUICK_CRAFT:
                    itemstack = a(player.activeContainer, packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), player);
                    break;
                case PICKUP_ALL:
                    click = ClickType.DOUBLE_CLICK;
                    action = InventoryAction.NOTHING;
                    if (packetplayinwindowclick.b() >= 0 && !player.inventory.getCarried().isEmpty()) {
                        cursor = player.inventory.getCarried();
                        action = InventoryAction.NOTHING;
//                        if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem()))) || inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem())))) {
                        if (contains(inventory.getTopInventory(), cursor) || contains(inventory.getBottomInventory(), cursor)) {
                            action = InventoryAction.COLLECT_TO_CURSOR;
                        }
                    }
            }

            if (packetplayinwindowclick.f() != InventoryClickType.QUICK_CRAFT) {
                Object event;
                if (click == ClickType.NUMBER_KEY) {
                    event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.b(), click, action, packetplayinwindowclick.c());
                } else {
                    event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.b(), click, action);
                }

                Inventory top = inventory.getTopInventory();
                if (packetplayinwindowclick.b() == 0 && top instanceof CraftingInventory) {
                    org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
                    if (recipe != null) {
                        if (click == ClickType.NUMBER_KEY) {
                            event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.b(), click, action, packetplayinwindowclick.c());
                        } else {
                            event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.b(), click, action);
                        }
                    }
                }

                ((InventoryClickEvent) event).setCancelled(cancelled);
                Container oldContainer = player.activeContainer;
                CraftServer craftServer = (CraftServer) Bukkit.getServer();
                craftServer.getPluginManager().callEvent((Event) event);
                if (player.activeContainer != oldContainer) {
                    return;
                }

                switch (((InventoryClickEvent) event).getResult()) {
                    case DENY: // 1
                        switch (action) {
                            case NOTHING:
                            default:
                                break;
                            case PICKUP_ALL:
                            case MOVE_TO_OTHER_INVENTORY:
                            case HOTBAR_MOVE_AND_READD:
                            case HOTBAR_SWAP:
                            case COLLECT_TO_CURSOR:
                            case UNKNOWN:
                                player.updateInventory(player.activeContainer);
                                break;
                            case PICKUP_SOME:
                            case PICKUP_HALF:
                            case PICKUP_ONE:
                            case PLACE_ALL:
                            case PLACE_SOME:
                            case PLACE_ONE:
                            case SWAP_WITH_CURSOR:
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, player.inventory.getCarried()));
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, packetplayinwindowclick.b(), player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                                break;
                            case DROP_ALL_CURSOR:
                            case DROP_ONE_CURSOR:
                            case CLONE_STACK:
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, player.inventory.getCarried()));
                                break;
                            case DROP_ALL_SLOT:
                            case DROP_ONE_SLOT:
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, packetplayinwindowclick.b(), player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                        }

                        return;
                    case DEFAULT: // 2
                    case ALLOW: // 3
                        itemstack = a(player.activeContainer, packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), player);
                    default:
                        if (event instanceof CraftItemEvent) {
                            player.updateInventory(player.activeContainer);
                        }
                }
            }

            if (ItemStack.matches(packetplayinwindowclick.e(), itemstack)) {
//            if (getCustomItemManager().itemsSimilar(packetplayinwindowclick.e(), itemstack)) {

                player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), true));
                player.f = true;
                player.activeContainer.b();
                player.broadcastCarriedItem();
                player.f = false;

                player.updateInventory(player.activeContainer);
            } else {
                ((IntHashMap<Short>) Reflect.getField(player.playerConnection, "k", false)).a(player.activeContainer.windowId, packetplayinwindowclick.d());
//                player.playerConnection.k.a(player.activeContainer.windowId, packetplayinwindowclick.d());
                player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), false));
                player.activeContainer.a(player, false);
                NonNullList nonnulllist1 = NonNullList.a();

                for (int j = 0; j < player.activeContainer.slots.size(); ++j) {
                    cursor = ((Slot) player.activeContainer.slots.get(j)).getItem();
                    ItemStack itemstack2 = cursor.isEmpty() ? ItemStack.a : cursor;
                    nonnulllist1.add(itemstack2);
                }

                player.a(player.activeContainer, nonnulllist1);

                player.updateInventory(player.activeContainer);
            }
        }

    }


    public ItemStack a(Container container, int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        ItemStack itemstack = ItemStack.a;
        PlayerInventory playerinventory = entityhuman.inventory;
        ItemStack itemstack1;
        int k;
        ItemStack itemstack2;
        int l;
        ItemStack itemstack4;
        ItemStack itemstack6;
        if (inventoryclicktype == InventoryClickType.QUICK_CRAFT) {
//            int i1 = container.g;
            int i1 = (int) Reflect.getField(container, Container.class, "g", false);
//            container.g = Container.c(j);
            Reflect.setField(container, Container.class, "g", Container.c(j), false);

            int g = (int) Reflect.getField(container, Container.class, "g", false);

//            if ((i1 != 1 || container.g != 2) && i1 != container.g) {
            if ((i1 != 1 || g != 2) && i1 != g) {
                Reflect.invokeMethod(container, Container.class, "d", false);
//                container.d();
            } else if (playerinventory.getCarried().isEmpty()) {
                Reflect.invokeMethod(container, Container.class, "d", false);
//                container.d();
//            } else if (container.g == 0) {
            } else if (g == 0) {
//                container.dragType = Container.b(j);
                Reflect.setField(container, Container.class, "dragType", Container.b(j), false);
//                if (a(container.dragType, entityhuman)) {
                if (Container.a((int) Reflect.getField(container, Container.class, "dragType", false), entityhuman)) {
//                    container.g = 1;
                    Reflect.setField(container, Container.class, "g", 1, false);
//                    container.h.clear();
                    Set<Slot> h = (Set<Slot>) Reflect.getField(container, Container.class, "h", false);
                    h.clear();
                    Reflect.setField(container, Container.class, "h", h, false, true);
                } else {
                    Reflect.invokeMethod(container, Container.class, "d", false);
//                    container.d();
                }
            } else if (g == 1) {
                Slot slot = (Slot) container.slots.get(i);
                itemstack1 = playerinventory.getCarried();
                if (slot != null && Container.a(slot, itemstack1, true) && slot.isAllowed(itemstack1) && ((int) Reflect.getField(container, Container.class, "dragType", false) == 2 || itemstack1.getCount() > ((Set<Slot>) Reflect.getField(container, Container.class, "h", false)).size()) && container.b(slot)) {
                    Set<Slot> h = (Set<Slot>) Reflect.getField(container, Container.class, "h", false);
                    h.add(slot);
                    Reflect.setField(container, Container.class, "h", h, false, true);
                }
            } else if (g == 2) {
                if (!((Set<Slot>) Reflect.getField(container, Container.class, "h", false)).isEmpty()) {
                    itemstack2 = playerinventory.getCarried().cloneItemStack();
                    l = playerinventory.getCarried().getCount();
                    Iterator iterator = ((Set<Slot>) Reflect.getField(container, Container.class, "h", false)).iterator();
                    HashMap draggedSlots = new HashMap();

                    label405:
                    while (true) {
                        Slot slot1;
                        ItemStack itemstack3;
                        do {
                            do {
                                do {
                                    do {
                                        if (!iterator.hasNext()) {
                                            InventoryView view = container.getBukkitView();
                                            org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack2);
                                            newcursor.setAmount(l);
                                            Map<Integer, org.bukkit.inventory.ItemStack> eventmap = new HashMap();
                                            Iterator var18 = draggedSlots.entrySet().iterator();

                                            while (var18.hasNext()) {
                                                Map.Entry<Integer, ItemStack> ditem = (Map.Entry) var18.next();
                                                eventmap.put((Integer) ditem.getKey(), CraftItemStack.asBukkitCopy((ItemStack) ditem.getValue()));
                                            }

                                            itemstack6 = playerinventory.getCarried();
                                            playerinventory.setCarried(CraftItemStack.asNMSCopy(newcursor));
                                            InventoryDragEvent event = new InventoryDragEvent(view, newcursor.getType() != Material.AIR ? newcursor : null, CraftItemStack.asBukkitCopy(itemstack6), (int) Reflect.getField(container, Container.class, "dragType", false) == 1, eventmap);
                                            entityhuman.world.getServer().getPluginManager().callEvent(event);
                                            boolean needsUpdate = event.getResult() != Event.Result.DEFAULT;
                                            if (event.getResult() != Event.Result.DENY) {
                                                Iterator var21 = draggedSlots.entrySet().iterator();

                                                while (var21.hasNext()) {
                                                    Map.Entry<Integer, ItemStack> dslot = (Map.Entry) var21.next();
                                                    view.setItem((Integer) dslot.getKey(), CraftItemStack.asBukkitCopy((ItemStack) dslot.getValue()));
                                                }

                                                if (playerinventory.getCarried() != null) {
                                                    playerinventory.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
                                                    needsUpdate = true;
                                                }
                                            } else {
                                                playerinventory.setCarried(itemstack6);
                                            }

                                            if (needsUpdate && entityhuman instanceof EntityPlayer) {
                                                ((EntityPlayer) entityhuman).updateInventory(container);
                                            }
                                            break label405;
                                        }

                                        slot1 = (Slot) iterator.next();
                                        itemstack3 = playerinventory.getCarried();
                                    } while (slot1 == null);
                                } while (!Container.a(slot1, itemstack3, true));
                            } while (!slot1.isAllowed(itemstack3));
                        }
                        while ((int) Reflect.getField(container, Container.class, "dragType", false) != 2 && itemstack3.getCount() < ((Set<Slot>) Reflect.getField(container, Container.class, "h", false)).size());

                        if (container.b(slot1)) {
                            itemstack4 = itemstack2.cloneItemStack();
                            int j1 = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            Container.a((Set<Slot>) Reflect.getField(container, Container.class, "h", false), (int) Reflect.getField(container, Container.class, "dragType", false), itemstack4, j1);
                            k = Math.min(itemstack4.getMaxStackSize(), slot1.getMaxStackSize(itemstack4));
                            if (itemstack4.getCount() > k) {
                                itemstack4.setCount(k);
                            }

                            l -= itemstack4.getCount() - j1;
                            draggedSlots.put(slot1.rawSlotIndex, itemstack4);
                        }
                    }
                }

                Reflect.invokeMethod(container, Container.class, "d", false);
//                container.d();
//                ContainerChest
            } else {
                Reflect.invokeMethod(container, Container.class, "d", false);
//                container.d();
            }
        } else if ((int) Reflect.getField(container, Container.class, "g", false) != 0) {
            Reflect.invokeMethod(container, Container.class, "d", false);
//            container.d();
        } else {
            Slot slot2;
            int k1;
            if (inventoryclicktype != InventoryClickType.PICKUP && inventoryclicktype != InventoryClickType.QUICK_MOVE || j != 0 && j != 1) {
                if (inventoryclicktype == InventoryClickType.SWAP && j >= 0 && j < 9) {
                    slot2 = (Slot) container.slots.get(i);
                    itemstack2 = playerinventory.getItem(j);
                    itemstack1 = slot2.getItem();
                    if (!itemstack2.isEmpty() || !itemstack1.isEmpty()) {
                        if (itemstack2.isEmpty()) {
                            if (slot2.isAllowed(entityhuman)) {
                                playerinventory.setItem(j, itemstack1);
                                Reflect.invokeMethod(slot2, "b", new Class<?>[] {int.class}, new Object[] {itemstack1.getCount()}, false);
                                slot2.set(ItemStack.a);
                                slot2.a(entityhuman, itemstack1);
                            }
                        } else if (itemstack1.isEmpty()) {
                            if (slot2.isAllowed(itemstack2)) {
                                k1 = slot2.getMaxStackSize(itemstack2);
                                if (itemstack2.getCount() > k1) {
                                    slot2.set(itemstack2.cloneAndSubtract(k1));
                                } else {
                                    slot2.set(itemstack2);
                                    playerinventory.setItem(j, ItemStack.a);
                                }
                            }
                        } else if (slot2.isAllowed(entityhuman) && slot2.isAllowed(itemstack2)) {
                            k1 = slot2.getMaxStackSize(itemstack2);
                            if (itemstack2.getCount() > k1) {
                                slot2.set(itemstack2.cloneAndSubtract(k1));
                                slot2.a(entityhuman, itemstack1);
                                if (!playerinventory.pickup(itemstack1)) {
                                    entityhuman.drop(itemstack1, true);
                                }
                            } else {
                                slot2.set(itemstack2);
                                playerinventory.setItem(j, itemstack1);
                                slot2.a(entityhuman, itemstack1);
                            }
                        }
                    }
                } else if (inventoryclicktype == InventoryClickType.CLONE && entityhuman.abilities.canInstantlyBuild && playerinventory.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) container.slots.get(i);
                    if (slot2 != null && slot2.hasItem()) {
                        itemstack2 = slot2.getItem().cloneItemStack();
                        itemstack2.setCount(getCustomItemManager().getMaxStackSize(itemstack2));
                        playerinventory.setCarried(itemstack2);
                        playerinventory.update();
                    }
                } else if (inventoryclicktype == InventoryClickType.THROW && playerinventory.getCarried().isEmpty() && i >= 0) {
                    slot2 = (Slot) container.slots.get(i);
                    if (slot2 != null && slot2.hasItem() && slot2.isAllowed(entityhuman)) {
                        itemstack2 = slot2.a(j == 0 ? 1 : slot2.getItem().getCount());
                        slot2.a(entityhuman, itemstack2);
                        entityhuman.drop(itemstack2, true);
                    }
                } else if (inventoryclicktype == InventoryClickType.PICKUP_ALL && i >= 0) {
                    slot2 = (Slot) container.slots.get(i);
                    itemstack2 = playerinventory.getCarried();
                    if (!itemstack2.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.isAllowed(entityhuman))) {
                        l = j == 0 ? 0 : container.slots.size() - 1;
                        k1 = j == 0 ? 1 : -1;

                        for (int l1 = 0; l1 < 2; ++l1) {
                            for (int i2 = l; i2 >= 0 && i2 < container.slots.size() && itemstack2.getCount() < itemstack2.getMaxStackSize(); i2 += k1) {
                                Slot slot3 = (Slot) container.slots.get(i2);
                                if (slot3.hasItem() && Container.a(slot3, itemstack2, true) && slot3.isAllowed(entityhuman) && container.a(itemstack2, slot3)) {
                                    itemstack4 = slot3.getItem();
                                    if (l1 != 0 || itemstack4.getCount() != itemstack4.getMaxStackSize()) {
                                        k = Math.min(itemstack2.getMaxStackSize() - itemstack2.getCount(), itemstack4.getCount());
                                        itemstack6 = slot3.a(k);
                                        itemstack2.add(k);
                                        if (itemstack6.isEmpty()) {
                                            slot3.set(ItemStack.a);
                                        }

                                        slot3.a(entityhuman, itemstack6);
                                    }
                                }
                            }
                        }
                    }

                    container.b();
                }
            } else if (i == -999) {
                if (!playerinventory.getCarried().isEmpty()) {
                    if (j == 0) {
                        ItemStack carried = playerinventory.getCarried();
                        playerinventory.setCarried(ItemStack.a);
                        entityhuman.drop(carried, true);
                    }

                    if (j == 1) {
                        entityhuman.drop(playerinventory.getCarried().cloneAndSubtract(1), true);
                    }
                }
            } else if (inventoryclicktype == InventoryClickType.QUICK_MOVE) {
                if (i < 0) {
                    return ItemStack.a;
                }

                slot2 = (Slot) container.slots.get(i);
                if (slot2 == null || !slot2.isAllowed(entityhuman)) {
                    return ItemStack.a;
                }

                for (itemstack2 = shiftClick(container, entityhuman, i); !itemstack2.isEmpty() && c(slot2.getItem(), itemstack2); itemstack2 = shiftClick(container, entityhuman, i)) {
                    itemstack = itemstack2.cloneItemStack();
                }
            } else {
                if (i < 0) {
                    return ItemStack.a;
                }

                slot2 = (Slot) container.slots.get(i);
                if (slot2 != null) {
                    itemstack2 = slot2.getItem();
                    itemstack1 = playerinventory.getCarried();
                    if (!itemstack2.isEmpty()) {
                        itemstack = itemstack2.cloneItemStack();
                    }

                    if (itemstack2.isEmpty()) {
                        if (!itemstack1.isEmpty() && slot2.isAllowed(itemstack1)) {
                            k1 = j == 0 ? itemstack1.getCount() : 1;
                            if (k1 > slot2.getMaxStackSize(itemstack1)) {
                                k1 = slot2.getMaxStackSize(itemstack1);
                            }

                            slot2.set(itemstack1.cloneAndSubtract(k1));
                        }
                    } else if (slot2.isAllowed(entityhuman)) {
                        if (itemstack1.isEmpty()) {
                            if (itemstack2.isEmpty()) {
                                slot2.set(ItemStack.a);
                                playerinventory.setCarried(ItemStack.a);
                            } else {
                                k1 = j == 0 ? itemstack2.getCount() : (itemstack2.getCount() + 1) / 2;
                                playerinventory.setCarried(slot2.a(k1));
                                if (itemstack2.isEmpty()) {
                                    slot2.set(ItemStack.a);
                                }

                                slot2.a(entityhuman, playerinventory.getCarried());
                            }
                        } else if (slot2.isAllowed(itemstack1)) {
//                            if (itemstack2.getItem() == itemstack1.getItem() && ItemBuilder.itemsEquals(itemstack2, itemstack1)) {
                            if (getCustomItemManager().getMaxStackSize(itemstack1) > 1 && getCustomItemManager().itemsSimilar(itemstack2, itemstack1)) {

                                k1 = j == 0 ? itemstack1.getCount() : 1;

                                if (k1 > getCustomItemManager().getMaxStackSize(itemstack1) - itemstack2.getCount()) {
                                    k1 = getCustomItemManager().getMaxStackSize(itemstack1) - itemstack2.getCount();
                                }

                                if (k1 > getCustomItemManager().getMaxStackSize(itemstack1) - itemstack2.getCount()) {
                                    k1 = getCustomItemManager().getMaxStackSize(itemstack1) - itemstack2.getCount();
                                }

                                itemstack1.subtract(k1);
                                itemstack2.add(k1);
                            } else if (itemstack1.getCount() <= getCustomItemManager().getMaxStackSize(itemstack1)) {
                                slot2.set(itemstack1);
                                playerinventory.setCarried(itemstack2);
                            }
                        } else if (itemstack1.getMaxStackSize() > 1 && getCustomItemManager().itemsSimilar(itemstack2, itemstack1)) {
                            k1 = itemstack2.getCount();
                            if (k1 + itemstack1.getCount() <= itemstack1.getMaxStackSize()) {
                                itemstack1.add(k1);
                                itemstack2 = slot2.a(k1);
                                if (itemstack2.isEmpty()) {
                                    slot2.set(ItemStack.a);
                                }

                                slot2.a(entityhuman, playerinventory.getCarried());
                            }
                        }
                    }

                    slot2.f();
                    if (entityhuman instanceof EntityPlayer && slot2.getMaxStackSize() != 64) {
                        ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, slot2.rawSlotIndex, slot2.getItem()));
                        if (container.getBukkitView().getType() == InventoryType.WORKBENCH || container.getBukkitView().getType() == InventoryType.CRAFTING) {
                            ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, 0, container.getSlot(0).getItem()));
                        }
                    }
                }
            }
        }

        return itemstack;
    }


    private static boolean c(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == itemstack1 || itemstack.getData() == itemstack1.getData();
    }

    private ItemStack shiftClick(Container container, EntityHuman entityhuman, int i) {
        ItemStackHolder itemStackHolder = new ItemStackHolder();
        if (clickActions.containsKey(container.getClass())) {
            clickActions.get(container.getClass()).accept(container, entityhuman, i, itemStackHolder);
        } else {
            craftContainer(container, entityhuman, i, itemStackHolder);
        }

        return itemStackHolder.getItemStack();
    }

    public ItemStack craftContainer(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        Container delegate = (Container) Reflect.getField(container, "delegate", false);
        return delegate != null ? delegate.shiftClick(entityhuman, i) : (ItemStack) Reflect.invokeMethod(container, container.getClass().getSuperclass(), "shiftClick", new Class<?>[] {EntityHuman.class, int.class}, new Object[] {entityhuman, i}, false);
    }

    public void containerWorkbench(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerWorkbench containerWorkbench = (ContainerWorkbench) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerWorkbench.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if (i == 0) {
                net.minecraft.server.v1_12_R1.World g = (net.minecraft.server.v1_12_R1.World) Reflect.getField(containerWorkbench, "g", false);
                itemstack1.getItem().b(itemstack1, g, entityhuman);
                if (!canAddItemToSlot(containerWorkbench, itemstack1, 10, 46, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (i >= 10 && i < 37) {
                if (!canAddItemToSlot(containerWorkbench, itemstack1, 37, 46, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 37 && i < 46) {
                if (!canAddItemToSlot(containerWorkbench, itemstack1, 10, 37, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerWorkbench, itemstack1, 10, 46, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            ItemStack itemstack2 = slot.a(entityhuman, itemstack1);
            if (i == 0) {
                entityhuman.drop(itemstack2, false);
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerShulkerBox(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerShulkerBox containerShulkerBox = (ContainerShulkerBox) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerShulkerBox.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            IInventory a = (IInventory) Reflect.getField(containerShulkerBox, "a", false);
            if (i < a.getSize()) {
                if (!canAddItemToSlot(containerShulkerBox, itemstack1, a.getSize(), containerShulkerBox.slots.size(), true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerShulkerBox, itemstack1, 0, a.getSize(), false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerPlayer(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerPlayer containerPlayer = (ContainerPlayer) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerPlayer.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            EnumItemSlot enumitemslot = EntityInsentient.d(itemstack);
            if (i == 0) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 9, 45, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (i >= 1 && i < 5) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 9, 45, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 5 && i < 9) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 9, 45, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (enumitemslot.a() == EnumItemSlot.Function.ARMOR && !((Slot) containerPlayer.slots.get(8 - enumitemslot.b())).hasItem()) {
                int j = 8 - enumitemslot.b();
                if (!canAddItemToSlot(containerPlayer, itemstack1, j, j + 1, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (enumitemslot == EnumItemSlot.OFFHAND && !((Slot) containerPlayer.slots.get(45)).hasItem()) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 45, 46, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 9 && i < 36) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 36, 45, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 36 && i < 45) {
                if (!canAddItemToSlot(containerPlayer, itemstack1, 9, 36, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerPlayer, itemstack1, 9, 45, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            ItemStack itemstack2 = slot.a(entityhuman, itemstack1);
            if (i == 0) {
                entityhuman.drop(itemstack2, false);
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerMerchant(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerMerchant containerMerchant = (ContainerMerchant) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerMerchant.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                if (!canAddItemToSlot(containerMerchant, itemstack1, 3, 39, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (i != 0 && i != 1) {
                if (i >= 3 && i < 30) {
                    if (!canAddItemToSlot(containerMerchant, itemstack1, 30, 39, false)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }
                } else if (i >= 30 && i < 39 && !canAddItemToSlot(containerMerchant, itemstack1, 3, 30, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerMerchant, itemstack1, 3, 39, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            slot.a(entityhuman, itemstack1);
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerHorse(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerHorse containerHorse = (ContainerHorse) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerHorse.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            IInventory a = (IInventory) Reflect.getField(containerHorse, "a", false);
            if (i < a.getSize()) {
                if (!canAddItemToSlot(containerHorse, itemstack1, a.getSize(), containerHorse.slots.size(), true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (containerHorse.getSlot(1).isAllowed(itemstack1) && !containerHorse.getSlot(1).hasItem()) {
                if (!canAddItemToSlot(containerHorse, itemstack1, 1, 2, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (containerHorse.getSlot(0).isAllowed(itemstack1)) {
                if (!canAddItemToSlot(containerHorse, itemstack1, 0, 1, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (a.getSize() <= 2 || !canAddItemToSlot(containerHorse, itemstack1, 2, a.getSize(), false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerHopper(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerHopper containerHopper = (ContainerHopper) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerHopper.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            IInventory hopper = (IInventory) Reflect.getField(containerHopper, "hopper", false);
            if (i < hopper.getSize()) {
                if (!canAddItemToSlot(containerHopper, itemstack1, hopper.getSize(), containerHopper.slots.size(), true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerHopper, itemstack1, 0, hopper.getSize(), false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerFurnace(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerFurnace containerFurnace = (ContainerFurnace) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerFurnace.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                if (!canAddItemToSlot(containerFurnace, itemstack1, 3, 39, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (i != 1 && i != 0) {
                if (!RecipesFurnace.getInstance().getResult(itemstack1).isEmpty()) {
                    if (!canAddItemToSlot(containerFurnace, itemstack1, 0, 1, false)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }
                } else if (TileEntityFurnace.isFuel(itemstack1)) {
                    if (!canAddItemToSlot(containerFurnace, itemstack1, 1, 2, false)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }
                } else if (i >= 3 && i < 30) {
                    if (!canAddItemToSlot(containerFurnace, itemstack1, 30, 39, false)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }
                } else if (i >= 30 && i < 39 && !canAddItemToSlot(containerFurnace, itemstack1, 3, 30, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerFurnace, itemstack1, 3, 39, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            slot.a(entityhuman, itemstack1);
        }

        itemStackHolder.setItemStack(itemstack);
    }


    public void containerDispenser(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerDispenser containerDispenser = (ContainerDispenser) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerDispenser.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if (i < 9) {
                if (!canAddItemToSlot(containerDispenser, itemstack1, 9, 45, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerDispenser, itemstack1, 0, 9, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            slot.a(entityhuman, itemstack1);
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerChest(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerChest containerChest = (ContainerChest) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerChest.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            int f = (int) Reflect.getField(containerChest, "f", false);
            if (i < f * 9) {
                if (!canAddItemToSlot(containerChest, itemstack1, f * 9, containerChest.slots.size(), true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerChest, itemstack1, 0, f * 9, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }
        }

        itemStackHolder.setItemStack(itemstack);
    }

    public void containerBrewingStand(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        try {
            ContainerBrewingStand containerBrewingStand = (ContainerBrewingStand) container;
            ItemStack itemstack = ItemStack.a;
            Slot slot = (Slot) containerBrewingStand.slots.get(i);
            if (slot != null && slot.hasItem()) {
                ItemStack itemstack1 = slot.getItem();
                itemstack = itemstack1.cloneItemStack();
                if ((i < 0 || i > 2) && i != 3 && i != 4) {
                    Slot f = (Slot) Reflect.getField(containerBrewingStand, "f", false);
                    boolean c_ = (boolean) Reflect.invokeMethod(null, Class.forName("net.minecraft.server.v1_12_R1.ContainerBrewingStand.SlotPotionBottle"), "c_", new Class<?>[] {ItemStack.class}, new Object[] {itemstack}, true);
                    boolean b_ = (boolean) Reflect.invokeMethod(null, Class.forName("net.minecraft.server.v1_12_R1.ContainerBrewingStand.a"), "b_", new Class<?>[] {ItemStack.class}, new Object[] {itemstack}, true);
                    if (f.isAllowed(itemstack1)) {
                        if (!canAddItemToSlot(containerBrewingStand, itemstack1, 3, 4, false)) {
                            itemStackHolder.setItemStack(ItemStack.a);
                            return;
                        }
                    } else if (c_ && itemstack.getCount() == 1) {
                        if (!canAddItemToSlot(containerBrewingStand, itemstack1, 0, 3, false)) {
                            itemStackHolder.setItemStack(ItemStack.a);
                            return;
                        }
                    } else if (b_) {
                        if (!canAddItemToSlot(containerBrewingStand, itemstack1, 4, 5, false)) {
                            itemStackHolder.setItemStack(ItemStack.a);
                            return;
                        }
                    } else if (i >= 5 && i < 32) {
                        if (!canAddItemToSlot(containerBrewingStand, itemstack1, 32, 41, false)) {
                            itemStackHolder.setItemStack(ItemStack.a);
                            return;
                        }
                    } else if (i >= 32 && i < 41) {
                        if (!canAddItemToSlot(containerBrewingStand, itemstack1, 5, 32, false)) {
                            itemStackHolder.setItemStack(ItemStack.a);
                            return;
                        }
                    } else if (!canAddItemToSlot(containerBrewingStand, itemstack1, 5, 41, false)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }
                } else {
                    if (!canAddItemToSlot(containerBrewingStand, itemstack1, 5, 41, true)) {
                        itemStackHolder.setItemStack(ItemStack.a);
                        return;
                    }

                    slot.a(itemstack1, itemstack);
                }

                if (itemstack1.isEmpty()) {
                    slot.set(ItemStack.a);
                } else {
                    slot.f();
                }

                if (itemstack1.getCount() == itemstack.getCount()) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(entityhuman, itemstack1);
            }

            itemStackHolder.setItemStack(itemstack);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void containerBeacon(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerBeacon containerBeacon = (ContainerBeacon) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerBeacon.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();

            Slot f = (Slot) Reflect.getField(containerBeacon, "f", false);

            if (i == 0) {
                if (!canAddItemToSlot(containerBeacon, itemstack1, 1, 37, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (!f.hasItem() && f.isAllowed(itemstack1) && itemstack1.getCount() == 1) {
                if (!canAddItemToSlot(containerBeacon, itemstack1, 0, 1, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 1 && i < 28) {
                if (!canAddItemToSlot(containerBeacon, itemstack1, 28, 37, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (i >= 28 && i < 37) {
                if (!canAddItemToSlot(containerBeacon, itemstack1, 1, 28, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerBeacon, itemstack1, 1, 37, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            slot.a(entityhuman, itemstack1);
        }

        itemStackHolder.setItemStack(itemstack);
    }


    private void containerAnvil(Container container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
        ContainerAnvil containerAnvil = (ContainerAnvil) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerAnvil.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.cloneItemStack();
            if (i == 2) {
                if (!canAddItemToSlot(containerAnvil, itemstack1, 3, 39, true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }

                slot.a(itemstack1, itemstack);
            } else if (i != 0 && i != 1) {
                if (i >= 3 && i < 39 && !canAddItemToSlot(containerAnvil, itemstack1, 0, 2, false)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    return;
                }
            } else if (!canAddItemToSlot(containerAnvil, itemstack1, 3, 39, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                itemStackHolder.setItemStack(ItemStack.a);
                return;
            }

            slot.a(entityhuman, itemstack1);
        }

        itemStackHolder.setItemStack(itemstack);
    }


    protected boolean canAddItemToSlot(Container container, ItemStack itemstack, int i, int j, boolean flag) {
        boolean flag1 = false;
        int k = i;
        if (flag) {
            k = j - 1;
        }

        Slot slot;
        ItemStack itemstack1;
        if (itemstack.isStackable() || getCustomItemManager().isItemStackable(itemstack)) {
            while (!itemstack.isEmpty()) {
                if (flag) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = container.slots.get(k);
                itemstack1 = slot.getItem();
//                if (!itemstack1.isEmpty() && itemstack1.getItem() == itemstack.getItem() && (!itemstack.usesData() || itemstack.getData() == itemstack1.getData()) && ItemStack.equals(itemstack, itemstack1)) {
                if (!itemstack1.isEmpty() && getCustomItemManager().itemsSimilar(itemstack, itemstack1)) {
                    int l = itemstack1.getCount() + itemstack.getCount();
                    if (l <= 64) {
//                    if (l <= itemstack.getMaxStackSize()) {
                        itemstack.setCount(0);
                        itemstack1.setCount(l);
                        slot.f();
                        flag1 = true;
//                    } else if (itemstack1.getCount() < itemstack.getMaxStackSize()) {
                    } else if (itemstack1.getCount() < 64) {
                        itemstack.subtract(64 - itemstack1.getCount());
                        itemstack1.setCount(64);
                        slot.f();
                        flag1 = true;
                    }
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            if (flag) {
                k = j - 1;
            } else {
                k = i;
            }

            while (true) {
                if (flag) {
                    if (k < i) {
                        break;
                    }
                } else if (k >= j) {
                    break;
                }

                slot = container.slots.get(k);
                itemstack1 = slot.getItem();
                if (itemstack1.isEmpty() && slot.isAllowed(itemstack)) {
//                    if (itemstack.getCount() > slot.getMaxStackSize()) {
                    if (itemstack.getCount() > 64) {
                        slot.set(itemstack.cloneAndSubtract(slot.getMaxStackSize()));
                    } else {
                        slot.set(itemstack.cloneAndSubtract(itemstack.getCount()));
                    }

                    slot.f();
                    flag1 = true;
                    break;
                }

                if (flag) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag1;
    }


    private boolean contains(Inventory inventory, ItemStack item) {
        if (item == null) {
            return false;
        } else {
            org.bukkit.inventory.ItemStack craftItemStack = CraftItemStack.asBukkitCopy(item);
            org.bukkit.inventory.ItemStack[] var5;
            int var4 = (var5 = inventory.getStorageContents()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                org.bukkit.inventory.ItemStack i = var5[var3];
                if (getCustomItemManager().itemsSimilar(craftItemStack, i)) {
                    return true;
                }
            }

            return false;
        }
    }


    private class ItemStackHolder {
        private ItemStack itemStack;

        public void setItemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return this.itemStack;
        }
    }

}
