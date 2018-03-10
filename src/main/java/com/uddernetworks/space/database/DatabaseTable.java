package com.uddernetworks.space.database;

public enum DatabaseTable {
    BLOCK_DATA("block_data", "CREATE TABLE IF NOT EXISTS block_data (\n" +
            "  x INT NOT NULL,\n" +
            "  y INT NOT NULL,\n" +
            "  z INT NOT NULL,\n" +
            "  key VARCHAR(255) NOT NULL,\n" +
            "  value TEXT DEFAULT ''\n" +
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
