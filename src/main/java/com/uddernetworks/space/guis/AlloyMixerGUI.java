package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.AnimatedBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.recipies.RecipeType;
import com.uddernetworks.space.utils.FastTask;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AlloyMixerGUI extends CustomGUI {

    private Main main;
    private double amount = 0;
    private double update = 1;
    private double speedInSeconds = 5;
    private boolean processing = false;
    private ProgressBar progressBar;
    private FastTask task;
    private FastTask task2;
//    private int windowID;
    private AnimatedBlock animatedBlock;

    public AlloyMixerGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.ALLOY_MIXER_MAIN);

        this.main = main;

        this.progressBar = main.getProgressBarManager().getProgressBar("AlloyMixerBar");
        this.animatedBlock = (AnimatedBlock) main.getCustomIDManager().getCustomBlockById(111);

        addSlot(new PopulatedSlot(46, false, progressBar.getItemStack(0)));

        addSlot(new UnsettableSlot(49, this::updateDoing));

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

        addSlot(new OpenSlot(11, slotAction));
        addSlot(new OpenSlot(15, slotAction));

        updateSlots();
    }

//    private int getWindowID() {
//        this.windowID = getInventory().getViewers().size() > 0 ? ((CraftPlayer) getInventory().getViewers().get(0)).getHandle().activeContainer.windowId : this.windowID;
//        return this.windowID;
//    }

    private void startProcessing() {
        if (!this.processing) {
            this.processing = true;
            this.amount = 0;

//            EntityPlayer ep = ((CraftPlayer) getInventory().getViewers().get(0)).getHandle();
//            this.windowID = ep.activeContainer.windowId;

            animatedBlock.startAnimation(getParentBlock());

            this.task = new FastTask(main).runRepeatingTask(true, () -> {
                if (!this.processing) return;

                PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(progressBar.getItemStack(amount)));

                amount += update;

                getInventory().getViewers().stream()
                        .map(player -> ((CraftPlayer) player).getHandle())
                        .forEach(entityPlayer -> {
                            System.out.println("Updating for: " + entityPlayer.displayName);
                            entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot);
                        });
            }, 0L, speedInSeconds / 100);

            this.task2 = new FastTask(main).runTaskLater(false, () -> {
                if (this.processing) {
                    this.processing = false;

                    stopProcessing();

                    ItemStack[][] tempGrid = new ItemStack[][] {
                            {getInventory().getItem(11), getInventory().getItem(15)}
                    };

                    ItemStack resulting = main.getRecipeManager().getResultingItem(tempGrid, RecipeType.ALLOY_MIXER);

                    ItemStack inSlot = getInventory().getItem(49);
                    if (inSlot != null && inSlot.isSimilar(resulting)) {
                        inSlot.setAmount(inSlot.getAmount() + resulting.getAmount());
                        getInventory().setItem(49, inSlot);
                    } else {
                        getInventory().setItem(49, resulting);
                    }

                    clearInputs();
                    updateDoing();
                }
            }, speedInSeconds);
        }
    }

    private void stopProcessing() {
        this.processing = false;
        if (this.task != null) this.task.cancel();
        if (this.task2 != null) this.task2.cancel();

        animatedBlock.stopAnimation(getParentBlock());

        resetMeter();
    }

    private void updateDoing() {
//        System.out.println("AlloyMixerGUI.updateDoing");
        Bukkit.getScheduler().runTaskLater(main, () -> {
//            System.out.println("15 = " + getInventory().getItem(15));
            ItemStack[][] tempGrid = new ItemStack[][] {
                    {getInventory().getItem(11), getInventory().getItem(15)}
            };

            ItemStack resulting = main.getRecipeManager().getResultingItem(tempGrid, RecipeType.ALLOY_MIXER);

//            System.out.println("111111111 resulting = " + resulting);

//            if (resulting != null && resulting.getType() == Material.AIR) {
            if (resulting != null && resulting.getType() != Material.AIR) {
//                System.out.println("========== 111");
                ItemStack inSlot = getInventory().getItem(49);

                if (inSlot == null || inSlot.getType() == Material.AIR) {
                    System.out.println("========== 222");
                    startProcessing();
                } else if (inSlot.getAmount() + resulting.getAmount() <= 64) {
                    System.out.println("========== 333");
                    startProcessing();
                } else {
                    System.out.println("NOPPPPPPPPPPPPPPPPPPPEEEEEEEEEEEEEEEEEEEEEEEE238907349805734958745jdofhp98fu73809rjodsiv......:: " + (inSlot.getAmount() + resulting.getAmount()));
                }
            } else {
//                System.out.println("========== 444");
                stopProcessing();
            }
        }, 1L);
    }

    private void clearInputs() {
        if (getInventory().getItem(11) != null) getInventory().getItem(11).setAmount(getInventory().getItem(11).getAmount() - 1);
        if (getInventory().getItem(15) != null) getInventory().getItem(15).setAmount(getInventory().getItem(15).getAmount() - 1);

        resetMeter();
    }

    private void resetMeter() {
        PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(progressBar.getItemStack(0)));

        getInventory().getViewers().stream()
                .map(player -> ((CraftPlayer) player).getHandle())
                .forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot));
    }
}
