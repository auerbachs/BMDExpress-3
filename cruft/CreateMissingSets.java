import java.sql.*;

public class CreateMissingSets {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:dehp_webapp_schema_fixed_final.duckdb")) {
            System.out.println("Creating missing category analysis results sets...\n");
            
            // Check current state
            System.out.println("Current state:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets")) {
                rs.next();
                System.out.println("  Category Analysis Results Sets: " + rs.getInt(1));
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bmdResults")) {
                rs.next();
                System.out.println("  BMD Results: " + rs.getInt(1));
            }
            
            // Get all BMD results
            System.out.println("\nBMD Results:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM bmdResults ORDER BY id")) {
                while (rs.next()) {
                    System.out.println("  ID: " + rs.getLong(1) + ", Name: " + rs.getString(2));
                }
            }
            
            // Check which BMD results already have category analysis results sets
            System.out.println("\nBMD Results with existing category analysis sets:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT bmdResultId FROM categoryAnalysisResultsSets ORDER BY bmdResultId")) {
                while (rs.next()) {
                    System.out.println("  BMD Result ID: " + rs.getLong(1));
                }
            }
            
            // Create missing sets for BMD results that don't have them
            System.out.println("\nCreating missing sets...");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT br.id, br.name FROM bmdResults br LEFT JOIN categoryAnalysisResultsSets cars ON br.id = cars.bmdResultId WHERE cars.bmdResultId IS NULL")) {
                while (rs.next()) {
                    long bmdResultId = rs.getLong(1);
                    String bmdResultName = rs.getString(2);
                    
                    // Create a category analysis results set for this BMD result
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO categoryAnalysisResultsSets (id, name, sex, organ, species, dataType, platform, bmdResultId, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        
                        // Get next ID
                        long nextId;
                        try (Statement idStmt = conn.createStatement();
                             ResultSet idRs = idStmt.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM categoryAnalysisResultsSets")) {
                            idRs.next();
                            nextId = idRs.getLong(1);
                        }
                        
                        insertStmt.setLong(1, nextId);
                        insertStmt.setString(2, bmdResultName + "_CategoryAnalysis");
                        insertStmt.setString(3, "Unknown");
                        insertStmt.setString(4, "Unknown");
                        insertStmt.setString(5, "Unknown");
                        insertStmt.setString(6, "Unknown");
                        insertStmt.setString(7, "Unknown");
                        insertStmt.setLong(8, bmdResultId);
                        insertStmt.setLong(9, 1L); // Default dataset ID
                        
                        insertStmt.executeUpdate();
                        System.out.println("  Created set for BMD Result: " + bmdResultName);
                    }
                }
            }
            
            // Check final state
            System.out.println("\nFinal state:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets")) {
                rs.next();
                System.out.println("  Category Analysis Results Sets: " + rs.getInt(1));
            }
            
            // Show all sets
            System.out.println("\nAll category analysis results sets:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, bmdResultId FROM categoryAnalysisResultsSets ORDER BY id")) {
                while (rs.next()) {
                    System.out.println("  ID: " + rs.getLong(1) + ", Name: " + rs.getString(2) + ", BMDResultId: " + rs.getLong(3));
                }
            }
        }
    }
}
