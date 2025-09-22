import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * Test the full complex query with optimizations applied
 */
public class TestComplexQueryOptimized {
    public static void main(String[] args) {
        String dbPath = "p3mp_webapp_schema.duckdb";
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            System.out.println("🚀 Testing optimized complex query...");
            
            // Test the full complex query with different LIMIT sizes
            testComplexQueryWithLimits(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testComplexQueryWithLimits(Connection conn) throws SQLException {
        System.out.println("\n🔍 Testing complex query with different LIMIT sizes...");
        
        int[] limits = {10, 50, 100, 500};
        
        for (int limit : limits) {
            System.out.println("\n🧪 Testing with LIMIT " + limit + "...");
            
            String complexQuery = buildComplexQuery(limit);
            
            long startTime = System.currentTimeMillis();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(complexQuery)) {
                
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    if (rowCount <= 3) { // Show first 3 rows
                        System.out.println("  📋 Row " + rowCount + ": " + 
                            rs.getString("set_name") + " | " + 
                            rs.getString("organ") + " | " + 
                            rs.getInt("bmdResultId"));
                    }
                }
                
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("  ✅ Query executed successfully in " + executionTime + "ms");
                System.out.println("  📊 Returned " + rowCount + " rows");
                
                if (executionTime > 30000) { // 30 seconds
                    System.out.println("  ❌ Query is too slow (> 30 seconds) - stopping test");
                    break;
                } else if (executionTime > 15000) { // 15 seconds
                    System.out.println("  ⚠️  Query is getting slow (15-30 seconds)");
                } else {
                    System.out.println("  🚀 Query performance is good (< 15 seconds)");
                }
                
            } catch (SQLException e) {
                System.err.println("  ❌ Query failed with LIMIT " + limit + ": " + e.getMessage());
                if (e.getMessage().contains("Out of Memory") || e.getMessage().contains("memory")) {
                    System.err.println("  💾 Memory error detected - optimizations may need adjustment");
                }
                break;
            }
        }
    }
    
    private static String buildComplexQuery(int limit) {
        return "WITH category_analysis_data AS (" +
            "SELECT " +
            "cars.id as set_id, " +
            "cars.name as set_name, " +
            "cars.bmdResultId, " +
            "cars.sex, " +
            "cars.organ, " +
            "cars.species, " +
            "cars.dataType, " +
            "cars.platform, " +
            "car.id as result_id, " +
            "car.categoryIdentifierId, " +
            "car.modelType, " +
            "car.geneAllCount, " +
            "car.percentage, " +
            "car.genesThatPassedAllFilters, " +
            "car.bmdFifthPercentileTotalGenes, " +
            "ci.id as category_id, " +
            "ci.title as category_title, " +
            "ci.modelType as category_model_type, " +
            "br.name as bmd_result_name, " +
            "br.organ as bmd_organ, " +
            "br.species as bmd_species, " +
            "br.dataType as bmd_dataType, " +
            "br.platform as bmd_platform, " +
            "br.bmdMethod, " +
            "br.wAUC, " +
            "br.logwAUC, " +
            "dre.id as experiment_id, " +
            "dre.name as experiment_name, " +
            "dre.chipId, " +
            "dre.logTransformation, " +
            "dre.columnHeader2, " +
            "dre.chipCreationDate " +
            "FROM categoryAnalysisResultsSets cars " +
            "JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId " +
            "JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id " +
            "JOIN bmdResults br ON cars.bmdResultId = br.id " +
            "JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id " +
            "WHERE ci.modelType = 'go' " +
            "AND car.percentage >= 5 " +
            "AND car.geneAllCount BETWEEN 40 AND 500 " +
            "AND car.genesThatPassedAllFilters >= 3" +
            ") " +
            "SELECT " +
            "cad.set_name, " +
            "cad.set_id, " +
            "cad.bmdResultId, " +
            "cad.sex, " +
            "cad.organ, " +
            "cad.species, " +
            "cad.dataType, " +
            "cad.platform, " +
            "COUNT(*) as result_count " +
            "FROM category_analysis_data cad " +
            "GROUP BY " +
            "cad.set_name, cad.set_id, cad.bmdResultId, " +
            "cad.sex, cad.organ, cad.species, cad.dataType, cad.platform " +
            "LIMIT " + limit;
    }
}
