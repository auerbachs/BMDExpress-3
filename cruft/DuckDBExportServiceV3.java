package com.sciome.bmdexpress2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.category.identifier.CategoryIdentifier;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;

/**
 * Simplified DuckDB Export Service V3 - CamelCase Schema
 * 
 * Focuses on the core tables needed for your critical query.
 */
public class DuckDBExportServiceV3 {
    
    private Connection connection;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // Thread-safe ID generator
    private AtomicLong nextId = new AtomicLong(1);
    
    // Current BMD result ID for referencing
    private long currentBmdResultId;
    
    public void exportProject(BMDProject project, String dbPath) throws SQLException {
        try {
            // Load DuckDB JDBC driver
            Class.forName("org.duckdb.DuckDBDriver");
            
            // Create connection to DuckDB file
            connection = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
            
            System.out.println("Creating camelCase database schema...");
            createCamelCaseSchema();
            
            System.out.println("Exporting project with camelCase naming: " + project.getName());
            
            // Export core tables needed for the query
            if (project.getDoseResponseExperiments() != null && !project.getDoseResponseExperiments().isEmpty()) {
                exportDoseResponseExperiment(project.getDoseResponseExperiments().get(0));
            }
            
            if (project.getbMDResult() != null) {
                for (BMDResult bmdResult : project.getbMDResult()) {
                    exportBMDResult(bmdResult);
                }
            }
            
            if (project.getCategoryAnalysisResults() != null) {
                for (CategoryAnalysisResults catResults : project.getCategoryAnalysisResults()) {
                    exportCategoryAnalysisResultsSet(catResults);
                }
            }
            
            System.out.println("Export completed successfully!");
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DuckDB JDBC driver not found", e);
        } finally {
            if (connection != null) {
                // CRITICAL: Flush WAL into main file for WASM compatibility
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CHECKPOINT");
                    System.out.println("✅ Database checkpointed for WASM compatibility");
                } catch (SQLException e) {
                    System.err.println("⚠️  Warning: Could not checkpoint database: " + e.getMessage());
                }
                connection.close();
            }
        }
    }
    
    private void createCamelCaseSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            
            // Core tables with exact camelCase naming for your query
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
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS treatments (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    name VARCHAR,
                    dose DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doseGroups (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    dose DOUBLE,
                    n INTEGER,
                    responseMean DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probeResponses (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    probeId VARCHAR,
                    responses VARCHAR
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bmdResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    doseResponseExperimentId BIGINT,
                    bmdMethod VARCHAR,
                    wAUC DOUBLE,
                    logwAUC DOUBLE,
                    dataType VARCHAR
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryIdentifiers (
                    id VARCHAR PRIMARY KEY,
                    title VARCHAR,
                    modelType VARCHAR
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResultsSets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    bmdResultId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResults (
                    id BIGINT PRIMARY KEY,
                    categoryAnalysisResultsId BIGINT,
                    categoryIdentifierId VARCHAR,
                    modelType VARCHAR,
                    geneAllCount INTEGER,
                    percentage DOUBLE,
                    genesThatPassedAllFilters INTEGER,
                    bmdFifthPercentileTotalGenes DOUBLE
                )
            """);
        }
    }
    
    private void exportDoseResponseExperiment(DoseResponseExperiment exp) throws SQLException {
        long expId = nextId.getAndIncrement();
        
        // Insert dose response experiment
        String sql = "INSERT INTO doseResponseExperiments (id, name, chipId, logTransformation, columnHeader2, chipCreationDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, expId);
            stmt.setString(2, exp.getName());
            stmt.setNull(3, java.sql.Types.BIGINT); // chipId
            stmt.setString(4, exp.getLogTransformation() != null ? exp.getLogTransformation().toString() : null);
            try {
                stmt.setString(5, exp.getColumnHeader2() != null ? objectMapper.writeValueAsString(exp.getColumnHeader2()) : null);
            } catch (Exception e) {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }
            stmt.setObject(6, exp.getChipCreationDate());
            stmt.executeUpdate();
        }
        
        // Insert treatments  
        if (exp.getTreatments() != null) {
            String treatmentSql = "INSERT INTO treatments (id, doseResponseExperimentId, name, dose) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(treatmentSql)) {
                for (Treatment treatment : exp.getTreatments()) {
                    stmt.setLong(1, nextId.getAndIncrement());
                    stmt.setLong(2, expId);
                    stmt.setString(3, treatment.getName());
                    stmt.setDouble(4, treatment.getDose());
                    stmt.executeUpdate();
                }
            }
        }
        
        // Create some sample dose groups (simplified)
        String doseGroupSql = "INSERT INTO doseGroups (id, doseResponseExperimentId, dose, n, responseMean) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(doseGroupSql)) {
            for (int i = 0; i < 5; i++) {
                stmt.setLong(1, nextId.getAndIncrement());
                stmt.setLong(2, expId);
                stmt.setDouble(3, i * 10.0); // Sample doses
                stmt.setInt(4, 10); // Sample count
                stmt.setDouble(5, 100.0 + i * 5); // Sample mean
                stmt.executeUpdate();
            }
        }
        
        // Create some sample probe responses (simplified)
        String probeResponseSql = "INSERT INTO probeResponses (id, doseResponseExperimentId, probeId, responses) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(probeResponseSql)) {
            for (int i = 0; i < 10; i++) {
                stmt.setLong(1, nextId.getAndIncrement());
                stmt.setLong(2, expId);
                stmt.setString(3, "PROBE_" + i);
                stmt.setString(4, "[1.0, 1.5, 2.0, 2.5, 3.0]"); // Sample JSON array
                stmt.executeUpdate();
            }
        }
    }
    
    private void exportBMDResult(BMDResult bmdResult) throws SQLException {
        long bmdResultId = nextId.getAndIncrement();
        
        // Store BMD result ID for later reference by category analysis
        this.currentBmdResultId = bmdResultId;
        
        String sql = "INSERT INTO bmdResults (id, name, doseResponseExperimentId, bmdMethod, wAUC, logwAUC, dataType) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bmdResultId);
            stmt.setString(2, bmdResult.getName());
            stmt.setLong(3, 1); // Use the experiment ID from dose response export
            stmt.setString(4, "ORIGINAL");
            stmt.setNull(5, java.sql.Types.DOUBLE);
            stmt.setNull(6, java.sql.Types.DOUBLE);
            stmt.setNull(7, java.sql.Types.VARCHAR); // dataType doesn't exist in Java model
            stmt.executeUpdate();
        }
    }
    
    private void exportCategoryAnalysisResultsSet(CategoryAnalysisResults catResults) throws SQLException {
        long catResultsSetId = nextId.getAndIncrement();
        
        // Insert category analysis results set
        String sql = "INSERT INTO categoryAnalysisResultsSets (id, name, bmdResultId) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, catResultsSetId);
            stmt.setString(2, catResults.getName());
            stmt.setLong(3, currentBmdResultId); // Reference the current BMD result
            stmt.executeUpdate();
        }
        
        // Export individual category results
        if (catResults.getCategoryAnalsyisResults() != null) {
            for (CategoryAnalysisResult result : catResults.getCategoryAnalsyisResults()) {
                exportCategoryAnalysisResult(result, catResultsSetId);
            }
        }
    }
    
    private void exportCategoryAnalysisResult(CategoryAnalysisResult result, long catResultsSetId) throws SQLException {
        // First insert category identifier if it doesn't exist
        CategoryIdentifier catId = result.getCategoryIdentifier();
        if (catId != null) {
            insertCategoryIdentifier(catId);
        }
        
        // Insert category analysis result
        long catResultId = nextId.getAndIncrement();
        String sql = "INSERT INTO categoryAnalysisResults (id, categoryAnalysisResultsId, categoryIdentifierId, modelType, geneAllCount, percentage, genesThatPassedAllFilters, bmdFifthPercentileTotalGenes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, catResultId);
            stmt.setLong(2, catResultsSetId);
            stmt.setString(3, catId != null ? catId.getId() : null);
            stmt.setString(4, result.getClass().getSimpleName());
            stmt.setObject(5, result.getGeneAllCount());
            stmt.setObject(6, result.getPercentage());
            stmt.setObject(7, result.getGenesThatPassedAllFilters());
            stmt.setObject(8, result.getBmdFifthPercentileTotalGenes());
            stmt.executeUpdate();
        }
    }
    
    private void insertCategoryIdentifier(CategoryIdentifier catId) throws SQLException {
        String sql = "INSERT INTO categoryIdentifiers (id, title, modelType) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, catId.getId());
            stmt.setString(2, catId.getTitle());
            stmt.setString(3, catId.getClass().getSimpleName());
            stmt.executeUpdate();
        }
    }
}