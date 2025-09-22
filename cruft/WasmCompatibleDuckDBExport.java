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

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.service.DuckDBExportService;

public class WasmCompatibleDuckDBExport {
    
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.err.println("Usage: java WasmCompatibleDuckDBExport <input.bm2> <output.duckdb> [--format regular|wasm]");
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
        
        System.out.println("BMDExpress -> WASM-Compatible DuckDB Exporter");
        System.out.println("=============================================");
        System.out.println("Input .bm2 file: " + inputBM2);
        System.out.println("Output DuckDB file: " + outputDB);
        System.out.println("Format: " + format.toUpperCase());
        System.out.println();
        
        // Load BMD Project from .bm2 file
        System.out.println("Loading BMD project...");
        BMDProject project = loadBMDProject(inputBM2);
        if (project == null) {
            System.err.println("ERROR: Failed to load BMD project from " + inputBM2);
            System.exit(1);
        }
        
        System.out.println("Successfully loaded project: " + project.getName());
        printProjectSummary(project);
        System.out.println();
        
        // Create database in specified format
        try {
            if (format.equals("wasm")) {
                createWasmCompatibleDatabase(project, outputDB);
            } else {
                createRegularDatabase(project, outputDB);
            }
            
            System.out.println("\n✅ Export completed successfully!");
            System.out.println("Database created in " + format.toUpperCase() + " format");
            if (format.equals("wasm")) {
                System.out.println("This database is compatible with DuckDB WASM (web applications)");
            } else {
                System.out.println("This database is compatible with DuckDB JDBC (server applications)");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static BMDProject loadBMDProject(String bm2FilePath) {
        try {
            FileInputStream fileIn = new FileInputStream(new File(bm2FilePath));
            BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);
            ObjectInputStream in = new ObjectInputStream(bIn);
            
            BMDProject project = (BMDProject) in.readObject();
            
            in.close();
            fileIn.close();
            
            return project;
            
        } catch (Exception e) {
            System.err.println("Error loading BMD project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void printProjectSummary(BMDProject project) {
        System.out.println("Project Summary:");
        System.out.println("  - Dose Response Experiments: " + project.getDoseResponseExperiments().size());
        System.out.println("  - One-Way ANOVA Results: " + project.getOneWayANOVAResults().size());
        System.out.println("  - Williams Trend Results: " + project.getWilliamsTrendResults().size());
        System.out.println("  - Curve Fit Prefilter Results: " + project.getCurveFitPrefilterResults().size());
        System.out.println("  - Oriogen Results: " + project.getOriogenResults().size());
        System.out.println("  - BMD Analysis Results: " + project.getbMDResult().size());
        System.out.println("  - Category Analysis Results: " + project.getCategoryAnalysisResults().size());
    }
    
    /**
     * Create a WASM-compatible DuckDB database using the web application schema
     */
    private static void createWasmCompatibleDatabase(BMDProject project, String dbPath) throws Exception {
        System.out.println("Creating WASM-compatible DuckDB database with web application schema...");
        
        // Load DuckDB JDBC driver
        Class.forName("org.duckdb.DuckDBDriver");
        
        // Create new database file (delete if exists)
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            
            // Configure DuckDB for optimal performance and memory management
            System.out.println("  Configuring DuckDB for optimal performance...");
            configureDuckDBSettings(conn);
            
            // Create web application schema
            System.out.println("  Creating web application schema...");
            WebAppSchemaCreator schemaCreator = new WebAppSchemaCreator(conn);
            schemaCreator.createSchema();
            
            // Map BMDProject data to web application schema
            System.out.println("  Mapping BMDProject data to web application schema...");
            BMDDataMapper dataMapper = new BMDDataMapper(conn);
            dataMapper.mapProjectData(project);
            
        }
        
        System.out.println("✅ WASM-compatible database created successfully!");
    }
    
    
    /**
     * Create a regular JDBC DuckDB database using the existing DuckDBExportService
     */
    private static void createRegularDatabase(BMDProject project, String dbPath) throws Exception {
        System.out.println("Creating regular JDBC DuckDB database...");
        
        // Use the existing DuckDBExportService
        DuckDBExportService exportService = new DuckDBExportService();
        exportService.exportProject(project, dbPath);
        
        System.out.println("✅ Regular JDBC database created successfully!");
    }
    
    /**
     * Configure DuckDB settings for optimal performance and memory management
     */
    private static void configureDuckDBSettings(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Set memory limits to prevent out-of-memory errors
            // Reserve 1GB for the JVM, limit DuckDB to 2GB
            stmt.execute("SET memory_limit='2GB'");
            System.out.println("    ✅ Set memory limit to 2GB");
            
            // Set temporary directory for offloading intermediate results
            // Use a virtual temp directory that DuckDB can manage
            stmt.execute("SET temp_directory='/tmp/duckdb_temp'");
            System.out.println("    ✅ Set temporary directory for intermediate results");
            
            // Enable query optimization (WASM-compatible parameters)
            stmt.execute("SET enable_profiling='json'");
            stmt.execute("SET disabled_optimizers=''"); // Enable all optimizers
            System.out.println("    ✅ Enabled query optimization and profiling");
            
            // Note: threads parameter not available in web worker environment
            // DuckDB WASM runs single-threaded in the browser
            
            // Enable parallel processing for large queries
            stmt.execute("SET enable_progress_bar=true");
            System.out.println("    ✅ Enabled progress bar for long-running queries");
            
        } catch (SQLException e) {
            System.out.println("    ⚠️  Could not configure all DuckDB settings: " + e.getMessage());
            // Don't fail the entire operation - some settings might not be available
        }
    }
    
}
