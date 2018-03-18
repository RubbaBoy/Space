package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.AnimatedBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ElectricFurnaceGUI extends CustomGUI {

    private Main main;
    private double amount = 0;
    private double update = 1;
    private double speedInSeconds = 5;
    private boolean processing = false;
    private ProgressBar progressBar;
    private FastTask task;
    private FastTask task2;
    private int windowID;
    private AnimatedBlock animatedBlock;

    private int adding = 1;
    private int index = 0;

    public ElectricFurnaceGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.ELECTRIC_FURNACE_MAIN);

        this.main = main;

        this.progressBar = main.getProgressBarManager().getProgressBar("ElectricFurnaceBar");
        this.animatedBlock = (AnimatedBlock) main.getCustomIDManager().getCustomBlockById(115);

        addSlot(new PopulatedSlot(19, false, progressBar.getItemStack(0)));

//        addSlot(new UnsettableSlot(49, this::updateDoing));

        SlotAction slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                updateDoing();
                return true;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                updateDoing();
                return true;
            }
        };

//        addSlot(new OpenSlot(11, slotAction));
//        addSlot(new OpenSlot(15, slotAction));

        updateSlots();

        Bukkit.getScheduler().runTaskLater(main, () -> {
            this.animatedBlock.startAnimation(getParentBlock(), 2);
        }, 40L);

        Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
            PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 19, CraftItemStack.asNMSCopy(progressBar.getItemStack(index / 17D * 100D)));

            getInventory().getViewers().stream()
                    .map(player -> ((CraftPlayer) player).getHandle())
                    .forEach(entityPlayer -> {
                        System.out.println("Updating for: " + entityPlayer.displayName);
                        entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot);
                    });

            index += adding;

            if ((adding > 0 && index >= 17) || (adding < 0 && index < 0)) {
                adding *= -1;

                index += adding;
            }

        }, 0L, 2L);
    }

    private int getWindowID() {
        this.windowID = getInventory().getViewers().size() > 0 ? ((CraftPlayer) getInventory().getViewers().get(0)).getHandle().activeContainer.windowId : this.windowID;
        return this.windowID;
    }

    private void startProcessing() {
        if (!this.processing) {
//            this.processing = true;
//            this.amount = 0;
//
////            animatedBlock.startAnimation(getParentBlock(), 1);
//
//            this.task = new FastTask(main).runRepeatingTask(true, () -> {
//                if (!this.processing) return;
//
//                PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(progressBar.getItemStack(amount)));
//
//                amount += update;
//
//                getInventory().getViewers().stream()
//                        .map(player -> ((CraftPlayer) player).getHandle())
//                        .forEach(entityPlayer -> {
//                            System.out.println("Updating for: " + entityPlayer.displayName);
//                            entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot);
//                        });
//            }, 0L, speedInSeconds / 100);
//
//            this.task2 = new FastTask(main).runTaskLater(false, () -> {
//                if (this.processing) {
//                    this.processing = false;
//
//                    stopProcessing();
//
//                    ItemStack[][] tempGrid = new ItemStack[][] {
//                            {getInventory().getItem(11), getInventory().getItem(15)}
//                    };
//
//                    ItemStack resulting = main.getRecipeManager().getResultingItem(tempGrid, RecipeType.ALLOY_MIXER);
//
//                    ItemStack inSlot = getInventory().getItem(49);
//                    if (inSlot != null && inSlot.isSimilar(resulting)) {
//                        inSlot.setAmount(inSlot.getAmount() + resulting.getAmount());
//                        getInventory().setItem(49, inSlot);
//                    } else {
//                        getInventory().setItem(49, resulting);
//                    }
//
//                    clearInputs();
//                    updateDoing();
//                }
//            }, speedInSeconds);
        }
    }

    private void stopProcessing() {
        this.processing = false;
        if (this.task != null) this.task.cancel();
        if (this.task2 != null) this.task2.cancel();

//        animatedBlock.stopAnimation(getParentBlock());

        resetMeter();
    }

    private void updateDoing() {
//        Bukkit.getScheduler().runTaskLater(main, () -> {
//            ItemStack[][] tempGrid = new ItemStack[][] {
//                    {getInventory().getItem(11), getInventory().getItem(15)}
//            };
//
//            ItemStack resulting = main.getRecipeManager().getResultingItem(tempGrid, RecipeType.ALLOY_MIXER);
//
//            if (resulting != null && resulting.getType() != Material.AIR) {
//                ItemStack inSlot = getInventory().getItem(49);
//
//                if (inSlot == null || inSlot.getType() == Material.AIR) {
//                    startProcessing();
//                } else if (inSlot.getAmount() + resulting.getAmount() <= 64) {
//                    startProcessing();
//                }
//            } else {
//                stopProcessing();
//            }
//        }, 1L);
    }

    private void clearInputs() {
        if (getInventory().getItem(11) != null) getInventory().getItem(11).setAmount(getInventory().getItem(11).getAmount() - 1);
        if (getInventory().getItem(15) != null) getInventory().getItem(15).setAmount(getInventory().getItem(15).getAmount() - 1);

        resetMeter();
    }

    private void resetMeter() {
//        PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(progressBar.getItemStack(0)));
//
//        getInventory().getViewers().stream()
//                .map(player -> ((CraftPlayer) player).getHandle())
//                .forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot));
    }
}
