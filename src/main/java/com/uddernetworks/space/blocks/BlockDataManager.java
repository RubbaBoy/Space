package com.uddernetworks.space.blocks;

import com.uddernetworks.space.main.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class BlockDataManager {

    private Main main;

    public BlockDataManager(Main main) {
        this.main = main;
    }

    public void setData(Block block, String key, Object value, Runnable callback) {
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
        main.newChain()
                .async(() -> { // DELETE FROM block_data WHERE coordinate LIKE 'ed60d88d-db1e-480a-b590-d88687caa7a6,62,90,324,%';
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

}
