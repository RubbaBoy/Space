package com.uddernetworks.space.main;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import com.uddernetworks.space.utils.QuadConsumer;
import com.uddernetworks.space.utils.Reflect;
import io.netty.channel.*;
import net.minecraft.server.v1_12_R1.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

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

        getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).forEach(this::create);

        getServer().getPluginManager().registerEvents(this.customItemManager, this);
        getServer().getPluginManager().registerEvents(this.customBlockManager, this);
//        getServer().getPluginManager().registerEvents(new ItemStacker(this), this);

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
        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).forEach(this::remove);
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

//                    ItemStack itemstack2 = playerEntity.activeContainer.a(packetPlayInWindowClick.b(), packetPlayInWindowClick.c(), packetPlayInWindowClick.f(), playerEntity);
                }

                super.channelRead(context, packet);
            }

            /**
             * Writes packets
             */
            @Override
            public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(context, packet, channelPromise);
            }
        };

        remove(player);

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
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\npipeline.getClass() = " + pipeline.getClass());
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler); // "packet_handler", --- beforeeeeeeee
    }


    public void a(PacketPlayInWindowClick packetplayinwindowclick, EntityPlayer player) throws InterruptedException {
        System.out.println("11111 packetplayinwindowclick = [" + packetplayinwindowclick + "], player = [" + player + "]");
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
            System.out.println("packetplayinwindowclick.f() = " + packetplayinwindowclick.f());
            /*
                1 PICKUP
                2 QUICK_MOVE
                3 SWAP
                4 CLONE
                5 THROW
                6 QUICK_CRAFT
                7 PICKUP_ALL
             */
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
                            System.out.println("111");
                            if (!slot.isAllowed(player)) {
                                System.out.println("222");
                                break;
                            }
                            System.out.println("333");

                            if (cursor.isEmpty()) {
                                System.out.println("444");
                                action = packetplayinwindowclick.c() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                System.out.println("action = " + action);
                            } else if (slot.isAllowed(cursor)) {
                                if (getCustomItemManager().itemsSimilar(clickedItem, cursor)) {
                                    System.out.println("555");
//                                if (true) {
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

                                    System.out.println("action = " + action + " toPlace = " + toPlace);
                                } else if (cursor.getCount() <= slot.getMaxStackSize()) {
                                    System.out.println("SWAP");
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
//                        if (true) {
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
//                        CraftInventory
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
                        System.out.println("DENY");
                        switch (action) {
                            case NOTHING:
                            default:
                                System.out.println("-1 -1 -1");
                                break;
                            case PICKUP_ALL:
                            case MOVE_TO_OTHER_INVENTORY:
                            case HOTBAR_MOVE_AND_READD:
                            case HOTBAR_SWAP:
                            case COLLECT_TO_CURSOR:
                            case UNKNOWN:
                                System.out.println("777");
                                player.updateInventory(player.activeContainer);
                                break;
                            case PICKUP_SOME:
                            case PICKUP_HALF:
                            case PICKUP_ONE:
                            case PLACE_ALL:
                            case PLACE_SOME:
                            case PLACE_ONE:
                            case SWAP_WITH_CURSOR:
                                System.out.println("888");
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, player.inventory.getCarried()));
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, packetplayinwindowclick.b(), player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                                break;
                            case DROP_ALL_CURSOR:
                            case DROP_ONE_CURSOR:
                            case CLONE_STACK:
                                System.out.println("999");
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, player.inventory.getCarried()));
                                break;
                            case DROP_ALL_SLOT:
                            case DROP_ONE_SLOT:
                                System.out.println("10 10 10");
                                player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, packetplayinwindowclick.b(), player.activeContainer.getSlot(packetplayinwindowclick.b()).getItem()));
                        }

                        return;
                    case DEFAULT: // 2
                    case ALLOW: // 3
                        System.out.println("Default / Allow");
                        itemstack = a(player.activeContainer, packetplayinwindowclick.b(), packetplayinwindowclick.c(), packetplayinwindowclick.f(), player);
                    default:
                        if (event instanceof CraftItemEvent) {
                            player.updateInventory(player.activeContainer);
                        }
                }
            }

            System.out.println(packetplayinwindowclick.e() + " == " + itemstack);
            if (ItemStack.matches(packetplayinwindowclick.e(), itemstack)) {
//            if (getCustomItemManager().itemsSimilar(packetplayinwindowclick.e(), itemstack)) {

                System.out.println("FIRST");
                player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.a(), packetplayinwindowclick.d(), true));
                player.f = true;
                player.activeContainer.b();
                player.broadcastCarriedItem();
                player.f = false;

                player.updateInventory(player.activeContainer);
            } else {
                System.out.println("SECOND");
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
            System.out.println("\n\ninventoryclicktype = " + inventoryclicktype);
            System.out.println(" entityhuman.abilities.canInstantlyBuild = " + entityhuman.abilities.canInstantlyBuild);
            if (inventoryclicktype != InventoryClickType.PICKUP && inventoryclicktype != InventoryClickType.QUICK_MOVE || j != 0 && j != 1) {
//                System.out.println("1111111111111111");
                if (inventoryclicktype == InventoryClickType.SWAP && j >= 0 && j < 9) {
                    slot2 = (Slot) container.slots.get(i);
                    itemstack2 = playerinventory.getItem(j);
                    itemstack1 = slot2.getItem();
                    if (!itemstack2.isEmpty() || !itemstack1.isEmpty()) {
                        if (itemstack2.isEmpty()) {
                            if (slot2.isAllowed(entityhuman)) {
                                playerinventory.setItem(j, itemstack1);
                                Reflect.invokeMethod(slot2, "b", new Class<?>[] {int.class}, new Object[] {itemstack1.getCount()}, false);
//                                slot2.b(itemstack1.getCount());
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
//                        itemstack2 = slot2.getItem().cloneItemStack();
                        System.out.println("CLONEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
                        itemstack2 = slot2.getItem().cloneItemStack();
                        itemstack2.setCount(getCustomItemManager().getMaxStackSize(itemstack2));
                        System.out.println("itemstack2 = " + itemstack2);
//                        itemstack2.setCount(itemstack2.getMaxStackSize());
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
                System.out.println("111");
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
                System.out.println("Quick moved!!!");
                if (i < 0) {
                    return ItemStack.a;
                }

                slot2 = (Slot) container.slots.get(i);
                if (slot2 == null || !slot2.isAllowed(entityhuman)) {
                    return ItemStack.a;
                }

                System.out.println("STARTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT is = " + slot2.getItem());

//                for(itemstack2 = shiftClick(container, entityhuman, i); !itemstack2.isEmpty() && c(slot2.getItem(), itemstack2); itemstack2 = shiftClick(container, entityhuman, i)) {
//                for(itemstack2 = shiftClick(container, entityhuman, i); !itemstack2.isEmpty() && getCustomItemManager().itemsSimilar(slot2.getItem(), itemstack2); itemstack2 = shiftClick(container, entityhuman, i)) {
                for(itemstack2 = shiftClick(container, entityhuman, i); !itemstack2.isEmpty() && c(slot2.getItem(), itemstack2); itemstack2 = shiftClick(container, entityhuman, i)) {
//                    itemstack = itemstack2.cloneItemStack();
//                }
                    System.out.println("tyttttt 666");
                    itemstack = itemstack2.cloneItemStack();
                }

                System.out.println("ENDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD slot is now = " + (container.slots.get(i).getItem()));
            } else {
                System.out.println("222");
                if (i < 0) {
                    return ItemStack.a;
                }

                slot2 = (Slot) container.slots.get(i);
                if (slot2 != null) {
                    System.out.println("333");
                    itemstack2 = slot2.getItem();
                    itemstack1 = playerinventory.getCarried();
                    if (!itemstack2.isEmpty()) {
                        itemstack = itemstack2.cloneItemStack();
                    }

                    System.out.println("444");
                    if (itemstack2.isEmpty()) {
                        System.out.println("555");
                        if (!itemstack1.isEmpty() && slot2.isAllowed(itemstack1)) {
                            k1 = j == 0 ? itemstack1.getCount() : 1;
                            if (k1 > slot2.getMaxStackSize(itemstack1)) {
                                k1 = slot2.getMaxStackSize(itemstack1);
                            }

                            System.out.println("666");
                            slot2.set(itemstack1.cloneAndSubtract(k1));
                        }
                    } else if (slot2.isAllowed(entityhuman)) {
                        System.out.println("777");
                        if (itemstack1.isEmpty()) {
                            if (itemstack2.isEmpty()) {
                                System.out.println("888");
                                slot2.set(ItemStack.a);
                                playerinventory.setCarried(ItemStack.a);
                            } else {
                                System.out.println("999");
                                k1 = j == 0 ? itemstack2.getCount() : (itemstack2.getCount() + 1) / 2;
                                playerinventory.setCarried(slot2.a(k1));
                                if (itemstack2.isEmpty()) {
                                    slot2.set(ItemStack.a);
                                }

                                slot2.a(entityhuman, playerinventory.getCarried());
                            }
                        } else if (slot2.isAllowed(itemstack1)) {
                            System.out.println("10 10 10");
//                            if (itemstack2.getItem() == itemstack1.getItem() && ItemBuilder.itemsEquals(itemstack2, itemstack1)) {
                            if (getCustomItemManager().getMaxStackSize(itemstack1) > 1 && getCustomItemManager().itemsSimilar(itemstack2, itemstack1)) {
                                System.out.println("14 14 14");
                                System.out.println("j = " + j);

                                k1 = j == 0 ? itemstack1.getCount() : 1;

                                // 1 is in hand

                                System.out.println("itemstack1 = " + itemstack1);
                                System.out.println("itemstack2 = " + itemstack2);

                                System.out.println("k1 = " + k1);

                                // k1 is 32
                                // item1 is 16
                                // item2 is 32

//                                if (k1 > slot2.getMaxStackSize(itemstack1) - itemstack2.getCount()) {
                                if (k1 > 64 - itemstack2.getCount()) {
                                    k1 = 64 - itemstack2.getCount();
                                }

                                if (k1 > 64 - itemstack2.getCount()) {
                                    k1 = 64 - itemstack2.getCount();
                                }

                                System.out.println("k1 = " + k1);

                                itemstack1.subtract(k1);
                                itemstack2.add(k1);

                                System.out.println("itemstack1 = " + itemstack1);
                                System.out.println("itemstack2 = " + itemstack2);

//                                Bukkit.getScheduler().runTaskLater(this, () -> {
//                                    playerinventory.update();
//                                }, 50L);

                            } else if (itemstack1.getCount() <= slot2.getMaxStackSize(itemstack1)) {
                                System.out.println("11 11 11");
                                slot2.set(itemstack1);
                                playerinventory.setCarried(itemstack2);
                            }
//                        } else if (itemstack2.getItem() == itemstack1.getItem() && itemstack1.getMaxStackSize() > 1 && (!itemstack2.usesData() || itemstack2.getData() == itemstack1.getData()) && ItemStack.equals(itemstack2, itemstack1) && !itemstack2.isEmpty()) {
                        } else if (itemstack1.getMaxStackSize() > 1 && getCustomItemManager().itemsSimilar(itemstack2, itemstack1)) {
                            System.out.println("12 12 12");
                            k1 = itemstack2.getCount();
                            if (k1 + itemstack1.getCount() <= itemstack1.getMaxStackSize()) {
                                System.out.println("13 13 13");
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
//        return itemstack == itemstack1 ? true : (!itemstack.isEmpty() && !itemstack1.isEmpty() ? itemstack.doMaterialsMatch(itemstack1) : false);
    }

    private ItemStack shiftClick(Container container, EntityHuman entityhuman, int i) {
        ItemStackHolder itemStackHolder = new ItemStackHolder();
        System.out.println("container.getClass() = " + container.getClass());
        if (clickActions.containsKey(container.getClass())) {
            System.out.println("Contains!");
            clickActions.get(container.getClass()).accept(container, entityhuman, i, itemStackHolder);
        } else {
            System.out.println("NO CONTAINS");
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
        System.out.println("CHESTTTTTTTTTTT iiiii == " + i);
        ContainerChest containerChest = (ContainerChest) container;
        ItemStack itemstack = ItemStack.a;
        Slot slot = (Slot) containerChest.slots.get(i);
        System.out.println((slot != null) + " and " + slot.hasItem());
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            System.out.println("Before item = " + itemstack1);
            itemstack = itemstack1.cloneItemStack();
            int f = (int) Reflect.getField(containerChest, "f", false);
            if (i < f * 9) {
                if (!canAddItemToSlot(containerChest, itemstack1, f * 9, containerChest.slots.size(), true)) {
                    itemStackHolder.setItemStack(ItemStack.a);
                    System.out.println("Air");
                    return;
                }
            } else if (!canAddItemToSlot(containerChest, itemstack1, 0, f * 9, false)) {
                itemStackHolder.setItemStack(ItemStack.a);
                System.out.println("Air");
                return;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.a);
            } else {
                slot.f();
            }
        }

        System.out.println("Got other shit");
        System.out.println("itemstack = " + itemstack);
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
//                } else if (ContainerBrewingStand.SlotPotionBottle.c_(itemstack) && itemstack.getCount() == 1) {
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
//                        itemstack.subtract(itemstack.getMaxStackSize() - itemstack1.getCount());
//                        itemstack1.setCount(itemstack.getMaxStackSize());
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


//    private ItemStack shiftClick(CraftContainer container, EntityHuman entityhuman, int i, ItemStackHolder itemStackHolder) {
//        return container.delegate != null ? container.delegate.shiftClick(entityhuman, i) : container.shiftClick(entityhuman, i);
//    }
    /**/


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
