import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Simple DuckDB WASM Converter
 * 
 * A standalone converter that doesn't depend on BMDExpress modules.
 * Converts JDBC-created DuckDB databases to WASM-compatible format.
 */
public class SimpleDuckDBConverter {
    
    private static final String DUCKDB_JDBC_DRIVER = "org.duckdb.DuckDBDriver";
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java SimpleDuckDBConverter <input.duckdb> <output.duckdb>");
            System.err.println("  input.duckdb  - JDBC-created DuckDB database");
            System.err.println("  output.duckdb - WASM-compatible DuckDB database");
            System.exit(1);
        }
        
        String inputDb = args[0];
        String outputDb = args[1];
        
        try {
            System.out.println("Simple DuckDB WASM Converter");
            System.out.println("============================");
            System.out.println("Input (JDBC):  " + inputDb);
            System.out.println("Output (WASM): " + outputDb);
            System.out.println();
            
            SimpleDuckDBConverter converter = new SimpleDuckDBConverter();
            converter.convertToWasmCompatible(inputDb, outputDb);
            
            System.out.println("✅ Conversion completed successfully!");
            System.out.println("The output database is now compatible with DuckDB WASM.");
            
        } catch (Exception e) {
            System.err.println("❌ Conversion failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Convert a JDBC-created DuckDB database to WASM-compatible format
     */
    public void convertToWasmCompatible(String inputDbPath, String outputDbPath) throws Exception {
        // Load DuckDB JDBC driver
        Class.forName(DUCKDB_JDBC_DRIVER);
        
        // Step 1: Connect to input database and export schema + data
        System.out.println("Step 1: Analyzing input database...");
        DatabaseInfo dbInfo = analyzeDatabase(inputDbPath);
        
        // Step 2: Create new WASM-compatible database
        System.out.println("Step 2: Creating WASM-compatible database...");
        createWasmCompatibleDatabase(outputDbPath, dbInfo);
        
        System.out.println("✅ Conversion completed successfully!");
    }
    
    private DatabaseInfo analyzeDatabase(String dbPath) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            DatabaseInfo info = new DatabaseInfo();
            
            // Get all tables
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                
                while (rs.next()) {
                    String tableName = rs.getString("name");
                    TableInfo tableInfo = analyzeTable(conn, tableName);
                    info.tables.put(tableName, tableInfo);
                    System.out.println("  - Table: " + tableName + " (" + tableInfo.rowCount + " rows)");
                }
            }
            
            return info;
        }
    }
    
    private TableInfo analyzeTable(Connection conn, String tableName) throws Exception {
        TableInfo tableInfo = new TableInfo();
        tableInfo.name = tableName;
        
        // Get table schema
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + tableName)) {
            
            while (rs.next()) {
                ColumnInfo col = new ColumnInfo();
                col.name = rs.getString("column_name");
                col.type = rs.getString("column_type");
                col.nullable = "YES".equals(rs.getString("null"));
                col.key = "PRI".equals(rs.getString("key"));
                tableInfo.columns.add(col);
            }
        }
        
        // Get row count
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tableName)) {
            
            if (rs.next()) {
                tableInfo.rowCount = rs.getLong("count");
            }
        }
        
        // Export all data
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableInfo.data.add(row);
            }
        }
        
        return tableInfo;
    }
    
    private void createWasmCompatibleDatabase(String dbPath, DatabaseInfo dbInfo) throws Exception {
        // Create new database file
        Files.deleteIfExists(Paths.get(dbPath));
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            
            // Create tables with proper schema
            for (TableInfo tableInfo : dbInfo.tables.values()) {
                createTable(conn, tableInfo);
            }
            
            // Insert data
            for (TableInfo tableInfo : dbInfo.tables.values()) {
                if (!tableInfo.data.isEmpty()) {
                    insertData(conn, tableInfo);
                }
            }
            
            // Create meta table for WASM compatibility
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS meta (key VARCHAR PRIMARY KEY, value BIGINT)");
                stmt.execute("INSERT OR IGNORE INTO meta (key, value) VALUES ('schema_version', 1)");
            }
        }
    }
    
    private void createTable(Connection conn, TableInfo tableInfo) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableInfo.name).append(" (");
        
        for (int i = 0; i < tableInfo.columns.size(); i++) {
            if (i > 0) sql.append(", ");
            
            ColumnInfo col = tableInfo.columns.get(i);
            sql.append(col.name).append(" ").append(col.type);
            
            if (!col.nullable) {
                sql.append(" NOT NULL");
            }
            
            if (col.key) {
                sql.append(" PRIMARY KEY");
            }
        }
        
        sql.append(")");
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
        }
    }
    
    private void insertData(Connection conn, TableInfo tableInfo) throws Exception {
        if (tableInfo.data.isEmpty()) return;
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableInfo.name).append(" (");
        
        // Column names
        for (int i = 0; i < tableInfo.columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(tableInfo.columns.get(i).name);
        }
        
        sql.append(") VALUES (");
        
        // Placeholders
        for (int i = 0; i < tableInfo.columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        
        sql.append(")");
        
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (Object[] row : tableInfo.data) {
                for (int i = 0; i < row.length; i++) {
                    stmt.setObject(i + 1, row[i]);
                }
                stmt.addBatch();
                
                // Execute in batches to avoid memory issues
                if (tableInfo.data.indexOf(row) % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        
        System.out.println("  - Inserted " + tableInfo.data.size() + " rows into " + tableInfo.name);
    }
    
    // Data structures
    private static class DatabaseInfo {
        Map<String, TableInfo> tables = new LinkedHashMap<>();
    }
    
    private static class TableInfo {
        String name;
        List<ColumnInfo> columns = new ArrayList<>();
        List<Object[]> data = new ArrayList<>();
        long rowCount;
    }
    
    private static class ColumnInfo {
        String name;
        String type;
        boolean nullable;
        boolean key;
    }
}
