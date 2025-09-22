import java.sql.*;

public class TestQuery {
    public static void main(String[] args) throws Exception {
        Class.forName("org.duckdb.DuckDBDriver");
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:p3mp_webapp_schema.duckdb")) {
            System.out.println("Testing query components...\n");
            
            // Test 1: Check categoryAnalysisResultsSets
            System.out.println("1. categoryAnalysisResultsSets:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 2: Check categoryAnalysisResults
            System.out.println("\n2. categoryAnalysisResults:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResults")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 3: Check categoryIdentifiers
            System.out.println("\n3. categoryIdentifiers:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryIdentifiers")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 4: Check bmdResults
            System.out.println("\n4. bmdResults:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bmdResults")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 5: Check doseResponseExperiments
            System.out.println("\n5. doseResponseExperiments:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM doseResponseExperiments")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 6: Check JOIN between categoryAnalysisResultsSets and categoryAnalysisResults
            System.out.println("\n6. JOIN categoryAnalysisResultsSets + categoryAnalysisResults:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 7: Check JOIN with categoryIdentifiers
            System.out.println("\n7. JOIN with categoryIdentifiers:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 8: Check JOIN with bmdResults
            System.out.println("\n8. JOIN with bmdResults:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id JOIN bmdResults br ON cars.bmdResultId = br.id")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 9: Check JOIN with doseResponseExperiments
            System.out.println("\n9. JOIN with doseResponseExperiments:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id JOIN bmdResults br ON cars.bmdResultId = br.id JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 10: Check WHERE clause
            System.out.println("\n10. WITH WHERE clause (ci.modelType = 'go'):");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id JOIN bmdResults br ON cars.bmdResultId = br.id JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id WHERE ci.modelType = 'go'")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 11: Check additional WHERE conditions
            System.out.println("\n11. WITH additional WHERE conditions:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categoryAnalysisResultsSets cars JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id JOIN bmdResults br ON cars.bmdResultId = br.id JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id WHERE ci.modelType = 'go' AND car.percentage >= 5 AND car.geneAllCount BETWEEN 40 AND 500 AND car.genesThatPassedAllFilters >= 3")) {
                rs.next();
                System.out.println("   Count: " + rs.getInt(1));
            }
            
            // Test 12: Check what modelType values exist
            System.out.println("\n12. modelType values in categoryIdentifiers:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DISTINCT modelType FROM categoryIdentifiers LIMIT 10")) {
                while (rs.next()) {
                    System.out.println("   " + rs.getString(1));
                }
            }
            
            // Test 13: Check sample data from categoryAnalysisResults
            System.out.println("\n13. Sample categoryAnalysisResults data:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT categoryIdentifierId, modelType, geneAllCount, percentage, genesThatPassedAllFilters FROM categoryAnalysisResults LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("   ID: " + rs.getString(1) + ", ModelType: " + rs.getString(2) + ", Genes: " + rs.getLong(3) + ", %: " + rs.getDouble(4) + ", Passed: " + rs.getLong(5));
                }
            }
        }
    }
}
