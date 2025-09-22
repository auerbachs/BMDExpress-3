package com.sciome.bmdexpress2.commandline;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.service.DuckDBExportServiceV4;

/**
 * Command-line runner for DuckDB Export Service V4
 *
 * Usage: java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV4 input.bm2 output.duckdb
 *
 * This runner provides the complete V4 export functionality with:
 * - All 22+ tables from the unified webapp schema
 * - WASM compatibility using CHECKPOINT commands
 * - Proper metadata field handling (sex, organ, species)
 * - Complete BMDProject object structure mapping
 */
public class DuckDBExportRunnerV4 {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV4 <input.bm2> <output.duckdb>");
            System.err.println("");
            System.err.println("This is DuckDB Export Service V4 - the complete solution combining:");
            System.err.println("  • Full schema coverage (22+ tables) from V1/V2");
            System.err.println("  • WASM compatibility using CHECKPOINT commands from V3");
            System.err.println("  • Proper metadata field population (sex, organ, species)");
            System.err.println("  • Complete BMDProject object structure mapping");
            System.err.println("");
            System.err.println("Example:");
            System.err.println("  java -cp 'target/classes:lib/*' com.sciome.bmdexpress2.commandline.DuckDBExportRunnerV4 P3MP-Parham.bm2 p3mp_v4_complete.duckdb");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        System.out.println("=== DuckDB Export Service V4 ===");
        System.out.println("Input:  " + inputFile);
        System.out.println("Output: " + outputFile);
        System.out.println("Features: Complete schema + WASM compatibility");
        System.out.println("");

        try {
            // Load BMD project from .bm2 file
            System.out.println("[V4] Loading BMD project from: " + inputFile);
            BMDProject project;
            try (FileInputStream fis = new FileInputStream(inputFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                project = (BMDProject) ois.readObject();
            }

            System.out.println("[V4] Project loaded successfully: " + project.getName());
            System.out.println("[V4] Dose response experiments: " +
                (project.getDoseResponseExperiments() != null ? project.getDoseResponseExperiments().size() : 0));
            System.out.println("[V4] BMD results: " +
                (project.getbMDResult() != null ? project.getbMDResult().size() : 0));
            System.out.println("[V4] Category analysis results: " +
                (project.getCategoryAnalysisResults() != null ? project.getCategoryAnalysisResults().size() : 0));
            System.out.println("");

            // Export to DuckDB
            System.out.println("[V4] Starting export to DuckDB...");
            DuckDBExportServiceV4 exporter = new DuckDBExportServiceV4();
            exporter.exportToFile(project, outputFile);

            System.out.println("");
            System.out.println("=== Export Complete ===");
            System.out.println("✅ Successfully exported to: " + outputFile);
            System.out.println("✅ WASM compatible database created");
            System.out.println("✅ All tables populated with complete schema");
            System.out.println("✅ Metadata fields included for population by webapp");
            System.out.println("");
            System.out.println("You can now:");
            System.out.println("1. Query the database: duckdb " + outputFile);
            System.out.println("2. Upload to the webapp for visualization");
            System.out.println("3. Use with R/Python data analysis tools");

        } catch (Exception e) {
            System.err.println("❌ Export failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}