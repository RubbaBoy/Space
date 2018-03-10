package com.uddernetworks.space.database;

public enum DatabaseTable {
    BLOCK_DATA("block_data", "CREATE TABLE IF NOT EXISTS block_data (\n" +
            "  coordinate VARCHAR(512) NOT NULL UNIQUE,\n" +
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
