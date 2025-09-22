import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java CheckDatabase <database.duckdb>");
            System.exit(1);
        }
        
        String dbPath = args[0];
        
        try {
            Class.forName("org.duckdb.DuckDBDriver");
            Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
            
            System.out.println("Database: " + dbPath);
            System.out.println("=====================================");
            
            // Get all tables
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            
            while (rs.next()) {
                String tableName = rs.getString(1);
                
                // Count rows in each table
                try {
                    Statement countStmt = conn.createStatement();
                    ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                    countRs.next();
                    int rowCount = countRs.getInt(1);
                    System.out.println(tableName + ": " + rowCount + " rows");
                    countRs.close();
                    countStmt.close();
                } catch (SQLException e) {
                    System.out.println(tableName + ": Error counting rows - " + e.getMessage());
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
