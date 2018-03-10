package com.uddernetworks.space.blocks;

import co.aikar.taskchain.BukkitTaskChainFactory;
import com.uddernetworks.space.main.Main;
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

}
