package com.uddernetworks.space.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MoonGenerator extends ChunkGenerator {
    private SimplexOctaveGenerator generator;

    private SimplexOctaveGenerator getGenerator(World world) {
        if (generator == null) {
            Random rand = new Random(world.getSeed());

            generator = new SimplexOctaveGenerator(rand, 8);
        }
        return generator;
    }

    @Override
    public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biome) {
        ChunkGenerator.ChunkData data = this.createChunkData(world);

        getGenerator(world).setScale(1 / 128D);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                double noise = getGenerator(world).noise(x + chunkX * 16, z + chunkZ * 16, 0.5, 0.5) * 10;

                data.setBlock(x, 0, z, Material.BEDROCK);

                for (int y = 1; y < 60 + noise; y++) {
                    data.setBlock(x, y, z, new MaterialData(Material.CONCRETE_POWDER, (byte) 8));
                }
            }
        }

        return data;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Arrays.asList(new ExamplePopulator());
//        return new ArrayList<>();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        // Create random position within 100 blocks of 0,0
        int x = random.nextInt(200) - 100;
        int z = random.nextInt(200) - 100;
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x, y, z);
    }
}