import java.sql.*;

public class TestSchema {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        // Create a new test database
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:test_schema.duckdb")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS doseGroups (
                        id BIGINT PRIMARY KEY,
                        doseResponseExperimentId BIGINT,
                        dose DOUBLE,
                        n BIGINT,
                        responseMean DOUBLE
                    )
                """);
                System.out.println("Created doseGroups table with 'n' column");
            }
            
            // Check the table structure
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(doseGroups)")) {
                
                System.out.println("Table structure:");
                while (rs.next()) {
                    String columnName = rs.getString(2);
                    String columnType = rs.getString(3);
                    System.out.println(columnName + " - " + columnType);
                }
            }
        }
    }
}
