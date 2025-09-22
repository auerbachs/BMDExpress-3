import java.sql.*;

public class CheckDataCompleteness {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:p3mp_webapp_schema.duckdb")) {
            System.out.println("Checking data completeness...\n");
            
            // Check what we have in the database
            System.out.println("1. Current data in database:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 'categoryAnalysisResultsSets' as table_name, COUNT(*) as count FROM categoryAnalysisResultsSets UNION ALL SELECT 'categoryAnalysisResults', COUNT(*) FROM categoryAnalysisResults UNION ALL SELECT 'categoryIdentifiers', COUNT(*) FROM categoryIdentifiers UNION ALL SELECT 'bmdResults', COUNT(*) FROM bmdResults UNION ALL SELECT 'doseResponseExperiments', COUNT(*) FROM doseResponseExperiments")) {
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1) + ": " + rs.getInt(2));
                }
            }
            
            // Check the original .bm2 file structure
            System.out.println("\n2. Let's check what should be in the .bm2 file:");
            System.out.println("   This requires loading the BMDExpress project to see the actual structure");
            
            // Check if we're missing data by looking at the category analysis results sets
            System.out.println("\n3. Category Analysis Results Sets details:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, bmdResultId FROM categoryAnalysisResultsSets ORDER BY id")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getLong(1) + ", Name: " + rs.getString(2) + ", BMDResultId: " + rs.getLong(3));
                }
            }
            
            // Check BMD results
            System.out.println("\n4. BMD Results details:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM bmdResults ORDER BY id")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getLong(1) + ", Name: " + rs.getString(2));
                }
            }
            
            // Check if there are multiple category analysis results that should be separate sets
            System.out.println("\n5. Category Analysis Results by set:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT categoryAnalysisResultsId, COUNT(*) as count FROM categoryAnalysisResults GROUP BY categoryAnalysisResultsId ORDER BY categoryAnalysisResultsId")) {
                while (rs.next()) {
                    System.out.println("   Set ID: " + rs.getLong(1) + ", Results count: " + rs.getInt(2));
                }
            }
            
            // Check unique category analysis result names
            System.out.println("\n6. Unique category analysis result names:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT name FROM categoryAnalysisResultsSets ORDER BY name")) {
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            }
        }
    }
}
