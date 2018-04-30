package com.uddernetworks.space.blocks;

import com.google.common.collect.ImmutableMap;
import com.uddernetworks.space.main.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class BlockDataManager {

    private static final boolean POSSIBLY_BAD_IMMEDIATE_PLACE_CALLBACK = true;

    private Main main;
    private Map<Block, CustomBlock> customBlockCache = new HashMap<>();
    private Map<String, Map<Block, String>> generalCache = new HashMap<>();

    public BlockDataManager(Main main) {
        this.main = main;
    }

    public void setData(Block block, String key, Object value, Runnable callback) {
        if (key.equals("customBlock")) {
            this.customBlockCache.put(block, main.getCustomIDManager().getCustomBlockById(Integer.valueOf(value.toString())));
        } else {
            setCache(block, key, value.toString());
        }

        if (POSSIBLY_BAD_IMMEDIATE_PLACE_CALLBACK) callback.run();

        main.newChain()
                .async(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("INSERT OR REPLACE INTO block_data VALUES (?, ?, ?, ?, ?, ?);");

                        preparedStatement.setString(1, block.getWorld().getUID().toString());
                        preparedStatement.setInt(2, block.getX());
                        preparedStatement.setInt(3, block.getY());
                        preparedStatement.setInt(4, block.getZ());
                        preparedStatement.setString(5, key);
                        preparedStatement.setString(6, value.toString());

                        preparedStatement.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .sync(() -> {
                    if (!POSSIBLY_BAD_IMMEDIATE_PLACE_CALLBACK) callback.run();
                })
                .execute();
    }

    public void getData(Block block, String key, Consumer<String> callback) {
        String cachedReturn = getCache(block, key);

        if (cachedReturn != null) {
            callback.accept(cachedReturn);
            return;
        }

        main.newChain()
                .asyncFirst(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT value FROM block_data WHERE world = ? AND x = ? AND y = ? AND z = ? AND key = ?;");

//                        System.out.println("block.getWorld().getUID().toString() = " + block.getWorld().getUID().toString());
//                        System.out.println("key = " + key);

                        preparedStatement.setString(1, block.getWorld().getUID().toString());
                        preparedStatement.setInt(2, block.getX());
                        preparedStatement.setInt(3, block.getY());
                        preparedStatement.setInt(4, block.getZ());
                        preparedStatement.setString(5, key);

                        ResultSet resultSet = preparedStatement.executeQuery();

                        return resultSet.isClosed() ? null : resultSet.getString("value");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .syncLast(callback::accept)
                .execute();
    }

    public void deleteData(Block block, Runnable callback) {
        this.customBlockCache.remove(block);
        removeCache(block);

        main.newChain()
                .async(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("DELETE FROM block_data WHERE world = ? AND x = ? AND y = ? AND z = ?;");

                        preparedStatement.setString(1, block.getWorld().getUID().toString());
                        preparedStatement.setInt(2, block.getX());
                        preparedStatement.setInt(3, block.getY());
                        preparedStatement.setInt(4, block.getZ());

                        preparedStatement.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .sync(callback::run)
                .execute();
    }

    public void increment(Block block, String key, int incrementBy, Consumer<Integer> newValue) {
        getData(block, key, data -> {
            if (StringUtils.isNumeric(data)) {
                if (incrementBy != 0) {
                    int value = Integer.valueOf(data) + incrementBy;
                    setData(block, key, value, () -> newValue.accept(value));
                }
            } else {
                setData(block, key, incrementBy, () -> newValue.accept(incrementBy));
            }
        });
    }

    public void updateCaches(Runnable callback) {
        main.newChain()
                .asyncFirst(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT * FROM block_data WHERE key = ?;");

                        preparedStatement.setString(1, "customBlock");

                        return preparedStatement.executeQuery();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .syncLast(resultSet -> {
                    this.customBlockCache.clear();

                    try {
                        if (resultSet == null || resultSet.isClosed()) {
                            if (callback != null) callback.run();
                            return;
                        }

                        while (resultSet.next()) {
                            World world = Bukkit.getWorld(UUID.fromString(resultSet.getString("world")));
                            int x = resultSet.getInt("x");
                            int y = resultSet.getInt("y");
                            int z = resultSet.getInt("z");
                            int customBlockID = Integer.valueOf(resultSet.getString("value"));

                            this.customBlockCache.put(world.getBlockAt(x, y, z), main.getCustomIDManager().getCustomBlockById(customBlockID));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (callback != null) callback.run();
                })
                .execute();
    }

    public void getBlocksInChunk(Chunk chunk, Consumer<Map<Block, CustomBlock>> callback) {
        main.newChain()
                .asyncFirst(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT * FROM block_data WHERE key == 'customBlock' AND x >> 4 == ? AND z >> 4 == ?;");

                        preparedStatement.setInt(1, chunk.getX());
                        preparedStatement.setInt(2, chunk.getZ());

//                        System.out.println("chunk.getX() = " + chunk.getX());
//                        System.out.println("chunk.getZ() = " + chunk.getZ());

                        return preparedStatement.executeQuery();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .syncLast(resultSet -> {
                    Map<Block, CustomBlock> blocks = new HashMap<>();

                    try {
                        if (resultSet == null || resultSet.isClosed()) {
                            if (callback != null) callback.accept(blocks);
                            return;
                        }

                        while (resultSet.next()) {
                            World world = Bukkit.getWorld(UUID.fromString(resultSet.getString("world")));
                            int x = resultSet.getInt("x");
                            int y = resultSet.getInt("y");
                            int z = resultSet.getInt("z");
                            int customBlockID = Integer.valueOf(resultSet.getString("value"));

//                            System.out.println("x = " + x);
//                            System.out.println("customBlockID = " + customBlockID);

                            blocks.put(world.getBlockAt(x, y, z), main.getCustomIDManager().getCustomBlockById(customBlockID));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (callback != null) {
                        System.out.println("blocks = " + blocks);
                        callback.accept(blocks);
                    }
                })
                .execute();
    }

    public CustomBlock getCustomBlock(Block block) {
        return this.customBlockCache.get(block);
    }

    public Map<Block, CustomBlock> getCustomBlockCache() {
        return this.customBlockCache;
    }

    private void setCache(Block block, String key, String value) {
        if (this.generalCache.containsKey(key)) {
            this.generalCache.get(key).put(block, value);
        } else {
            this.generalCache.put(key, new HashMap<>(ImmutableMap.of(block, value)));
        }
    }

    private void removeCache(Block block) {
        this.generalCache.values().forEach(values -> values.remove(block));
    }

    private String getCache(Block block, String key) {
        if (!this.generalCache.containsKey(key)) return null;
        return this.generalCache.get(key).get(block);
    }

}
