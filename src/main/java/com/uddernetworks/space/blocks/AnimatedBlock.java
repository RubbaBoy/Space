package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import com.uddernetworks.space.utils.MutableInt;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AnimatedBlock extends CustomBlock {

//    private Consumer<Player> openInventory;

    private short[] damages;
    private Map<Block, BukkitTask> fastTasks = new HashMap<>();
    private Map<Block, MutableInt> currentCycleIndex = new HashMap<>();

    public AnimatedBlock(Main main, int id, Material material, short[] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages[0], particle, name, customGUISupplier);

        this.damages = damages;
    }

    // Speed is the time it takes for one full animation in seconds
    public void startAnimation(Block block, double speed) {
        System.out.println("block = " + block);
        currentCycleIndex.put(block, new MutableInt().setLoopbackCap(damages.length - 1));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(main, () -> {
            int currentIndex = currentCycleIndex.get(block).getValue();

            if (block.getWorld().getPlayers().size() <= 0) return;

//            setAnimation(block, damages[currentIndex]);

            main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), damages[currentIndex]);

            currentCycleIndex.get(block).increment();
        }, 0, Double.valueOf(speed / damages.length * 20).longValue());

//        FastTask task = new FastTask(main).runRepeatingTask(false, () -> {

//        }, 0, speed / damages.length);

        fastTasks.put(block, task);
    }

    public void stopAnimation(Block block) {
        if (fastTasks.containsKey(block)) {
            fastTasks.get(block).cancel();
            fastTasks.remove(block);
        }
    }

    public void resetAnimation(Block block) {
        stopAnimation(block);
        setAnimation(block, damages[0]);
    }

    public void setAnimation(Block block, int data) {

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendBlockChange(block.getLocation(), block.getType(), (byte) data);
        });

//        World world = ((CraftWorld) block.getLocation().getWorld()).getHandle();
//        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
//
//        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(world, blockPosition);
//
//        int combined = block.getTypeId() + (data << 12);
//        packetPlayOutBlockChange.block = net.minecraft.server.v1_12_R1.Block.getByCombinedId(combined);
//
//        Bukkit.getOnlinePlayers().stream().map(CraftPlayer.class::cast).map(CraftPlayer::getHandle).forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutBlockChange));
    }


    @Override
    boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player) {
        return true;
    }

    @Override
    void onPlace(Block block, Player player) {

    }

    @Override
    void onClick(PlayerInteractEvent event) {
//        openInventory.accept(event.getPlayer());
    }

    @Override
    boolean hasGUI() {
        return true;
    }
}
