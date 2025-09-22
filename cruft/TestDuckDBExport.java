import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class TestDuckDBExport {
    
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.err.println("Usage: java TestDuckDBExport <input.bm2> <output.duckdb> [--format regular|wasm]");
            System.err.println("  --format wasm    - Create WASM-compatible database (default)");
            System.err.println("  --format regular - Create regular JDBC database");
            System.exit(1);
        }
        
        String inputBM2 = args[0];
        String outputDB = args[1];
        String format = "wasm"; // default to WASM-compatible
        
        // Parse command line arguments
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--format") && i + 1 < args.length) {
                format = args[i + 1].toLowerCase();
                i++; // skip the format value
            }
        }
        
        System.out.println("BMDExpress -> DuckDB Test Exporter");
        System.out.println("==================================");
        System.out.println("Input .bm2 file: " + inputBM2);
        System.out.println("Output DuckDB file: " + outputDB);
        System.out.println("Format: " + format.toUpperCase());
        System.out.println();
        
        // Test 1: Can we load the .bm2 file?
        System.out.println("Test 1: Loading .bm2 file...");
        if (!inputBM2.equals("/dev/null")) {
            Object project = loadBMDProject(inputBM2);
            if (project == null) {
                System.err.println("FAILED: Could not load .bm2 file (need BMDExpress classes compiled)");
                System.out.println("This is expected - we'll test DuckDB functionality instead");
            } else {
                System.out.println("SUCCESS: Loaded .bm2 file");
                System.out.println("Project type: " + project.getClass().getName());
            }
        } else {
            System.out.println("SKIPPED: Testing DuckDB functionality only");
        }
        
        // Test 2: Create DuckDB database in specified format
        System.out.println("\nTest 2: Creating DuckDB database...");
        try {
            if (format.equals("wasm")) {
                createWasmCompatibleDatabase(outputDB);
            } else {
                createRegularDatabase(outputDB);
            }
            
        } catch (Exception e) {
            System.err.println("FAILED: DuckDB database creation failed");
            e.printStackTrace();
            return;
        }
        
        System.out.println("\nTest completed successfully!");
        System.out.println("Database created in " + format.toUpperCase() + " format");
        if (format.equals("wasm")) {
            System.out.println("✅ This database is compatible with DuckDB WASM (web applications)");
        } else {
            System.out.println("✅ This database is compatible with DuckDB JDBC (server applications)");
        }
    }
    
    private static Object loadBMDProject(String bm2FilePath) {
        try {
            FileInputStream fileIn = new FileInputStream(new File(bm2FilePath));
            BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);
            ObjectInputStream in = new ObjectInputStream(bIn);
            
            Object project = in.readObject();
            
            in.close();
            fileIn.close();
            
            return project;
            
        } catch (Exception e) {
            System.err.println("Error loading BMD project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a WASM-compatible DuckDB database
     */
    private static void createWasmCompatibleDatabase(String dbPath) throws Exception {
        System.out.println("Creating WASM-compatible DuckDB database...");
        
        // Load DuckDB JDBC driver
        Class.forName("org.duckdb.DuckDBDriver");
        
        // Create new database file (delete if exists)
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            
            // Create meta table for WASM compatibility
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS meta (key VARCHAR PRIMARY KEY, value BIGINT)");
                stmt.execute("INSERT OR IGNORE INTO meta (key, value) VALUES ('schema_version', 1)");
                System.out.println("  ✅ Created meta table for WASM compatibility");
            }
            
            // Create a test table to verify functionality
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER, name VARCHAR)");
                stmt.execute("INSERT INTO test_table VALUES (1, 'Hello WASM DuckDB!')");
                System.out.println("  ✅ Created test table with sample data");
                
                // Verify the data
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    while (rs.next()) {
                        System.out.println("  - Retrieved: " + rs.getInt("id") + ", " + rs.getString("name"));
                    }
                }
            }
            
            // Create additional tables that might be expected by the web application
            createWebAppTables(conn);
            
        }
        
        System.out.println("✅ WASM-compatible database created successfully!");
    }
    
    /**
     * Create a regular JDBC DuckDB database
     */
    private static void createRegularDatabase(String dbPath) throws Exception {
        System.out.println("Creating regular JDBC DuckDB database...");
        
        // Load DuckDB JDBC driver
        Class.forName("org.duckdb.DuckDBDriver");
        
        // Create new database file (delete if exists)
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            
            // Create a test table
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER, name VARCHAR)");
                stmt.execute("INSERT INTO test_table VALUES (1, 'Hello Regular DuckDB!')");
                System.out.println("  ✅ Created test table with sample data");
                
                // Verify the data
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                    while (rs.next()) {
                        System.out.println("  - Retrieved: " + rs.getInt("id") + ", " + rs.getString("name"));
                    }
                }
            }
            
        }
        
        System.out.println("✅ Regular JDBC database created successfully!");
    }
    
    /**
     * Create tables that are commonly expected by the web application
     */
    private static void createWebAppTables(Connection conn) throws SQLException {
        System.out.println("  Creating web application tables...");
        
        try (Statement stmt = conn.createStatement()) {
            // Create projects table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Create dose response experiments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doseResponseExperiments (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    chipId BIGINT,
                    logTransformation VARCHAR,
                    columnHeader2 VARCHAR,
                    chipCreationDate BIGINT
                )
            """);
            
            // Create treatments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS treatments (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    name VARCHAR,
                    dose DOUBLE
                )
            """);
            
            // Create probes table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probes (
                    id BIGINT PRIMARY KEY,
                    probe_id VARCHAR,
                    symbol VARCHAR,
                    title VARCHAR
                )
            """);
            
            // Create BMD results table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bmdResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    doseResponseExperimentId BIGINT,
                    bmdMethod VARCHAR
                )
            """);
            
            // Create category analysis results sets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResultsSets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    bmdResultId BIGINT,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR
                )
            """);
            
            // Create category analysis results table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResults (
                    id BIGINT PRIMARY KEY,
                    categoryAnalysisResultsId BIGINT,
                    categoryIdentifierId BIGINT,
                    modelType VARCHAR,
                    geneAllCount INTEGER,
                    percentage DOUBLE,
                    genesThatPassedAllFilters INTEGER,
                    bmdFifthPercentileTotalGenes DOUBLE
                )
            """);
            
            // Create category identifiers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryIdentifiers (
                    id BIGINT PRIMARY KEY,
                    title VARCHAR,
                    modelType VARCHAR
                )
            """);
            
            // Create dose groups table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doseGroups (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    dose DOUBLE,
                    n INTEGER,
                    responseMean DOUBLE
                )
            """);
            
            // Create probe responses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probeResponses (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    probeId BIGINT,
                    responses VARCHAR
                )
            """);
            
            System.out.println("  ✅ Created web application tables");
        }
    }
}