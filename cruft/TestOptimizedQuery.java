import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * Test the optimized query performance with the large P3MP database
 */
public class TestOptimizedQuery {
    public static void main(String[] args) {
        String dbPath = "p3mp_webapp_schema.duckdb";
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            System.out.println("🔍 Testing optimized query performance...");
            
            // Test 1: Check if compound indexes were created
            testCompoundIndexes(conn);
            
            // Test 2: Test query with LIMIT to prevent memory issues
            testQueryWithLimit(conn);
            
            // Test 3: Test query performance analysis
            testQueryPerformance(conn);
            
            // Test 4: Test memory usage with different LIMIT sizes
            testMemoryUsage(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testCompoundIndexes(Connection conn) throws SQLException {
        System.out.println("\n📊 Testing compound indexes...");
        
        // Check for compound indexes on categoryAnalysisResultsSets
        String indexQuery = """
            SELECT indexname, indexdef 
            FROM pg_indexes 
            WHERE tablename = 'categoryAnalysisResultsSets' 
            AND indexname LIKE 'cidx_%'
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(indexQuery)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  ✅ Found compound index: " + rs.getString("indexname"));
            }
            
            if (count > 0) {
                System.out.println("  ✅ Found " + count + " compound indexes on categoryAnalysisResultsSets");
            } else {
                System.out.println("  ⚠️  No compound indexes found - this may indicate schema issues");
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️  Could not check indexes (this is expected with some DuckDB versions): " + e.getMessage());
        }
    }
    
    private static void testQueryWithLimit(Connection conn) throws SQLException {
        System.out.println("\n🚀 Testing query with LIMIT...");
        
        // Simplified version of the complex query with LIMIT
        String testQuery = """
            WITH category_analysis_data AS (
              SELECT 
                cars.id as set_id,
                cars.name as set_name,
                cars.bmdResultId,
                cars.sex,
                cars.organ,
                cars.species,
                cars.dataType,
                cars.platform,
                car.id as result_id,
                car.categoryIdentifierId,
                car.modelType,
                car.geneAllCount,
                car.percentage,
                car.genesThatPassedAllFilters,
                car.bmdFifthPercentileTotalGenes,
                ci.id as category_id,
                ci.title as category_title,
                ci.modelType as category_model_type,
                br.name as bmd_result_name,
                br.organ as bmd_organ,
                br.species as bmd_species,
                br.dataType as bmd_dataType,
                br.platform as bmd_platform,
                br.bmdMethod,
                br.wAUC,
                br.logwAUC,
                dre.id as experiment_id,
                dre.name as experiment_name,
                dre.chipId,
                dre.logTransformation,
                dre.columnHeader2,
                dre.chipCreationDate
              FROM categoryAnalysisResultsSets cars
              JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId
              JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id
              JOIN bmdResults br ON cars.bmdResultId = br.id
              JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id
              WHERE ci.modelType = 'go'
                AND car.percentage >= 5
                AND car.geneAllCount BETWEEN 40 AND 500
                AND car.genesThatPassedAllFilters >= 3
            )
            SELECT 
              cad.set_name,
              cad.set_id,
              cad.bmdResultId,
              cad.sex,
              cad.organ,
              cad.species,
              cad.dataType,
              cad.platform,
              COUNT(*) as result_count
            FROM category_analysis_data cad
            GROUP BY 
              cad.set_name, cad.set_id, cad.bmdResultId,
              cad.sex, cad.organ, cad.species, cad.dataType, cad.platform
            LIMIT 100
            """;
        
        long startTime = System.currentTimeMillis();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(testQuery)) {
            
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                if (rowCount <= 5) { // Show first 5 rows
                    System.out.println("  📋 Row " + rowCount + ": " + 
                        rs.getString("set_name") + " | " + 
                        rs.getString("organ") + " | " + 
                        rs.getInt("result_count") + " results");
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("  ✅ Query executed successfully in " + executionTime + "ms");
            System.out.println("  📊 Returned " + rowCount + " rows (limited to 100)");
            
            if (executionTime < 5000) {
                System.out.println("  🚀 Query performance is good (< 5 seconds)");
            } else if (executionTime < 15000) {
                System.out.println("  ⚠️  Query performance is acceptable (5-15 seconds)");
            } else {
                System.out.println("  ❌ Query performance is poor (> 15 seconds)");
            }
            
        } catch (SQLException e) {
            System.err.println("  ❌ Query failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testQueryPerformance(Connection conn) throws SQLException {
        System.out.println("\n🔍 Testing query performance analysis...");
        
        // Test EXPLAIN on a simple query
        String explainQuery = """
            EXPLAIN SELECT COUNT(*) 
            FROM categoryAnalysisResultsSets cars
            JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId
            WHERE car.percentage >= 5
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(explainQuery)) {
            
            System.out.println("  📋 Query execution plan:");
            while (rs.next()) {
                String plan = rs.getString(1);
                if (plan != null) {
                    System.out.println("    " + plan);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("  ⚠️  Could not get execution plan: " + e.getMessage());
        }
    }
    
    private static void testMemoryUsage(Connection conn) throws SQLException {
        System.out.println("\n💾 Testing memory usage with different LIMIT sizes...");
        
        int[] limits = {10, 50, 100, 500, 1000};
        
        for (int limit : limits) {
            System.out.println("  🧪 Testing with LIMIT " + limit + "...");
            
            String query = "SELECT cars.id, cars.name, cars.organ, cars.species " +
                "FROM categoryAnalysisResultsSets cars " +
                "JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId " +
                "JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id " +
                "WHERE ci.modelType = 'go' " +
                "LIMIT " + limit;
            
            long startTime = System.currentTimeMillis();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }
                
                long executionTime = System.currentTimeMillis() - startTime;
                System.out.println("    ✅ " + rowCount + " rows in " + executionTime + "ms");
                
                if (executionTime > 10000) { // 10 seconds
                    System.out.println("    ⚠️  Query is getting slow with larger limits");
                    break;
                }
                
            } catch (SQLException e) {
                System.err.println("    ❌ Query failed with LIMIT " + limit + ": " + e.getMessage());
                break;
            }
        }
    }
}
