import java.sql.*;

public class CheckTableStructure {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java CheckTableStructure <table_name>");
            System.exit(1);
        }
        
        String tableName = args[0];
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:dehp_webapp_schema_final.duckdb")) {
            System.out.println("Table structure for: " + tableName);
            System.out.println("=====================================");
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                
                while (rs.next()) {
                    String columnName = rs.getString(2);
                    String columnType = rs.getString(3);
                    System.out.println(columnName + " - " + columnType);
                }
            }
        }
    }
}
