package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.Reflect;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
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

    public CustomBlock getCustomBlock(Block block) {
        List<MetadataValue> materialMetaList = block.getMetadata("material");
        List<MetadataValue> damageMetaList = block.getMetadata("damage");

        if (materialMetaList.size() == 0 || damageMetaList.size() == 0) return null;

        String material = materialMetaList.get(0).asString();
        short damage = damageMetaList.get(0).asShort();

        return main.getCustomBlockManager().getCustomBlock(Material.getMaterial(material), damage);
    }

    public CustomBlock getCustomBlock(String name) {
        for (CustomBlock customBlock : customBlocks) {
            if (customBlock.getName().equalsIgnoreCase(name)) return customBlock;
        }

        return null;
    }

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

        CustomBlock customBlock = getCustomBlock(block);

        if (customBlock == null) return;

        customBlock.spawnParticles(block);

        if (player.getGameMode() != GameMode.CREATIVE) block.getWorld().dropItemNaturally(block.getLocation(), customBlock.toItemStack());

        if (!customBlock.onBreak(block, player)) {
            event.setCancelled(true);
            return;
        }

//        block.setMetadata("inventoryID", new FixedMetadataValue(main, new ArrayList<>()));
//        block.setMetadata("material", new FixedMetadataValue(main, new ArrayList<>()));
//        block.setMetadata("damage", new FixedMetadataValue(main, new ArrayList<>()));
    }

    @EventHandler
    public void onInteractWithCustomBlock(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;

//        System.out.println("11111");

        if (player.isSneaking() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
//        System.out.println("22222");
        CustomBlock customBlock = getCustomBlock(clicked);
//        System.out.println("33333");
        if (customBlock == null) return;
//        System.out.println("4444444");
        customBlock.onClick(event);
//        System.out.println("555555");
        CustomGUI customGUI = customBlock.getGUI(clicked);
//        System.out.println("6666666");
        if (customGUI == null) return;
//        System.out.println("77777777");
        event.setCancelled(false);
        player.openInventory(customGUI.getInventory());
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        System.out.println("1111");
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;
        System.out.println("222");
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        System.out.println("333");
        if (event.getItem() == null) return;
        System.out.println("444");
        if (!player.isSneaking() && event.getClickedBlock().getState() instanceof InventoryHolder) return;
        System.out.println("555");
        CustomBlock customBlockClicked = getCustomBlock(event.getClickedBlock());
        System.out.println("666");
        if (!player.isSneaking() && (customBlockClicked != null && customBlockClicked.getGUI(event.getClickedBlock()) != null)) return;
        System.out.println("777");
        CustomBlock customBlock = getCustomBlock(item.getType(), item.getDurability());
        System.out.println("888");
        if (customBlock == null) return;
        System.out.println("999");
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
        System.out.println("101010101:::: " + toPlaceBlock);
        if (toPlaceBlock == null || !toPlaceBlock.isEmpty()) return;
        System.out.println("11 11 11");
        if (!customBlock.onPrePlace(toPlaceBlock, player)) return;
        System.out.println("22 22 22");
        if (player.getGameMode() != GameMode.CREATIVE) item.setAmount(item.getAmount() - 1);
        System.out.println("33 33 33");
        toPlaceBlock.setMetadata("material", new FixedMetadataValue(main, customBlock.getMaterial().name()));
        toPlaceBlock.setMetadata("damage", new FixedMetadataValue(main, customBlock.getDamage()));

        if (customBlock.hasGUI()) {
            toPlaceBlock.setMetadata("inventoryID", new FixedMetadataValue(main, customBlock.getGUI(toPlaceBlock).getUUID()));
        }

        System.out.println("44 44 44");
        sendArmSwing(player, event.getHand());

        setBlockData(player, toPlaceBlock, customBlock.getMaterial(), customBlock.getDamage());

        event.setCancelled(true);

        System.out.println("10101010101");
        customBlock.onPlace(toPlaceBlock, player);
    }

    public void setBlockData(Player player, Block toPlaceBlock, Material material, short damage) {
        toPlaceBlock.setType(Material.MOB_SPAWNER);

        CreatureSpawner cs = (CreatureSpawner) toPlaceBlock.getState();

        CraftCreatureSpawner ccs = (CraftCreatureSpawner) cs;
        TileEntityMobSpawner tileEntityMobSpawner = (TileEntityMobSpawner) Reflect.invokeMethod(ccs, CraftBlockEntityState.class, "getTileEntity", false);

        MobSpawnerAbstract mobSpawnerAbstract = tileEntityMobSpawner.getSpawner();

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setShort("RequiredPlayerRange", (short) 0);

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        EntityArmorStand entityArmorStand = new EntityArmorStand(entityPlayer.getWorld(), toPlaceBlock.getX(), toPlaceBlock.getY(), toPlaceBlock.getZ());

//        entityArmorStand.setEquipment(EnumItemSlot.MAINHAND, ItemBuilder.from(Material.DIAMOND_HOE).setDamage());

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
}
