package com.uddernetworks.space.blocks;

import com.google.gson.internal.Primitives;
import com.uddernetworks.space.main.Main;
import org.bukkit.block.Block;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class BlockDataManager {

    private Main main;

    public BlockDataManager(Main main) {
        this.main = main;
    }

    public void setData(Block block, String key, Object object) {
        try {
            PreparedStatement preparedStatement;

            if (getData(block, key, Object.class) == null) {
                System.out.println("Inserting");
                preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("INSERT INTO block_data VALUES (?, ?, ?, ?, ?);");
                preparedStatement.setInt(1, block.getX());
                preparedStatement.setInt(2, block.getY());
                preparedStatement.setInt(3, block.getZ());
                preparedStatement.setString(4, key);
                preparedStatement.setObject(5, object);
            } else {
                System.out.println("Updating");
                preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("UPDATE block_data SET value = ? WHERE x = ? AND y = ? AND z = ? AND key = ?;");
                preparedStatement.setObject(1, object);
                preparedStatement.setInt(2, block.getX());
                preparedStatement.setInt(3, block.getY());
                preparedStatement.setInt(4, block.getZ());
                preparedStatement.setString(5, key);
            }

            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> T getData(Block block, String key, Class<T> type) {
        try {
            PreparedStatement preparedStatement = this.main.getDatabaseManager().getConnection().prepareStatement("SELECT value FROM block_data WHERE x = ? AND y = ? AND z = ? AND key = ?;");

            preparedStatement.setInt(1, block.getX());
            preparedStatement.setInt(2, block.getY());
            preparedStatement.setInt(3, block.getZ());
            preparedStatement.setString(4, key);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.isClosed() ? null : Primitives.wrap(type).cast(resultSet.getObject("value"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
