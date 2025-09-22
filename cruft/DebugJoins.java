import java.sql.*;

public class DebugJoins {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:dehp_webapp_schema_fixed_final.duckdb")) {
            System.out.println("Debugging JOIN issues...\n");
            
            // Check categoryAnalysisResultsSets data
            System.out.println("1. categoryAnalysisResultsSets data:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, bmdResultId FROM categoryAnalysisResultsSets")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getLong(1) + ", bmdResultId: " + rs.getLong(2));
                }
            }
            
            // Check bmdResults data
            System.out.println("\n2. bmdResults data:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM bmdResults")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getLong(1) + ", Name: " + rs.getString(2));
                }
            }
            
            // Check if there are any matching bmdResultIds
            System.out.println("\n3. Checking for matching bmdResultIds:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT cars.bmdResultId FROM categoryAnalysisResultsSets cars WHERE cars.bmdResultId IN (SELECT id FROM bmdResults)")) {
                while (rs.next()) {
                    System.out.println("   Matching bmdResultId: " + rs.getLong(1));
                }
            }
            
            // Check what bmdResultIds exist in categoryAnalysisResultsSets
            System.out.println("\n4. bmdResultIds in categoryAnalysisResultsSets:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT bmdResultId FROM categoryAnalysisResultsSets")) {
                while (rs.next()) {
                    System.out.println("   bmdResultId: " + rs.getLong(1));
                }
            }
            
            // Check what IDs exist in bmdResults
            System.out.println("\n5. IDs in bmdResults:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM bmdResults")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getLong(1));
                }
            }
        }
    }
}
