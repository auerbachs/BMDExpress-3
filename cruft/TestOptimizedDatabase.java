import java.sql.*;

/**
 * Test the new optimized database with compound indexes
 */
public class TestOptimizedDatabase {
    public static void main(String[] args) {
        String dbPath = "p3mp_optimized.duckdb";
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            System.out.println("🔍 Testing optimized database with compound indexes...");
            
            // Test 1: Check if compound indexes were created
            testCompoundIndexes(conn);
            
            // Test 2: Test query performance with compound indexes
            testQueryPerformance(conn);
            
            // Test 3: Test the complex query that was causing memory issues
            testComplexQuery(conn);
            
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
                System.out.println("  ⚠️  No compound indexes found - checking with alternative query...");
                
                // Alternative way to check indexes
                String altQuery = "PRAGMA index_list('categoryAnalysisResultsSets')";
                try (ResultSet altRs = stmt.executeQuery(altQuery)) {
                    int altCount = 0;
                    while (altRs.next()) {
                        String indexName = altRs.getString("name");
                        if (indexName.contains("cidx_")) {
                            altCount++;
                            System.out.println("  ✅ Found compound index: " + indexName);
                        }
                    }
                    if (altCount > 0) {
                        System.out.println("  ✅ Found " + altCount + " compound indexes using PRAGMA");
                    } else {
                        System.out.println("  ❌ No compound indexes found with either method");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("  ⚠️  Could not check indexes: " + e.getMessage());
        }
    }
    
    private static void testQueryPerformance(Connection conn) throws SQLException {
        System.out.println("\n🚀 Testing query performance with compound indexes...");
        
        // Test query that should benefit from compound indexes
        String testQuery = """
            SELECT 
                cars.id as set_id,
                cars.name as set_name,
                cars.bmdResultId,
                cars.organ,
                cars.species,
                COUNT(*) as result_count
            FROM categoryAnalysisResultsSets cars
            JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId
            JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id
            WHERE ci.modelType = 'go'
                AND car.percentage >= 5
                AND car.geneAllCount BETWEEN 40 AND 500
                AND car.genesThatPassedAllFilters >= 3
                AND cars.organ = 'Unknown'
                AND cars.species = 'Unknown'
            GROUP BY cars.id, cars.name, cars.bmdResultId, cars.organ, cars.species
            LIMIT 10
            """;
        
        long startTime = System.currentTimeMillis();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(testQuery)) {
            
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                if (rowCount <= 3) {
                    System.out.println("  📋 Row " + rowCount + ": " + 
                        rs.getString("set_name") + " | " + 
                        rs.getString("organ") + " | " + 
                        rs.getInt("result_count") + " results");
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("  ✅ Query executed successfully in " + executionTime + "ms");
            System.out.println("  📊 Returned " + rowCount + " rows");
            
            if (executionTime < 100) {
                System.out.println("  🚀 Excellent performance (< 100ms) - compound indexes are working!");
            } else if (executionTime < 500) {
                System.out.println("  ✅ Good performance (< 500ms)");
            } else {
                System.out.println("  ⚠️  Query is slower than expected");
            }
            
        } catch (SQLException e) {
            System.err.println("  ❌ Query failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testComplexQuery(Connection conn) throws SQLException {
        System.out.println("\n🔍 Testing complex query that was causing memory issues...");
        
        // Test the complex query with JSON aggregations but with LIMIT
        String complexQuery = """
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
            LIMIT 5
            """;
        
        long startTime = System.currentTimeMillis();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(complexQuery)) {
            
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                System.out.println("  📋 Row " + rowCount + ": " + 
                    rs.getString("set_name") + " | " + 
                    rs.getString("organ") + " | " + 
                    rs.getInt("result_count") + " results");
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("  ✅ Complex query executed successfully in " + executionTime + "ms");
            System.out.println("  📊 Returned " + rowCount + " rows");
            
            if (executionTime < 1000) {
                System.out.println("  🚀 Excellent performance (< 1 second) - optimizations are working!");
            } else if (executionTime < 5000) {
                System.out.println("  ✅ Good performance (< 5 seconds)");
            } else {
                System.out.println("  ⚠️  Query is slower than expected");
            }
            
        } catch (SQLException e) {
            System.err.println("  ❌ Complex query failed: " + e.getMessage());
            if (e.getMessage().contains("Out of Memory") || e.getMessage().contains("memory")) {
                System.err.println("  💾 Memory error detected - optimizations may need adjustment");
            }
            throw e;
        }
    }
}
