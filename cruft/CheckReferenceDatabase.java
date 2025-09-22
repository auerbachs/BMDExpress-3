import java.sql.*;

public class CheckReferenceDatabase {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:test_comprehensive_v2_fixed.duckdb")) {
            System.out.println("Checking reference database structure...\n");
            
            // Check what tables exist
            System.out.println("1. Available tables:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            }
            
            // Check category analysis results sets if the table exists
            System.out.println("\n2. Checking for category analysis data:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE '%category%'")) {
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    System.out.println("   Found table: " + tableName);
                    
                    // Count rows in this table
                    try (Statement countStmt = conn.createStatement();
                         ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        countRs.next();
                        System.out.println("     Rows: " + countRs.getInt(1));
                    }
                }
            }
            
            // Check if there are any tables that might contain the expected 40-50 sets
            System.out.println("\n3. Looking for tables with many rows:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    try (Statement countStmt = conn.createStatement();
                         ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        countRs.next();
                        int rowCount = countRs.getInt(1);
                        if (rowCount > 20) {
                            System.out.println("   " + tableName + ": " + rowCount + " rows");
                        }
                    }
                }
            }
        }
    }
}
