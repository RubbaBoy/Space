package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AnimatedBlock extends CustomBlock {

//    private Consumer<Player> openInventory;

    short[][] damages;
    private double speed = 1;
    private List<AnimatingDamages> animatingDamages = new ArrayList<>();
//    private Map<Block, short[]> animations = new HashMap<>();
//    private Map<Block, BukkitTask> fastTasks = new HashMap<>();
//    private Map<Block, Double> speeds = new HashMap<>();
//    private Map<Block, MutableInt> currentCycleIndex = new HashMap<>();

    public AnimatedBlock(Main main, int id, Material material, short[] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        this(main, id, material, new short[][] {damages}, particle, name, customGUISupplier);
    }

    public AnimatedBlock(Main main, int id, Material material, short[][] damages, Material particle, String name, Supplier<CustomGUI> customGUISupplier) {
        super(main, id, material, damages[0][0], false, particle, name, customGUISupplier);

        this.damages = damages;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setDamages(Block block, short[] damages) { // TODO: MORNING: Make sure this class stops shit when needed
        System.out.println("AnimatedBlock.setDamages");
//        this.animations.put(block, damages);
//
//        if (fastTasks.containsKey(block)) {
//            fastTasks.get(block).cancel();
//            fastTasks.remove(block);
//
//            startAnimation(block, speeds.get(block));
//        }

        for (AnimatingDamages animatingDamage : animatingDamages) {
            if (Arrays.equals(animatingDamage.getDamages(), damages)) {
                animatingDamage.addBlock(block);
                return;
            }
        }

        AnimatingDamages animatingDamages = new AnimatingDamages(damages, speed);
        animatingDamages.addBlock(block);
        this.animatingDamages.add(animatingDamages);
    }

    // Speed is the time it takes for one full animation in seconds
    public void startAnimation(Block block) {
        for (AnimatingDamages animatingDamage : animatingDamages) {
            if (animatingDamage.containsBlock(block)) {
                animatingDamage.setRunning(block, true);
            }
        }


//        speeds.put(block, speed);
//
//        if (fastTasks.containsKey(block)) {
//            fastTasks.get(block).cancel();
//            fastTasks.remove(block);
//        }

//        getBlockDamages(block, blockDamages -> {


//            currentCycleIndex.put(block, new MutableInt().setLoopbackCap(blockDamages.length - 1));

//            BukkitTask task = Bukkit.getScheduler().runTaskTimer(main, () -> {
//                System.out.println("111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
//                int currentIndex = currentCycleIndex.get(block).getValue();
//
//                if (block.getWorld().getPlayers().size() <= 0) return;
//
//                main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), blockDamages[currentIndex]);
//
//                currentCycleIndex.get(block).increment();
//            }, 0, Double.valueOf(speed / blockDamages.length * 20).longValue());

//            fastTasks.put(block, task);
//        });
    }

    public short[] getDamages(Block block) {
        Optional<AnimatingDamages> animatedDamages = animatingDamages.stream().filter(animatingDamages1 -> animatingDamages1.containsBlock(block)).findFirst();
        return animatedDamages.map(AnimatingDamages::getDamages).orElse(null);
    }

    public void getBlockDamages(Block block, Consumer<short[]> callback) {
        short[] damageArray = getDamages(block);
        if (damageArray != null) {
            callback.accept(damageArray);
        } else {
            main.getBlockDataManager().getData(block, "direction", directionString -> {
                if (directionString == null) {
//                    animations.put(block, damages[0]);
                    callback.accept(damages[0]);
                    return;
                }

                int direction = Integer.valueOf(directionString);

//                animations.put(block, damages[direction]);
                callback.accept(damages[direction]);
            });
        }
    }

    public void stopAnimation(Block block) {
//        if (fastTasks.containsKey(block)) {
//            fastTasks.get(block).cancel();
//            fastTasks.remove(block);
//        }
        for (AnimatingDamages animatingDamage : animatingDamages) {
            if (animatingDamage.containsBlock(block)) {
                animatingDamage.setRunning(block, false);
            }
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
//        if (fastTasks.containsKey(block)) {
//            System.out.println("Cancelling stuffffffffffff");
//            System.out.println(fastTasks.get(block));
//            Bukkit.getScheduler().cancelTask(fastTasks.get(block).getTaskId());
//            fastTasks.get(block).cancel();
//            fastTasks.remove(block);
//
//            animations.remove(block);
//            speeds.remove(block);
//            currentCycleIndex.remove(block);
//
//            System.out.println("fastTasks = " + fastTasks);
//        } else {
//            System.out.println("NO CONTAINNNNNNN stuffffffffffffff");
//        }
//        stopAnimation(block);
        for (AnimatingDamages animatingDamage : animatingDamages) {
            if (animatingDamage.containsBlock(block)) {
                animatingDamage.removeBlock(block);
            }
        }

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

    private class AnimatingDamages {
        private short[] damages;
        private double speed;
        private Map<Block, Boolean> running = new HashMap<>();
        private Map<Block, MutableInt> currentCycleIndex = new HashMap<>();

        public AnimatingDamages(short[] damages, double speed) {
            this.damages = damages;
            this.speed = speed;


            Bukkit.getScheduler().runTaskTimer(main, () -> {
                currentCycleIndex.forEach((block, mutableInt) -> {
                    System.out.println("111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
                    int currentIndex = currentCycleIndex.get(block).getValue();

                    main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), damages[currentIndex]);

                    currentCycleIndex.get(block).increment();
                });
            }, 0, Double.valueOf(speed / damages.length * 20).longValue());
        }

        public void addBlock(Block block) {
            currentCycleIndex.put(block, new MutableInt().setLoopbackCap(damages.length - 1));
        }

        public void removeBlock(Block block) {
            currentCycleIndex.remove(block);
            running.remove(block);
        }

        public boolean containsBlock(Block block) {
            return currentCycleIndex.containsKey(block);
        }

        public boolean isRunning(Block block) {
            if (!running.containsKey(block)) return false;
            return running.get(block);
        }

        public void setRunning(Block block, boolean running) {
            this.running.put(block, running);
        }

        public short[] getDamages() {
            return damages;
        }

        public double getSpeed() {
            return speed;
        }
    }
}
