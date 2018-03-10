package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BlockDataManager {

    private Main main;
    private Map<Block, CustomBlock> customBlockCache = new HashMap<>();

    public BlockDataManager(Main main) {
        this.main = main;
    }

    public void setData(Block block, String key, Object value, Runnable callback) {
        if (key.equals("customBlock")) {
            this.customBlockCache.put(block, main.getCustomIDManager().getCustomBlockById(Integer.valueOf(value.toString())));
        }

        main.newChain()
                .async(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("INSERT OR REPLACE INTO block_data VALUES (?, ?);");
                        preparedStatement.setString(1, block.getWorld().getUID() + "," + block.getX() + "," + block.getY() + "," + block.getZ() + "," + key);
                        preparedStatement.setString(2, value.toString());
                        preparedStatement.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .sync(callback::run)
                .execute();
    }

    public void getData(Block block, String key, Consumer<String> callback) {
        main.newChain()
                .asyncFirst(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT value FROM block_data WHERE coordinate = ?;");

                        preparedStatement.setString(1, block.getWorld().getUID() + "," + block.getX() + "," + block.getY() + "," + block.getZ() + "," + key);

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

        main.newChain()
                .async(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("DELETE FROM block_data WHERE coordinate LIKE ?;");
                        preparedStatement.setString(1, block.getWorld().getUID() + "," + block.getX() + "," + block.getY() + "," + block.getZ() + ",%");
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

    public void updateCaches(Runnable callback) { // customBlock
        main.newChain()
                .asyncFirst(() -> {
                    try {
                        PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT * FROM block_data WHERE coordinate = ?;");

                        preparedStatement.setString(1, "%customBlock");

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
                            String[] data = resultSet.getString("coordinate").split(",");
                            int customBlockID = Integer.valueOf(resultSet.getString("value"));

                            World world = Bukkit.getWorld(UUID.fromString(data[0]));

                            this.customBlockCache.put(world.getBlockAt(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3])), main.getCustomIDManager().getCustomBlockById(customBlockID));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (callback != null) callback.run();
                })
                .execute();
    }

    public void addCachedValue(Block block, CustomBlock customBlock) {
        if (customBlock == null) {
            this.customBlockCache.remove(block);
        } else {
            this.customBlockCache.put(block, customBlock);
        }
    }

    public CustomBlock getCustomBlock(Block block) {
        return this.customBlockCache.get(block);
    }

}
