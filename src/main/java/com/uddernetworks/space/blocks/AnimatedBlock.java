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
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AnimatedBlock extends CustomBlock {

//    private Consumer<Player> openInventory;

    private short[][] damages;
    private Map<Block, short[]> animations = new HashMap<>();
    private Map<Block, BukkitTask> fastTasks = new HashMap<>();
    private Map<Block, Double> speeds = new HashMap<>();
    private Map<Block, MutableInt> currentCycleIndex = new HashMap<>();

    public AnimatedBlock(Main main, int id, Material material, short[] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        this(main, id, material, new short[][] {damages}, particle, name, customGUISupplier);
    }

    public AnimatedBlock(Main main, int id, Material material, short[][] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages[0][0], particle, name, customGUISupplier);

        this.damages = damages;
    }

    public void setDamages(Block block, short[] damages) {
        this.animations.put(block, damages);

        if (fastTasks.containsKey(block)) {
            fastTasks.get(block).cancel();
            fastTasks.remove(block);

            startAnimation(block, speeds.get(block));
        }
    }

    // Speed is the time it takes for one full animation in seconds
    public void startAnimation(Block block, double speed) {
        System.out.println("block = " + block);
        speeds.put(block, speed);

        if (fastTasks.containsKey(block)) {
            fastTasks.get(block).cancel();
            fastTasks.remove(block);
        }

        getBlockDamages(block, blockDamages -> {
            currentCycleIndex.put(block, new MutableInt().setLoopbackCap(blockDamages.length - 1));

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(main, () -> {
                int currentIndex = currentCycleIndex.get(block).getValue();

                if (block.getWorld().getPlayers().size() <= 0) return;

                main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), blockDamages[currentIndex]);

                currentCycleIndex.get(block).increment();
            }, 0, Double.valueOf(speed / blockDamages.length * 20).longValue());

            fastTasks.put(block, task);
        });
    }

    public void getBlockDamages(Block block, Consumer<short[]> callback) {
        if (animations.containsKey(block)) {
            callback.accept(animations.get(block));
        } else {
            main.getBlockDataManager().getData(block, "direction", directionString -> {
                if (directionString == null) {
                    animations.put(block, damages[0]);
                    callback.accept(damages[0]);
                    return;
                }

                int direction = Integer.valueOf(directionString);

                animations.put(block, damages[direction]);
                callback.accept(damages[direction]);
            });
        }
    }

    public void stopAnimation(Block block) {
        if (fastTasks.containsKey(block)) {
            fastTasks.get(block).cancel();
            fastTasks.remove(block);
        }
    }

    public void resetAnimation(Block block) {
        stopAnimation(block);

        getBlockDamages(block, damages -> setAnimation(block, damages[0]));
    }

    public void setAnimation(Block block, int data) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendBlockChange(block.getLocation(), block.getType(), (byte) data);
        });
    }


    @Override
    boolean onBreak(Block block, Player player) {
        return true;
    }

    @Override
    boolean onPrePlace(Block block, Player player, CustomBlockManager.BlockPrePlace blockPrePlace) {
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
