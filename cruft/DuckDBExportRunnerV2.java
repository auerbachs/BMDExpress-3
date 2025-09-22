package com.sciome.bmdexpress2.commandline;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.service.DuckDBExportServiceV2;
import com.sciome.bmdexpress2.util.MatrixData;
import com.sciome.bmdexpress2.util.ProjectUtilities;

/**
 * Command-line runner for DuckDB Export Service V2
 * 
 * Usage: java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV2 input.bm2 output.duckdb
 */
public class DuckDBExportRunnerV2 {
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java DuckDBExportRunnerV2 <input.bm2> <output.duckdb>");
            System.exit(1);
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        
        try {
            System.out.println("DuckDB Export Service V2 - Normalized Schema");
            System.out.println("==============================================");
            System.out.println("Input file: " + inputFile);
            System.out.println("Output file: " + outputFile);
            System.out.println();
            
            // Load BMD project
            System.out.println("Loading BMD project...");
            BMDProject project = loadProject(inputFile);
            
            if (project == null) {
                System.err.println("Failed to load project from: " + inputFile);
                System.exit(1);
            }
            
            System.out.println("Project loaded successfully: " + project.getName());
            
            // Print project statistics
            printProjectStats(project);
            
            // Export to DuckDB
            System.out.println("\nExporting to normalized DuckDB schema...");
            DuckDBExportServiceV2 exportService = new DuckDBExportServiceV2();
            exportService.exportProject(project, outputFile);
            
            // Verify output file
            File dbFile = new File(outputFile);
            if (dbFile.exists()) {
                System.out.println("Export completed successfully!");
                System.out.println("Database file size: " + (dbFile.length() / 1024 / 1024) + " MB");
                System.out.println();
                System.out.println("You can now query the database with:");
                System.out.println("  duckdb " + outputFile);
                System.out.println();
                System.out.println("Example queries:");
                System.out.println("  SHOW TABLES;");
                System.out.println("  SELECT * FROM category_analysis_results WHERE gene_all_count >= 40 AND gene_all_count <= 500 AND percentage >= 5;");
                System.out.println("  SELECT category_identifier_id, gene_all_count, genes_that_passed_all_filters, bmd_fifth_percentile_total_genes FROM category_analysis_results;");
            } else {
                System.err.println("Export failed - output file not created");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("Export failed with error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static BMDProject loadProject(String inputFile) {
        try {
            File file = new File(inputFile);
            if (!file.exists()) {
                System.err.println("Input file does not exist: " + inputFile);
                return null;
            }
            
            // Load BMD project using Java object deserialization (same as original)
            FileInputStream fileIn = new FileInputStream(file);
            BufferedInputStream bIn = new BufferedInputStream(fileIn, 1024 * 2000);
            ObjectInputStream in = new ObjectInputStream(bIn);
            
            BMDProject project = (BMDProject) in.readObject();
            
            in.close();
            fileIn.close();
            
            return project;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void printProjectStats(BMDProject project) {
        System.out.println("\nProject Statistics:");
        System.out.println("==================");
        
        // Experiments
        int experimentCount = project.getDoseResponseExperiments() != null ? project.getDoseResponseExperiments().size() : 0;
        System.out.println("Dose Response Experiments: " + experimentCount);
        
        if (experimentCount > 0 && project.getDoseResponseExperiments().get(0) != null) {
            var exp = project.getDoseResponseExperiments().get(0);
            int treatmentCount = exp.getTreatments() != null ? exp.getTreatments().size() : 0;
            int probeCount = exp.getProbeResponses() != null ? exp.getProbeResponses().size() : 0;
            System.out.println("  - Treatments: " + treatmentCount);
            System.out.println("  - Probes: " + probeCount);
        }
        
        // BMD Results
        int bmdResultCount = project.getbMDResult() != null ? project.getbMDResult().size() : 0;
        System.out.println("BMD Results: " + bmdResultCount);
        
        if (bmdResultCount > 0 && project.getbMDResult().get(0) != null) {
            var bmdResult = project.getbMDResult().get(0);
            int probeStatCount = bmdResult.getProbeStatResults() != null ? bmdResult.getProbeStatResults().size() : 0;
            System.out.println("  - Probe Stat Results: " + probeStatCount);
        }
        
        // Category Analysis
        int catAnalysisCount = project.getCategoryAnalysisResults() != null ? project.getCategoryAnalysisResults().size() : 0;
        System.out.println("Category Analysis Results: " + catAnalysisCount);
        
        if (catAnalysisCount > 0 && project.getCategoryAnalysisResults().get(0) != null) {
            var catResults = project.getCategoryAnalysisResults().get(0);
            int catResultCount = catResults.getCategoryAnalsyisResults() != null ? catResults.getCategoryAnalsyisResults().size() : 0;
            System.out.println("  - Individual Categories: " + catResultCount);
        }
        
        // Prefilter Results
        int anovaCount = project.getOneWayANOVAResults() != null ? project.getOneWayANOVAResults().size() : 0;
        int williamsCount = project.getWilliamsTrendResults() != null ? project.getWilliamsTrendResults().size() : 0;
        int oriogenCount = project.getOriogenResults() != null ? project.getOriogenResults().size() : 0;
        System.out.println("Prefilter Results:");
        System.out.println("  - OneWayANOVA: " + anovaCount);
        System.out.println("  - Williams Trend: " + williamsCount);
        System.out.println("  - Oriogen: " + oriogenCount);
    }
}