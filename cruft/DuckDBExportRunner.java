package com.sciome.bmdexpress2.commandline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.service.DuckDBExportService;

/**
 * Command-line runner to export BMDExpress .bm2 files to DuckDB database
 */
public class DuckDBExportRunner {
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -cp bmdexpress3.jar com.sciome.bmdexpress2.commandline.DuckDBExportRunner <input.bm2> <output.duckdb>");
            System.err.println("Example: java -cp bmdexpress3.jar com.sciome.bmdexpress2.commandline.DuckDBExportRunner myproject.bm2 myproject.duckdb");
            System.exit(1);
        }
        
        String inputBM2 = args[0];
        String outputDB = args[1];
        
        DuckDBExportRunner runner = new DuckDBExportRunner();
        runner.exportToDuckDB(inputBM2, outputDB);
    }
    
    public void exportToDuckDB(String inputBM2File, String outputDBFile) {
        System.out.println("BMDExpress -> DuckDB Exporter");
        System.out.println("============================");
        System.out.println("Input .bm2 file: " + inputBM2File);
        System.out.println("Output DuckDB file: " + outputDBFile);
        System.out.println();
        
        // Check if input file exists
        File inputFile = new File(inputBM2File);
        if (!inputFile.exists()) {
            System.err.println("ERROR: Input file does not exist: " + inputBM2File);
            return;
        }
        
        // Load BMD Project from .bm2 file
        BMDProject project = loadBMDProject(inputBM2File);
        if (project == null) {
            System.err.println("ERROR: Failed to load BMD project from " + inputBM2File);
            return;
        }
        
        System.out.println("Successfully loaded project: " + project.getName());
        printProjectSummary(project);
        System.out.println();
        
        // Export to DuckDB
        try {
            DuckDBExportService exportService = new DuckDBExportService();
            exportService.exportProject(project, outputDBFile);
            
            System.out.println();
            System.out.println("SUCCESS: Export completed! Database saved to: " + outputDBFile);
            System.out.println();
            printUsageInstructions(outputDBFile);
            
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to export to DuckDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private BMDProject loadBMDProject(String bm2FilePath) {
        try {
            FileInputStream fileIn = new FileInputStream(new File(bm2FilePath));
            BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);
            ObjectInputStream in = new ObjectInputStream(bIn);
            
            BMDProject project = (BMDProject) in.readObject();
            
            in.close();
            fileIn.close();
            
            return project;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading BMD project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void printProjectSummary(BMDProject project) {
        System.out.println("Project Summary:");
        System.out.println("  - Dose Response Experiments: " + project.getDoseResponseExperiments().size());
        System.out.println("  - One-Way ANOVA Results: " + project.getOneWayANOVAResults().size());
        System.out.println("  - Williams Trend Results: " + project.getWilliamsTrendResults().size());
        System.out.println("  - Curve Fit Prefilter Results: " + project.getCurveFitPrefilterResults().size());
        System.out.println("  - Oriogen Results: " + project.getOriogenResults().size());
        System.out.println("  - BMD Analysis Results: " + project.getbMDResult().size());
        System.out.println("  - Category Analysis Results: " + project.getCategoryAnalysisResults().size());
    }
    
    private void printUsageInstructions(String dbFile) {
        System.out.println("Usage Instructions:");
        System.out.println("==================");
        System.out.println();
        System.out.println("You can now query your data using DuckDB CLI or any SQL interface:");
        System.out.println();
        System.out.println("1. Install DuckDB CLI:");
        System.out.println("   brew install duckdb  # macOS");
        System.out.println("   # or download from https://duckdb.org/");
        System.out.println();
        System.out.println("2. Query your data:");
        System.out.println("   duckdb " + dbFile);
        System.out.println();
        System.out.println("3. Example queries:");
        System.out.println("   -- List all tables");
        System.out.println("   SHOW TABLES;");
        System.out.println();
        System.out.println("   -- View project info");
        System.out.println("   SELECT * FROM projects;");
        System.out.println();
        System.out.println("   -- Find probes with low BMD values");
        System.out.println("   SELECT p.symbol, psr.best_bmd, psr.best_model");
        System.out.println("   FROM probe_stat_results psr");
        System.out.println("   JOIN probes p ON psr.probe_id = p.id");
        System.out.println("   WHERE psr.best_bmd < 10");
        System.out.println("   ORDER BY psr.best_bmd;");
        System.out.println();
        System.out.println("   -- Get category analysis summary");
        System.out.println("   SELECT category_type, category_description, bmd_median, p_value");
        System.out.println("   FROM category_results");
        System.out.println("   WHERE p_value < 0.05");
        System.out.println("   ORDER BY p_value;");
        System.out.println();
        System.out.println("4. Use with Python/R:");
        System.out.println("   Python: import duckdb; conn = duckdb.connect('" + dbFile + "')");
        System.out.println("   R: library(duckdb); con <- dbConnect(duckdb(), '" + dbFile + "')");
    }
}