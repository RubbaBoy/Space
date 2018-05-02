package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.MutableInt;
import com.uddernetworks.space.utils.Reflect;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

public class CustomBlockManager implements Listener {

    private Main main;
    private List<CustomBlock> customBlocks = new ArrayList<>();
//    private float currSpeed = 1F;

    public CustomBlockManager(Main main) {
        this.main = main;
    }

    public void addCustomBlock(CustomBlock customBlock) {
        if (!customBlocks.contains(customBlock)) customBlocks.add(customBlock);
    }

    public CustomBlock getCustomBlock(Material material, short data) {
        for (CustomBlock customBlock : customBlocks) {
            if (customBlock.getMaterial() == material && customBlock.getDamage() == data) return customBlock;
        }

        return null;
    }

    private void sendArmSwing(Player player, EquipmentSlot slot) {
        Entity playerEntity = ((CraftPlayer) player).getHandle();

        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(playerEntity, slot == EquipmentSlot.HAND ? 0 : 1);

        Bukkit.getOnlinePlayers().forEach(forPlayer -> ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(animation));
    }

//    public void getCustomBlock(Block block, Consumer<CustomBlock> customBlockConsumer) {
//        main.getBlockDataManager().getData(block, "customBlock", customBlockString -> {
//            if (!StringUtils.isNumeric(customBlockString)) customBlockConsumer.accept(null);
//
//            customBlockConsumer.accept((CustomBlock) main.getCustomIDManager().getByID(Integer.valueOf(customBlockString)));
//        });
//    }

    public CustomBlock getCustomBlock(String name) {
        for (CustomBlock customBlock : customBlocks) {
            if (customBlock.getName().equalsIgnoreCase(name)) return customBlock;
        }

        return null;
    }

//    public CustomBlock getCustomBlock(int id) {
//        for (CustomBlock customBlock : customBlocks) {
//            if (customBlock.getName().equalsIgnoreCase(name)) return customBlock;
//        }
//
//        return null;
//    }

    private net.minecraft.server.v1_12_R1.Material getNMSMaterial(net.minecraft.server.v1_12_R1.Block nmsBlock) {
        return nmsBlock.q(null);
    }

    public List<CustomBlock> getCustomBlocks() {
        return new ArrayList<>(customBlocks);
    }

    @EventHandler
    public void onDestroyEvent(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(block);

        if (customBlock == null) return;

        customBlock.spawnParticles(block);

        if (player.getGameMode() != GameMode.CREATIVE) block.getWorld().dropItemNaturally(block.getLocation(), customBlock.toItemStack());

        if (!customBlock.onBreak(block, player)) {
            System.out.println("Cancel");
            event.setCancelled(true);
            return;
        }
        System.out.println("Go");

        if (customBlock.hasGUI()) {
            customBlock.getGUI(block, customGUI -> {
                if (customGUI != null) {
                    main.getGUIManager().removeGUI(customGUI.getUUID());
                }

                main.getBlockDataManager().deleteData(block, () -> {});
            });
        } else {
            main.getBlockDataManager().deleteData(block, () -> {});
        }
//        main.getGUIManager().removeGUI();
        System.out.println("Done! Cancelled = " + event.isCancelled());

        block.setType(Material.AIR);

//        block.setMetadata("inventoryID", new FixedMetadataValue(main, new ArrayList<>()));
//        block.setMetadata("material", new FixedMetadataValue(main, new ArrayList<>()));
//        block.setMetadata("damage", new FixedMetadataValue(main, new ArrayList<>()));
    }

    @EventHandler
    public void onInteractWithCustomBlock(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) return;

        if (player.isSneaking() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(clicked);

        if (customBlock == null) return;

        customBlock.onClick(event);

        if (!customBlock.hasGUI()) return;
        event.setCancelled(false);

        customBlock.getGUI(clicked, customGUI -> {
            if (customGUI != null) {
                player.openInventory(customGUI.getInventory());
            }
        });
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getItem() == null) return;

        if (!player.isSneaking() && event.getClickedBlock().getState() instanceof InventoryHolder) return;

        CustomBlock customBlockClicked = main.getBlockDataManager().getCustomBlock(event.getClickedBlock());

        if (!player.isSneaking() && (customBlockClicked != null && customBlockClicked.hasGUI())) return;

        CustomBlock customBlock = getCustomBlock(item.getType(), item.getDurability());

        if (customBlock == null) return;

        BlockFace blockFace = event.getBlockFace();
        Block clickedBlock = event.getClickedBlock();
        World world = clickedBlock.getWorld();


        Block toPlaceBlock = null;

