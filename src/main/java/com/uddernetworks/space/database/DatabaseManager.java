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

        } catch (SQLException | IOException e) {
            main.getLogger().log(Level.SEVERE, "Error connecting to SQL database: " + e.getMessage());
            e.printStackTrace();
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

    public Connection getConnection() {
        return this.connection;
    }
}
