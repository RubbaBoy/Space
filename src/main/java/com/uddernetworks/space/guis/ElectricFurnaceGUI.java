package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.AnimatedBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ElectricFurnaceGUI extends CustomGUI {

    private Main main;
    private double amount = 0;
    private double update = 1;
    private double speedInSeconds = 5;
    private boolean processing = false;
//    private ProgressBar fuelProgress;
    private ProgressBar arrowProgress;
    private FastTask task;
    private FastTask task2;
    private AnimatedBlock animatedBlock;

    private int adding2 = 1;
    private int index2 = 0;

    public ElectricFurnaceGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.ELECTRIC_FURNACE_MAIN);

        this.main = main;

        this.arrowProgress = main.getProgressBarManager().getProgressBar("FurnaceArrowBar");

        this.animatedBlock = (AnimatedBlock) main.getCustomIDManager().getCustomBlockById(115);

        addSlot(new PopulatedSlot(20, false, arrowProgress.getItemStack(0)));

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



//        Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
//
////            System.out.println("index2 = " + index2);
//
////            Bukkit.getPlayer("RubbaBoy").sendMessage("Power: " + main.getBlockDataManager().getCustomBlock(getParentBlock()).getSupply(getParentBlock()));
//
//            PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 20, CraftItemStack.asNMSCopy(arrowProgress.getItemStack(index2 / 23D * 100D)));
//
//            new ArrayList<>(getInventory().getViewers()).stream()
//                    .map(player -> ((CraftPlayer) player).getHandle())
//                    .forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot));
//
//            index2 += adding2;
//
//            if ((adding2 > 0 && index2 >= 23) || (adding2 < 0 && index2 < 0)) {
//                adding2 *= -1;
//
//                index2 += adding2;
//            }
//
//        }, 0L, 2L);
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        super.setParentBlock(parentBlock);

        // For testing
        this.animatedBlock.startAnimation(getParentBlock());
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
//                PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(fuelProgress.getItemStack(amount)));
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
//        PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(fuelProgress.getItemStack(0)));
//
//        getInventory().getViewers().stream()
//                .map(player -> ((CraftPlayer) player).getHandle())
//                .forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot));
    }
}