        switch (blockFace) {
            case NORTH:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(0, 0, -1));
                break;
            case SOUTH:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(0, 0, 1));
                break;
            case EAST:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(1, 0, 0));
                break;
            case WEST:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(-1, 0, 0));
                break;
            case UP:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(0, 1, 0));
                break;
            case DOWN:
                toPlaceBlock = world.getBlockAt(clickedBlock.getLocation().add(0, -1, 0));
                break;
            default:
                System.out.println("Uncalled for block face! " + blockFace);
                break;
        }

        if (toPlaceBlock == null || !toPlaceBlock.isEmpty()) return;

        BlockPrePlace blockPrePlace = new BlockPrePlace(customBlock.getID(), customBlock.getDamage());

        Block finalToPlaceBlock = toPlaceBlock;
        Runnable runnable = () -> {
            if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);

            main.getBlockDataManager().setData(finalToPlaceBlock, "customBlock", blockPrePlace.getId(), () -> {
                sendArmSwing(player, event.getHand());

                setBlockData(player.getWorld(), finalToPlaceBlock, customBlock.getMaterial(), (short) blockPrePlace.getDamage());

                customBlock.onPlace(finalToPlaceBlock, player);
            });

            event.setCancelled(true);
        };

        System.out.println("1111111111111111111111");

        blockPrePlace.setCallback(runnable);

        if (!customBlock.onPrePlace(toPlaceBlock, player, blockPrePlace)) return;

        if (blockPrePlace.isUsingCallback()) {
            event.setCancelled(true);
        } else {
            runnable.run();
        }
    }

    public void setBlockData(World world, Block toPlaceBlock, Material material, short damage) {
        toPlaceBlock.setType(Material.MOB_SPAWNER);

        CreatureSpawner cs = (CreatureSpawner) toPlaceBlock.getState();

        CraftCreatureSpawner ccs = (CraftCreatureSpawner) cs;
        TileEntityMobSpawner tileEntityMobSpawner = (TileEntityMobSpawner) Reflect.invokeMethod(ccs, CraftBlockEntityState.class, "getTileEntity", false);

        MobSpawnerAbstract mobSpawnerAbstract = tileEntityMobSpawner.getSpawner();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setShort("RequiredPlayerRange", (short) 0);

        EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) world).getHandle(), toPlaceBlock.getX(), toPlaceBlock.getY(), toPlaceBlock.getZ());

        mobSpawnerAbstract.spawnDelay = 0;

        NBTTagCompound nbt = new NBTTagCompound();

        entityArmorStand.b(nbt);

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < 3; i++) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            nbttaglist.add(nbttagcompound1);
        }

        NBTTagCompound nbttagcompound1;

        net.minecraft.server.v1_12_R1.ItemStack itemstack = new net.minecraft.server.v1_12_R1.ItemStack(net.minecraft.server.v1_12_R1.Item.getById(material.getId()));

        nbttagcompound1 = new NBTTagCompound();
        itemstack.save(nbttagcompound1);

        nbttagcompound1.setShort("Damage", damage);
        nbttagcompound1.setBoolean("Unbreakable", true);

        nbttaglist.add(nbttagcompound1);

        nbt.set("ArmorItems", nbttaglist);

        NBTTagCompound pose = new NBTTagCompound();

        NBTTagList var1 = new NBTTagList();
        var1.add(new NBTTagFloat(10));
        var1.add(new NBTTagFloat(0));
        var1.add(new NBTTagFloat(0));
        pose.set("Head", var1);

        nbt.set("Pose", pose);

        entityArmorStand.a(nbt);

        NBTTagCompound nbt222 = new NBTTagCompound();

        entityArmorStand.b(nbt222);

        nbt222.setString("id", "minecraft:armor_stand");
        nbt222.setBoolean("Invisible", true);
        nbt222.setBoolean("Marker", true);


        MobSpawnerData mobSpawnerData = new MobSpawnerData(0, nbt222);

        mobSpawnerAbstract.a(mobSpawnerData);

        NBTTagCompound stopSpamingCrap = new NBTTagCompound();
        stopSpamingCrap.setShort("MaxNearbyEntities", (short) 0);
        stopSpamingCrap.setShort("RequiredPlayerRange", (short) 0);
        stopSpamingCrap.setShort("MaxSpawnDelay", (short) 0);
        stopSpamingCrap.setShort("MinSpawnDelay", (short) 0);
        stopSpamingCrap.setShort("SpawnRange", (short) 0);

        mobSpawnerAbstract.a(stopSpamingCrap);
    }


    class BlockPrePlace {
        private int id;
        private int damage;
        private boolean usingCallback = false;
        private Runnable callback;

        public BlockPrePlace(int id, int damage) {
            this.id = id;
            this.damage = damage;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDamage() {
            return damage;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }

        public boolean isUsingCallback() {
            return usingCallback;
        }

        public void setUsingCallback(boolean usingCallback) {
            this.usingCallback = usingCallback;
        }

        public void setCallback(Runnable callback) {
            this.callback = callback;
        }

        public Runnable getCallback() {
            return callback;
        }
    }
}
