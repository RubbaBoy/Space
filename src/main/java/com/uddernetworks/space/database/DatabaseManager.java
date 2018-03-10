package com.uddernetworks.space.database;

import com.uddernetworks.space.main.Main;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.logging.Level;

public class DatabaseManager {

    private Main main;
    private Connection connection;

    public DatabaseManager(Main main) {
        this.main = main;
    }

    public void connect(File file) {
        try {
            System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            file.getParentFile().mkdirs();
            file.createNewFile();
            // db parameters
            String url = "jdbc:sqlite:" + file.getAbsolutePath();

            System.out.println("Connecting to: " + url);

            // create a connection to the database
            this.connection = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            main.getLogger().log(Level.SEVERE, "Error connecting to SQL database: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void initialize() {
        try (Statement stmt = this.connection.createStatement()) {
            for (DatabaseTable databaseTable : DatabaseTable.values()) {
                stmt.execute(databaseTable.getSQL());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(DatabaseTable table, Object... values) {
        prepareUpdate(table, "INSERT INTO {0} VALUES ({1});", values);
    }



    public Connection getConnection() {
        return this.connection;
    }


    public ResultSet prepareQuery(DatabaseTable databaseTable, String sql, Object... values) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(MessageFormat.format(sql, databaseTable.getTableName(), getRepeatingQuestionMarks(values.length)))) {

            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }

            return preparedStatement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void prepareUpdate(DatabaseTable databaseTable, String sql, Object... values) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(MessageFormat.format(sql, databaseTable.getTableName(), getRepeatingQuestionMarks(values.length)))) {

            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }

            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getRepeatingQuestionMarks(int amount) {
        return StringUtils.repeat(", ?", amount).substring(2);
    }

}
