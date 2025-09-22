import java.sql.*;

/**
 * Test which DuckDB configuration parameters work in WASM mode
 */
public class TestDuckDBWasmConfig {
    public static void main(String[] args) {
        String dbPath = "p3mp_optimized.duckdb";
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            System.out.println("🔍 Testing DuckDB WASM configuration parameters...");
            
            // Test memory limit
            testConfig(conn, "SET memory_limit='2GB'", "memory_limit");
            
            // Test temporary directory
            testConfig(conn, "SET temp_directory='/tmp/duckdb_temp'", "temp_directory");
            
            // Test profiling
            testConfig(conn, "SET enable_profiling='json'", "enable_profiling");
            
            // Test optimizers
            testConfig(conn, "SET disabled_optimizers=''", "disabled_optimizers");
            
            // Test verification
            testConfig(conn, "SET enable_verification=true", "enable_verification");
            
            // Test threads (may not work in web worker environment)
            testConfig(conn, "SET threads=4", "threads (web worker)");
            
            // Test progress bar
            testConfig(conn, "SET enable_progress_bar=true", "enable_progress_bar");
            
            // Test other potential parameters
            testConfig(conn, "SET max_memory='2GB'", "max_memory");
            testConfig(conn, "SET memory_limit='2GB'", "memory_limit (alternative)");
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testConfig(Connection conn, String sql, String description) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("  ✅ " + description + " - SUCCESS");
        } catch (SQLException e) {
            System.out.println("  ❌ " + description + " - FAILED: " + e.getMessage());
        }
    }
}
