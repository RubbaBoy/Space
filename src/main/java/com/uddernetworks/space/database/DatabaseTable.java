package com.uddernetworks.space.database;

public enum DatabaseTable {
    BLOCK_DATA("block_data", "CREATE TABLE block_data (\n" +
            "  world VARCHAR(36),\n" +
            "  x     INT,\n" +
            "  y     INT,\n" +
            "  z     INT,\n" +
            "  key TEXT NOT NULL,\n" +
            "  value TEXT DEFAULT '',\n" +
            "  UNIQUE (`world`, `x`, `y`, `z`, `key`)\n" +
            "    ON CONFLICT REPLACE\n" +
            ");");

    private final String tableName;
    private final String sql;

    DatabaseTable(String tableName, String sql) {
        this.tableName = tableName;
        this.sql = sql;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSQL() {
        return sql;
    }
}
