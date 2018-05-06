package com.uddernetworks.space.blocks;

import com.uddernetworks.space.guis.CustomGUI;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AnimatedBlock extends CustomBlock {

    short[][] damages;
    private double speed = 1;
    private List<AnimatingDamages> animatingDamages = new ArrayList<>();

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

    public void setDamages(Block block, short[] damages) {
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
                    callback.accept(damages[0]);
                    return;
                }

                int direction = Integer.valueOf(directionString);

                callback.accept(damages[direction]);
            });
        }
    }

    public void stopAnimation(Block block) {
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

    @Override
    public void getGUI(Block blockInstance, Consumer<CustomGUI> customGUIConsumer) {
        main.getBlockDataManager().getData(blockInstance, "inventoryID", inventoryID -> {
            if (inventoryID == null || main.getGUIManager().getGUI(UUID.fromString(inventoryID)) == null) {
                CustomGUI customGUI = getCustomGUISupplier() == null ? null : main.getGUIManager().addGUI(getCustomGUISupplier().get());
                if (customGUI == null) return;
                customGUI.setParentBlock(blockInstance);
                main.getBlockDataManager().setData(blockInstance, "inventoryID", customGUI.getUUID(), () -> {
                    main.getBlockDataManager().getData(blockInstance, "direction", data -> {
                        int direction = Integer.valueOf(data);
                        setDamages(blockInstance, damages[direction]);
                        startAnimation(blockInstance);

                        setTypeTo(blockInstance, damages[direction][0]);
                        if (isElectrical()) main.getCircuitMapManager().addBlock(blockInstance);
                        if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
                    });
                });
            } else {
                CustomGUI customGUI = main.getGUIManager().getGUI(UUID.fromString(inventoryID));
                if (customGUI != null) customGUI.setParentBlock(blockInstance);
                if (customGUIConsumer != null) customGUIConsumer.accept(customGUI);
            }
        });
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
                try {
                    currentCycleIndex.forEach((block, mutableInt) -> {
                        try {
                            if (running.containsKey(block) && running.get(block)) {
                                int currentIndex = currentCycleIndex.get(block).getValue();

                                main.getCustomBlockManager().setBlockData(block.getWorld(), block, getMaterial(), damages[currentIndex]);

                                currentCycleIndex.get(block).increment();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
