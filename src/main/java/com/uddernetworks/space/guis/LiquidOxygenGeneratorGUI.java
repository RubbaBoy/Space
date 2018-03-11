package com.uddernetworks.space.guis;

import com.uddernetworks.space.blocks.CryogenicContainerBlock;
import com.uddernetworks.space.blocks.CustomBlock;
import com.uddernetworks.space.main.Main;
import com.uddernetworks.space.utils.ItemBuilder;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.stream.IntStream;

public class LiquidOxygenGeneratorGUI extends CustomGUI {

    private boolean isProcessing = false;

    private int tempFuel = 0;
//    private int fuelNeeded = 102_400; // In ticks. 102,400 is 64 coal per fill (150 fills per Cryogenic Container), which will take 16 seconds per 64 coal   Wool is 100, 12 pieces of wool is needed, should take 6.5 seconds at 10 ticks. Coal is 1,600, Lava bucket is 20,000
    private int fuelNeeded = 1600; // In ticks. 102,400 is 64 coal per fill (150 fills per Cryogenic Container), which will take 16 seconds per 64 coal   Wool is 100, 12 pieces of wool is needed, should take 6.5 seconds at 10 ticks. Coal is 1,600, Lava bucket is 20,000
    private BukkitTask task;
    private int smokeDelay = 0; // The amount of 5 tick intervals that should go by until it spawns smoke. A value of 4 will spawn one after each second.
    private int currentSmoke = 0;

    public LiquidOxygenGeneratorGUI(Main main, String title, int size, UUID uuid) {
        super(main, title, size, uuid, GUIItems.LIQUID_OXYGEN_GENERATOR_MAIN);

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

        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 5; i2++) {
                addSlot(new OpenSlot(11 + (i * 9) + i2, slotAction));
            }
        }

        updateSlots();
    }

    private int removeFuel() {
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 5; i2++) {
                int slot = 11 + (i * 9) + i2;
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

    private void startFuelProcessing() {
        if (this.isProcessing) return;
        this.isProcessing = true;
        this.task = Bukkit.getScheduler().runTaskTimer(main, () -> {
            Block container = getParentBlock().getRelative(-1, 0, 0);
            CustomBlock customBlock = main.getBlockDataManager().getCustomBlock(container);

            if (customBlock == null || !customBlock.getName().equalsIgnoreCase("Cryogenic Container")) {
                this.tempFuel = 0;

                this.isProcessing = false;
                this.task.cancel();
                return;
            }

            if (this.fuelNeeded > this.tempFuel) {
                int fuel = removeFuel();

                if (fuel == -1) {
                    this.isProcessing = false;
                    this.task.cancel();
                    return;
                }

                this.tempFuel += fuel;
            }

            if (this.tempFuel >= this.fuelNeeded) {
                this.tempFuel = this.tempFuel - this.fuelNeeded;

                CryogenicContainerBlock cryogenicContainerBlock = (CryogenicContainerBlock) customBlock;
                cryogenicContainerBlock.addFill(container);
            }

            if (this.currentSmoke > this.smokeDelay) {
                this.currentSmoke = 0;

                Location blockLocation = getParentBlock().getLocation();

                blockLocation.add(0.3, 0.4, 0.6);

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
        }, 0L, 5L);
    }
}
