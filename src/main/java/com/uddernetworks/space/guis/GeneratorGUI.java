package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.blocks.GeneratorBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import com.uddernetworks.space.utils.SpaceMath;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class GeneratorGUI extends CustomGUI {


    private boolean isProcessing = false;

    private int tempFuel = 0;
//    private int fuelNeeded = 102_400; // In ticks. 102,400 is 64 coal per fill (150 fills per Cryogenic Container), which will take 16 seconds per 64 coal   Wool is 100, 12 pieces of wool is needed, should take 6.5 seconds at 10 ticks. Coal is 1,600, Lava bucket is 20,000
    private final int fuelNeeded = 1600; // In ticks. 102,400 is 64 coal per fill (150 fills per Cryogenic Container), which will take 16 seconds per 64 coal   Wool is 100, 12 pieces of wool is needed, should take 6.5 seconds at 10 ticks. Coal is 1,600, Lava bucket is 20,000
    private BukkitTask task;
    private final double baseSmokeDelay = 4;
    private double smokeDelay = 4; // The amount of 5 tick intervals that should go by until it spawns smoke. A value of 4 will spawn one after each second.
    private int currentSmoke = 0;

    private ProgressBar wattageProgress;
    private GeneratorBlock generatorBlock;

    private int adding2 = 1;
    private int index2 = 0;

    public GeneratorGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.GENERATOR_MAIN);

        this.generatorBlock = (GeneratorBlock) main.getCustomIDManager().getCustomBlockById(116);

        this.wattageProgress = main.getProgressBarManager().getProgressBar("GeneratorLoad");

        addSlot(new PopulatedSlot(47, false, wattageProgress.getItemStack(0)));

        SlotAction slotAction = new SlotAction() {
            @Override
            public boolean putIn(int position, ItemStack item) {
                startFuelProcessing();
                return true;
            }

            @Override
            public boolean takeOut(int position, ItemStack item) {
                startFuelProcessing();
                return true;
            }
        };

        for (int i = 0; i < 6; i++) {
            for (int i2 = 0; i2 < 6; i2++) {
                addSlot(new OpenSlot(3 + (i * 9) + i2, slotAction));
//                addSlot(new PopulatedSlot(3 + (i * 9) + i2, false, ItemBuilder.itemFrom(Material.GOLD_BLOCK)));
            }
        }

        updateSlots();

        setPacketItem(47, wattageProgress.getItemStack(0));
    }

    @Override
    public void setParentBlock(Block parentBlock) {
        super.setParentBlock(parentBlock);

        main.getBlockDataManager().getData(getParentBlock(), "inventoryContents", data -> {
            if (data == null) return;
            getInventory().setContents(InventoryUtils.deserializeInventory(data));
        });
    }

    public void updateOutputMeter(double power, double maxPower) {
        setPacketItem(47, wattageProgress.getItemStack(power / maxPower * 100D));

        double mapped = SpaceMath.map(power / maxPower * 100D, 0, 100, 0, 3);

        smokeDelay = baseSmokeDelay / mapped;
        smokeDelay = Math.max(smokeDelay, 1);
    }

    private int removeFuel() {
        for (int i = 0; i < 6; i++) {
            for (int i2 = 0; i2 < 6; i2++) {
                int slot = 3 + (i * 9) + i2;
                ItemStack itemStack = getInventory().getItem(slot);

                if (itemStack == null || itemStack.getType() == Material.AIR || !TileEntityFurnace.isFuel(CraftItemStack.asNMSCopy(itemStack)))
                    continue;

                int time = TileEntityFurnace.fuelTime(CraftItemStack.asNMSCopy(itemStack));

                itemStack.setAmount(itemStack.getAmount() - 1);

                getInventory().setItem(slot, itemStack);

                return time;
            }
        }

        return -1;
    }

    private boolean prevPowered = false;

    private void startFuelProcessing() {
        System.out.println("GeneratorGUI.startFuelProcessing");
        if (this.isProcessing) return;
        this.isProcessing = true;
        this.task = Bukkit.getScheduler().runTaskTimer(main, () -> {
            try {
//            Block container = getParentBlock().getRelative(-1, 0, 0);
//            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(container);

//            if (customBlock == null || !customBlock.getName().equalsIgnoreCase("Generator")) {
//                this.tempFuel = 0;
//
//                this.isProcessing = false;
//                this.task.cancel();
//                return;
//            }

                if (this.fuelNeeded > this.tempFuel) {
                    int fuel = removeFuel();

                    System.out.println("fuel = " + fuel);

                    if (fuel == -1) {
                        System.out.println("111");

                        this.generatorBlock.setPowered(getParentBlock(), false);
                        this.generatorBlock.updateCircuit(getParentBlock());

                        this.prevPowered = false;
                        this.isProcessing = false;
                        this.task.cancel();
                        return;
                    }

                    if (!this.prevPowered) {
                        this.prevPowered = true;
                        this.generatorBlock.setPowered(getParentBlock(), true);
                        this.generatorBlock.updateCircuit(getParentBlock());
                    }

                    this.tempFuel += fuel;
                }

                if (this.tempFuel >= this.fuelNeeded) {
                    System.out.println("222");
                    this.tempFuel = this.tempFuel - this.fuelNeeded;
                }

                if (this.currentSmoke > this.smokeDelay) {
                    this.currentSmoke = 0;

                    Location blockLocation = getParentBlock().getLocation();

                    blockLocation.add(0.5, 0.4, 0.5);

                    PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(
                            EnumParticle.SMOKE_LARGE, // Particle
                            false, // 256 render distance over 65536
                            Double.valueOf(blockLocation.getX()).floatValue(), // X
                            Double.valueOf(blockLocation.getY()).floatValue(), // Y
                            Double.valueOf(blockLocation.getZ()).floatValue(), // Z
                            0, // Offset X
                            0, // Offset Y
                            0, // Offset Z
                            0, // Data
                            1); // Count

                    Bukkit.getOnlinePlayers().forEach(forPlayer -> ((CraftPlayer) forPlayer).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles));
                }

                this.currentSmoke++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1L, 5L);
    }
}
