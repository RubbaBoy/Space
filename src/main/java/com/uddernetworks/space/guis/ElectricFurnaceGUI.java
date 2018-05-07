package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.AnimatedBlock;
import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.blocks.ElectricFurnaceBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.FastTask;
import net.minecraft.server.v1_12_R1.RecipesFurnace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class ElectricFurnaceGUI extends CustomGUI {

    private Main main;
    private double amount = 0;
    private double update = 1;
    private double speedInSeconds = 2.5;
    //    private ProgressBar fuelProgress;
    private ProgressBar arrowProgress;
    private FastTask task;
    private FastTask task2;
    private AnimatedBlock animatedBlock;

    private boolean processing = false;
    private Slot input = null;

    private Slot firstInput;
    private Slot secondInput;

    private Slot firstOutput;
    private Slot secondOutput;

    private double supply;
    private double demand;

    public ElectricFurnaceGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.ELECTRIC_FURNACE_MAIN);

        this.main = main;

        this.arrowProgress = main.getProgressBarManager().getProgressBar("FurnaceArrowBar");

        this.animatedBlock = (AnimatedBlock) main.getCustomIDManager().getCustomBlockById(115);

        addSlot(new PopulatedSlot(20, false, arrowProgress.getItemStack(0)));

        SlotAction slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                System.out.println("ElectricFurnaceGUI.putIn " + position);
                updateDoing();
                return true;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                System.out.println("ElectricFurnaceGUI.takeOut");
                updateDoing();
                return true;
            }
        };

        addSlot(firstInput = new OpenSlot(11, slotAction));
        addSlot(secondInput = new OpenSlot(12, slotAction));

        addSlot(firstOutput = new UnsettableSlot(15, this::updateDoing));
        addSlot(secondOutput = new UnsettableSlot(16, this::updateDoing));

        updateSlots();

        this.demand = animatedBlock.getDefaultDemand();
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        super.setParentBlock(parentBlock);

        // For testing
//        this.animatedBlock.startAnimation(getParentBlock());
    }

    public double getSupply() {
        return supply;
    }

    public void setSupply(double supply) {
        this.supply = supply;

        if (!processing) {
            ElectricFurnaceBlock customBlock = (ElectricFurnaceBlock) main.getBlockDataManager().getCustomBlock(getParentBlock());
            customBlock.setTypeTo(getParentBlock(), customBlock.getDamages(getParentBlock())[1]);
        }
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    private void startProcessing() {
        this.processing = true;

        this.task = new FastTask(main).runRepeatingTask(true, () -> {
            if (!this.processing) return;

            setPacketItem(20, arrowProgress.getItemStack(amount));

            amount += update;
        }, 0L, speedInSeconds / 100);

        this.task2 = new FastTask(main).runTaskLater(false, () -> {
            try {
                if (!this.processing) return;

                stopProcessing();

                ItemStack resulting = CraftItemStack.asBukkitCopy(RecipesFurnace.getInstance().getResult(CraftItemStack.asNMSCopy(getInventory().getItem(input.getIndex()))));

                Slot output = getOutputFor(resulting);

                if (output != null) {
                    resulting.setAmount(Math.min(resulting.getAmount() + (getInventory().getItem(output.getIndex()) != null ? getInventory().getItem(output.getIndex()).getAmount() : 0), 64));

                    getInventory().setItem(output.getIndex(), resulting);

                    ItemStack inputItem = getInventory().getItem(input.getIndex());
                    inputItem.setAmount(inputItem.getAmount() - 1);
                    getInventory().setItem(input.getIndex(), inputItem);
                }

//            Slot testSlot = firstOutput;
//            ItemStack testItem = first;
//
//            for (int i = 0; i < 2; i++) {
//                if (testItem.isSimilar(resulting) || testItem.getType() == Material.AIR || testItem.getAmount() <= 0) {
//                    testItem.setAmount(testItem.getAmount() + resulting.getAmount());
//                    getInventory().setItem(testSlot.getIndex(), testItem);
//                    break;
//                }
//
//                testSlot = secondOutput;
//                testItem = second;
//            }

                updateDoing();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, speedInSeconds);


//        if (!this.processing) {
//            this.processing = true;
//            this.amount = 0;


//            animatedBlock.startAnimation(getParentBlock(), 1);

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
//        }
    }

    private void stopProcessing() {
        this.processing = false;
        if (this.task != null) this.task.cancel();
        if (this.task2 != null) this.task2.cancel();
        this.amount = 0;

//        animatedBlock.stopAnimation(getParentBlock());

        resetMeter();
    }

    private void updateDoing() {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            try {
                if (processing) {
                    if (input == null) return;

                    if (!goodInputItem(input)) {
//                    processing = false;
                        stopProcessing();
                        return;
                    }

                    Slot output = getOutputFor(CraftItemStack.asBukkitCopy(RecipesFurnace.getInstance().getResult(CraftItemStack.asNMSCopy(getInventory().getItem(input.getIndex())))));

                    if (output == null) {
                        stopProcessing();
                        return;
                    }
                } else {
                    if (goodInputItem(firstInput)) {
                        input = firstInput;

                        Slot output = getOutputFor(CraftItemStack.asBukkitCopy(RecipesFurnace.getInstance().getResult(CraftItemStack.asNMSCopy(getInventory().getItem(input.getIndex())))));
                        if (output == null) return;

                        startProcessing();
                    } else if (goodInputItem(secondInput)) {
                        input = secondInput;

                        Slot output = getOutputFor(CraftItemStack.asBukkitCopy(RecipesFurnace.getInstance().getResult(CraftItemStack.asNMSCopy(getInventory().getItem(input.getIndex())))));
                        if (output == null) return;

                        startProcessing();
                    } else {
                        input = null;
//                        startProcessing();
                    }
                }


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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1L);
    }

    private Slot getOutputFor(ItemStack resulting) {
        ItemStack first = getInventory().getItem(firstOutput.getIndex());
        ItemStack second = getInventory().getItem(secondOutput.getIndex());

        Slot testSlot = firstOutput;
        ItemStack testItem = first;

        for (int i = 0; i < 2; i++) {
            if (testItem == null || testItem.isSimilar(resulting) || testItem.getType() == Material.AIR || testItem.getAmount() <= 0) return testSlot;
            testSlot = secondOutput;
            testItem = second;
        }

        return null;
    }

    private boolean goodInputItem(Slot slot) {
        return goodInputItem(getInventory().getItem(slot.getIndex()));
    }

    private boolean goodInputItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        return itemStack.getAmount() > 0 && itemStack.getType() != Material.AIR && !RecipesFurnace.getInstance().getResult(CraftItemStack.asNMSCopy(itemStack)).isEmpty();
    }

    private void clearInputs() {
        if (getInventory().getItem(11) != null)
            getInventory().getItem(11).setAmount(getInventory().getItem(11).getAmount() - 1);
        if (getInventory().getItem(15) != null)
            getInventory().getItem(15).setAmount(getInventory().getItem(15).getAmount() - 1);

        resetMeter();
    }

    private void resetMeter() {
        setPacketItem(20, arrowProgress.getItemStack(0));
//        PacketPlayOutSetSlot packetPlayOutSetSlot = new PacketPlayOutSetSlot(getWindowID(), 46, CraftItemStack.asNMSCopy(fuelProgress.getItemStack(0)));
//
//        getInventory().getViewers().stream()
//                .map(player -> ((CraftPlayer) player).getHandle())
//                .forEach(entityPlayer -> entityPlayer.playerConnection.networkManager.sendPacket(packetPlayOutSetSlot));
    }
}
