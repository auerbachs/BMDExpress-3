package com.sciome.bmdexpress2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.category.identifier.CategoryIdentifier;
import com.sciome.bmdexpress2.mvp.model.category.ReferenceGeneProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResult;
import com.sciome.bmdexpress2.mvp.model.probe.Probe;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGene;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;

/**
 * DuckDB Export Service V2 - Normalized Schema
 * 
 * This version follows the TypeScript reference schema from BMD-Express-Plus
 * with proper normalization, entity extraction, and relationship derivation.
 */
public class DuckDBExportServiceV2 {
    
    private Connection connection;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // ID generators
    private long nextId = 1;
    
    // Entity caches for normalization
    private Map<String, Long> probeIds = new HashMap<>();
    private Map<String, Long> geneIds = new HashMap<>(); 
    private Map<String, Long> categoryIds = new HashMap<>();
    private Map<String, Long> chipIds = new HashMap<>();
    
    // Object-to-ID mapping for foreign key relationships
    private Map<BMDResult, Long> bmdResultIdMap = new HashMap<>();
    
    public void exportProject(BMDProject project, String dbPath) throws SQLException {
        try {
            // Load DuckDB JDBC driver
            Class.forName("org.duckdb.DuckDBDriver");
            
            // Create connection to DuckDB file
            connection = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
            
            System.out.println("Creating normalized database schema...");
            createNormalizedSchema();
            
            System.out.println("Exporting project: " + project.getName());
            
            // Export following dependency order
            long datasetId = insertDataset(project);
            
            // Process experiments
            if (project.getDoseResponseExperiments() != null) {
                for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
                    exportExperiment(exp, datasetId);
                }
            }
            
            // Process BMD results
            if (project.getbMDResult() != null) {
                for (BMDResult bmdResult : project.getbMDResult()) {
                    exportBMDResult(bmdResult, datasetId);
                }
            }
            
            // Process category analysis
            if (project.getCategoryAnalysisResults() != null) {
                for (CategoryAnalysisResults catResults : project.getCategoryAnalysisResults()) {
                    exportCategoryAnalysis(catResults, datasetId);
                }
            }
            
            // Process prefilter results
            exportPrefilterResults(project, datasetId);
            
            System.out.println("Export completed successfully!");
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DuckDB JDBC driver not found", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
    
    private void createNormalizedSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            
            // Core organizational tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS groups (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR UNIQUE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS datasets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    created VARCHAR,
                    groupId BIGINT,
                    dataType VARCHAR,
                    groupName VARCHAR
                )
            """);
            
            // Entity tables (normalized)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probes (
                    id VARCHAR PRIMARY KEY
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reference_genes (
                    id VARCHAR PRIMARY KEY,
                    gene_symbol VARCHAR
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chips (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    provider VARCHAR,
                    species VARCHAR,
                    geo_id VARCHAR,
                    geo_name VARCHAR
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryIdentifiers (
                    id VARCHAR PRIMARY KEY,
                    title VARCHAR,
                    modelType VARCHAR,
                    goLevel VARCHAR
                )
            """);
            
            // Experiment data tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doseResponseExperiments (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    chipId BIGINT,
                    logTransformation VARCHAR,
                    columnHeader2 VARCHAR, -- JSON array
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
                    responses VARCHAR -- JSON array
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reference_gene_annotations (
                    id BIGINT PRIMARY KEY,
                    dose_response_experiment_id BIGINT,
                    probe_id VARCHAR,
                    reference_gene_id VARCHAR
                )
            """);
            
            // Prefilter tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilter_result_sets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    data_type VARCHAR,
                    platform VARCHAR,
                    prefilter_type VARCHAR,
                    dose_response_experiment_id BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilter_results (
                    id BIGINT PRIMARY KEY,
                    prefilter_result_set_id BIGINT,
                    probe_id VARCHAR,
                    p_value DOUBLE,
                    adjusted_p_value DOUBLE,
                    best_fold_change DOUBLE,
                    fold_changes VARCHAR, -- JSON array
                    loel_dose DOUBLE,
                    noel_dose DOUBLE,
                    noel_loel_p_values VARCHAR, -- JSON array
                    f_value DOUBLE,
                    df1 INTEGER,
                    df2 INTEGER,
                    williams_p_value DOUBLE,
                    williams_adjusted_p_value DOUBLE,
                    curve_fit_p_value DOUBLE,
                    curve_fit_adjusted_p_value DOUBLE,
                    profile VARCHAR
                )
            """);
            
            // BMD analysis tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bmdResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    doseResponseExperimentId BIGINT,
                    prefilterResultSetId BIGINT,
                    bmdMethod VARCHAR,
                    wAUC DOUBLE,
                    logwAUC DOUBLE,
                    wAUCList VARCHAR, -- JSON array
                    logwAUCList VARCHAR, -- JSON array
                    datasetId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probe_stat_results (
                    id BIGINT PRIMARY KEY,
                    bmd_result_id BIGINT,
                    probe_response_id BIGINT,
                    best_stat_result_id BIGINT,
                    best_poly_stat_result_id BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stat_results (
                    id BIGINT PRIMARY KEY,
                    probe_stat_result_id BIGINT,
                    model_type VARCHAR,
                    fit_p_value DOUBLE,
                    adverse_direction INTEGER,
                    fit_log_likelihood DOUBLE,
                    aic DOUBLE,
                    bmd DOUBLE,
                    bmdl DOUBLE,
                    bmdu DOUBLE,
                    success VARCHAR,
                    r_squared DOUBLE,
                    is_step_function BOOLEAN,
                    is_step_with_bmd_less_lowest BOOLEAN,
                    residuals VARCHAR, -- JSON array
                    curve_parameters VARCHAR, -- JSON array
                    other_parameters VARCHAR, -- JSON array
                    covariances VARCHAR, -- JSON array
                    zscore DOUBLE,
                    bmr_counts_to_top DOUBLE,
                    fold_change_to_top DOUBLE,
                    bmd_low_dose_ratio DOUBLE,
                    bmd_high_dose_ratio DOUBLE,
                    bmd_response_low_dose_response_ratio DOUBLE,
                    bmd_response_high_dose_response_ratio DOUBLE,
                    k_flag INTEGER,
                    option_value INTEGER,
                    degree INTEGER,
                    vertext VARCHAR,
                    -- Model-specific fields
                    bmr DOUBLE, -- GCurveP
                    weighted_averages VARCHAR, -- JSON array, GCurveP
                    weighted_std_deviations VARCHAR, -- JSON array, GCurveP
                    adjusted_control_dose_value DOUBLE, -- GCurveP
                    model_weights VARCHAR -- JSON array, ModelAveraging
                )
            """);
            
            // Category analysis tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResultsSets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    bmdResultId BIGINT,
                    datasetId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResults (
                    id BIGINT PRIMARY KEY,
                    categoryAnalysisResultsId BIGINT,
                    categoryIdentifierId VARCHAR,
                    modelType VARCHAR,
                    geneAllCount INTEGER,
                    geneAllCountFromExperiment INTEGER,
                    geneCountSignificantANOVA INTEGER,
                    percentage DOUBLE,
                    genesWithBMDLessEqualHighDose INTEGER,
                    genesWithBMDpValueGreaterEqualValue INTEGER,
                    genesWithBMDRsquaredValueGreaterEqualValue INTEGER,
                    genesWithBMDBMDLRatioBelowValue INTEGER,
                    genesWithBMDUBMDLRatioBelowValue INTEGER,
                    genesWithBMDUBMDRatioBelowValue INTEGER,
                    genesWithNFoldBelowLowPositiveDoseValue INTEGER,
                    genesWithFoldChangeAboveValue INTEGER,
                    genesWithPrefilterPValueAboveValue INTEGER,
                    genesWithPrefilterAdjustedPValueAboveValue INTEGER,
                    genesNotStepFunction INTEGER,
                    genesNotStepFunctionWithBMDLower INTEGER,
                    genesNotAdverseDirection INTEGER,
                    genesThatPassedAllFilters INTEGER,
                    fishersA INTEGER,
                    fishersB INTEGER,
                    fishersC INTEGER,
                    fishersD INTEGER,
                    fishersExactLeftPValue DOUBLE,
                    fishersExactRightPValue DOUBLE,
                    fishersExactTwoTailPValue DOUBLE,
                    genesWithConflictingProbeSets VARCHAR,
                    bmdMean DOUBLE,
                    bmdMedian DOUBLE,
                    bmdMinimum DOUBLE,
                    bmdSD DOUBLE,
                    bmdWMean DOUBLE,
                    bmdWSD DOUBLE,
                    bmdlMean DOUBLE,
                    bmdlMedian DOUBLE,
                    bmdlMinimum DOUBLE,
                    bmdlSD DOUBLE,
                    bmdlWMean DOUBLE,
                    bmdlWSD DOUBLE,
                    bmduMean DOUBLE,
                    bmduMedian DOUBLE,
                    bmduMinimum DOUBLE,
                    bmduSD DOUBLE,
                    bmduWMean DOUBLE,
                    bmduWSD DOUBLE,
                    overallDirection INTEGER,
                    meanFoldChange DOUBLE,
                    bmdFifthPercentile DOUBLE,
                    bmdlFifthPercentile DOUBLE,
                    bmduFifthPercentile DOUBLE,
                    bmdTenthPercentile DOUBLE,
                    bmdlTenthPercentile DOUBLE,
                    bmduTenthPercentile DOUBLE,
                    -- The fields you were looking for
                    fifthPercentileIndex DOUBLE,
                    bmdFifthPercentileTotalGenes DOUBLE,
                    tenthPercentileIndex DOUBLE,
                    bmdTenthPercentileTotalGenes DOUBLE,
                    bmdlFifthPercentileTotalGenes DOUBLE,
                    bmdlTenthPercentileTotalGenes DOUBLE,
                    bmduFifthPercentileTotalGenes DOUBLE,
                    bmduTenthPercentileTotalGenes DOUBLE,
                    genesUpBMDMean DOUBLE,
                    genesUpBMDMedian DOUBLE,
                    genesUpBMDSD DOUBLE,
                    genesUpBMDLMean DOUBLE,
                    genesUpBMDLMedian DOUBLE,
                    genesUpBMDLSD DOUBLE,
                    genesUpBMDUMean DOUBLE,
                    genesUpBMDUMedian DOUBLE,
                    genesUpBMDUSD DOUBLE,
                    genesDownBMDMean DOUBLE,
                    genesDownBMDMedian DOUBLE,
                    genesDownBMDSD DOUBLE,
                    genesDownBMDLMean DOUBLE,
                    genesDownBMDLMedian DOUBLE,
                    genesDownBMDLSD DOUBLE,
                    genesDownBMDUMean DOUBLE,
                    genesDownBMDUMedian DOUBLE,
                    genesDownBMDUSD DOUBLE,
                    statResultCounts VARCHAR, -- JSON string
                    ivive VARCHAR, -- JSON string
                    totalFoldChange DOUBLE,
                    medianFoldChange DOUBLE,
                    maxFoldChange DOUBLE,
                    minFoldChange DOUBLE,
                    stdDevFoldChange DOUBLE,
                    bmdLower95 DOUBLE,
                    bmdUpper95 DOUBLE,
                    bmdlLower95 DOUBLE,
                    bmdlUpper95 DOUBLE,
                    bmduLower95 DOUBLE,
                    bmduUpper95 DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reference_gene_probe_stat_results (
                    id BIGINT PRIMARY KEY,
                    category_analysis_result_id BIGINT,
                    reference_gene_id VARCHAR,
                    adverse_direction VARCHAR,
                    conflict_min_correlation DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ref_gene_probe_stat_probe_stat_join (
                    id BIGINT PRIMARY KEY,
                    ref_gene_probe_stat_result_id BIGINT,
                    probe_stat_result_id BIGINT
                )
            """);
            
            // Specific prefilter result tables (from TypeScript schema)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS oneWayANOVAResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    doseResponseExperimentId BIGINT,
                    oneWayANOVAResults VARCHAR, -- JSON array of OneWayANOVAResult objects
                    datasetId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS williamsTrendResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    doseResponseExperimentId BIGINT,
                    williamsTrendResults VARCHAR, -- JSON array of WilliamsTrendResult objects
                    datasetId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS curveFitPrefilterResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    doseResponseExperimentId BIGINT,
                    curveFitPrefilterResults VARCHAR, -- JSON array of CurveFitPrefilterResult objects
                    datasetId BIGINT
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS oriogenResults (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    doseResponseExperimentId BIGINT,
                    oriogenResults VARCHAR, -- JSON array of OriogenResult objects
                    datasetId BIGINT
                )
            """);
            
            // Individual prefilter result tables for detailed analysis
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS oneWayANOVAResult (
                    id BIGINT PRIMARY KEY,
                    oneWayANOVAResultsId BIGINT,
                    probeId VARCHAR,
                    pValue DOUBLE,
                    adjustedPValue DOUBLE,
                    fValue DOUBLE,
                    df1 INTEGER,
                    df2 INTEGER,
                    foldChanges VARCHAR, -- JSON array
                    bestFoldChange DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS williamsTrendResult (
                    id BIGINT PRIMARY KEY,
                    williamsTrendResultsId BIGINT,
                    probeId VARCHAR,
                    pValue DOUBLE,
                    adjustedPValue DOUBLE,
                    foldChanges VARCHAR, -- JSON array
                    bestFoldChange DOUBLE,
                    loelDose DOUBLE,
                    noelDose DOUBLE,
                    noelLoelPValues VARCHAR, -- JSON array
                    tStatistic DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS curveFitPrefilterResult (
                    id BIGINT PRIMARY KEY,
                    curveFitPrefilterResultsId BIGINT,
                    probeId VARCHAR,
                    pValue DOUBLE,
                    adjustedPValue DOUBLE,
                    foldChanges VARCHAR, -- JSON array
                    bestFoldChange DOUBLE,
                    bestModel VARCHAR,
                    fitPValue DOUBLE,
                    fitLogLikelihood DOUBLE,
                    aic DOUBLE
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS oriogenResult (
                    id BIGINT PRIMARY KEY,
                    oriogenResultsId BIGINT,
                    probeId VARCHAR,
                    pValue DOUBLE,
                    adjustedPValue DOUBLE,
                    foldChanges VARCHAR, -- JSON array
                    bestFoldChange DOUBLE,
                    testDirection INTEGER,
                    testStatistic DOUBLE
                )
            """);
            
            // Analysis info table for storing analysis metadata
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS analysisInfo (
                    id BIGINT PRIMARY KEY,
                    resultSetId BIGINT,
                    resultSetType VARCHAR, -- 'BMDResult', 'PrefilterResult', 'CategoryAnalysisResults', etc.
                    analysisName VARCHAR,
                    analysisType VARCHAR,
                    notes VARCHAR,
                    createdDate BIGINT,
                    parameters VARCHAR -- JSON object of analysis parameters
                )
            """);
        }
    }
    
    private long insertDataset(BMDProject project) throws SQLException {
        // Create a default group first
        long groupId = getNextId();
        insertGroup(groupId, "Default");
        
        long datasetId = getNextId();
        String sql = "INSERT INTO datasets (id, name, created, groupId, dataType, groupName) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, datasetId);
            stmt.setString(2, project.getName());
            stmt.setString(3, java.time.Instant.now().toString());
            stmt.setLong(4, groupId);
            stmt.setString(5, "genomic"); // Inferred metadata
            stmt.setString(6, "Default");
            stmt.executeUpdate();
        }
        
        return datasetId;
    }
    
    private void insertGroup(long groupId, String name) throws SQLException {
        String sql = "INSERT INTO groups (id, name) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, groupId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }
    
    private void exportExperiment(DoseResponseExperiment exp, long datasetId) throws SQLException {
        // Insert experiment with metadata inference
        long expId = getNextId();
        String sql = "INSERT INTO doseResponseExperiments (id, name, sex, organ, species, dataType, platform, chipId, logTransformation, columnHeader2, chipCreationDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, expId);
            stmt.setString(2, exp.getName());
            
            // Metadata inference (simplified - could be more sophisticated)
            stmt.setString(3, inferSex(exp.getName()));
            stmt.setString(4, inferOrgan(exp.getName()));
            stmt.setString(5, inferSpecies(exp.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, inferPlatform(exp.getChip()));
            
            // Handle chip
            Long chipId = null;
            if (exp.getChip() != null) {
                chipId = insertChip(exp.getChip());
            }
            if (chipId != null) stmt.setLong(8, chipId); else stmt.setNull(8, java.sql.Types.BIGINT);
            
            stmt.setString(9, exp.getLogTransformation() != null ? exp.getLogTransformation().toString() : null);
            stmt.setString(10, exp.getColumnHeader2() != null ? objectMapper.writeValueAsString(exp.getColumnHeader2()) : null);
            stmt.setNull(11, java.sql.Types.BIGINT); // chipCreationDate
            
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new SQLException("Failed to export experiment", e);
        }
        
        // Insert normalized entities and relationships
        insertTreatments(exp.getTreatments(), expId);
        insertProbeResponses(exp.getProbeResponses(), expId);
        insertReferenceGeneAnnotations(exp.getReferenceGeneAnnotations(), expId);
        insertDoseGroups(exp, expId); // Derived data
    }
    
    // Metadata inference methods (simplified)
    private String inferSex(String expName) {
        if (expName != null) {
            String lower = expName.toLowerCase();
            if (lower.contains("male") && !lower.contains("female")) return "M";
            if (lower.contains("female")) return "F";
        }
        return "U"; // Unknown
    }
    
    private String inferOrgan(String expName) {
        if (expName != null) {
            String lower = expName.toLowerCase();
            if (lower.contains("liver")) return "liver";
            if (lower.contains("kidney")) return "kidney";
            if (lower.contains("lung")) return "lung";
        }
        return "U"; // Unknown
    }
    
    private String inferSpecies(String expName) {
        if (expName != null) {
            String lower = expName.toLowerCase();
            if (lower.contains("mouse")) return "mouse";
            if (lower.contains("rat")) return "rat";
            if (lower.contains("human")) return "human";
        }
        return "U"; // Unknown
    }
    
    private String inferPlatform(com.sciome.bmdexpress2.mvp.model.chip.ChipInfo chip) {
        if (chip != null && chip.getProvider() != null) {
            return chip.getProvider();
        }
        return "U"; // Unknown
    }
    
    // Additional helper methods would be implemented here...
    // This is a foundational structure showing the normalized approach
    
    private long getNextId() {
        return nextId++;
    }
    
    // Entity normalization methods
    private Long insertChip(com.sciome.bmdexpress2.mvp.model.chip.ChipInfo chip) throws SQLException {
        if (chip == null) return null;
        
        // Check cache first
        String chipKey = chip.getName() + "_" + chip.getProvider();
        if (chipIds.containsKey(chipKey)) {
            return chipIds.get(chipKey);
        }
        
        long chipId = getNextId();
        String sql = "INSERT INTO chips (id, name, provider, species, geo_id, geo_name) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, chipId);
            stmt.setString(2, chip.getName());
            stmt.setString(3, chip.getProvider());
            stmt.setString(4, chip.getSpecies());
            stmt.setString(5, chip.getGeoID());
            stmt.setString(6, chip.getGeoName());
            stmt.executeUpdate();
        }
        
        chipIds.put(chipKey, chipId);
        return chipId;
    }
    
    private void insertTreatments(List<Treatment> treatments, long expId) throws SQLException {
        if (treatments == null) return;
        
        String sql = "INSERT INTO treatments (id, doseResponseExperimentId, name, dose) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Treatment treatment : treatments) {
                stmt.setLong(1, getNextId());
                stmt.setLong(2, expId);
                stmt.setString(3, treatment.getName());
                stmt.setDouble(4, treatment.getDose());
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertProbeResponses(List<ProbeResponse> probeResponses, long expId) throws SQLException {
        if (probeResponses == null) return;
        
        // First, normalize probes
        Set<String> uniqueProbes = new HashSet<>();
        for (ProbeResponse pr : probeResponses) {
            uniqueProbes.add(pr.getProbe().getId());
        }
        insertProbes(uniqueProbes);
        
        // Insert probe responses with JSON array storage
        String sql = "INSERT INTO probeResponses (id, doseResponseExperimentId, probeId, responses) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ProbeResponse pr : probeResponses) {
                stmt.setLong(1, getNextId());
                stmt.setLong(2, expId);
                stmt.setString(3, pr.getProbe().getId());
                // Convert response array to JSON
                List<Float> responses = pr.getResponses();
                if (responses != null) {
                    stmt.setString(4, objectMapper.writeValueAsString(responses));
                } else {
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                }
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new SQLException("Failed to insert probe responses", e);
        }
    }
    
    private void insertProbes(Set<String> probeIds) throws SQLException {
        String sql = "INSERT INTO probes (id) VALUES (?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String probeId : probeIds) {
                stmt.setString(1, probeId);
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertReferenceGeneAnnotations(List<ReferenceGeneAnnotation> annotations, long expId) throws SQLException {
        if (annotations == null) return;
        
        // First normalize reference genes
        Set<ReferenceGene> uniqueGenes = new HashSet<>();
        for (ReferenceGeneAnnotation annotation : annotations) {
            if (annotation.getReferenceGenes() != null) {
                uniqueGenes.addAll(annotation.getReferenceGenes());
            }
        }
        insertReferenceGenes(uniqueGenes);
        
        // Insert flattened annotations (probe -> genes relationship)
        String sql = "INSERT INTO reference_gene_annotations (id, dose_response_experiment_id, probe_id, reference_gene_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ReferenceGeneAnnotation annotation : annotations) {
                // Simplified - just use the probe ID from the annotation
                String probeId = annotation.getProbe() != null ? annotation.getProbe().getId() : null;
                
                if (annotation.getReferenceGenes() != null && probeId != null) {
                    for (ReferenceGene gene : annotation.getReferenceGenes()) {
                        stmt.setLong(1, getNextId());
                        stmt.setLong(2, expId);
                        stmt.setString(3, probeId);
                        stmt.setString(4, gene.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    private void insertReferenceGenes(Set<ReferenceGene> genes) throws SQLException {
        String sql = "INSERT INTO reference_genes (id, gene_symbol) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ReferenceGene gene : genes) {
                stmt.setString(1, gene.getId());
                stmt.setString(2, gene.getGeneSymbol());
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertDoseGroups(DoseResponseExperiment exp, long expId) throws SQLException {
        // Derive dose groups by aggregating probe response data
        if (exp.getTreatments() == null || exp.getProbeResponses() == null) return;
        
        Map<Double, List<Float>> doseResponseMap = new HashMap<>();
        
        // Group responses by dose
        for (int i = 0; i < exp.getTreatments().size(); i++) {
            double dose = exp.getTreatments().get(i).getDose();
            
            List<Float> responsesForDose = new ArrayList<>();
            for (ProbeResponse pr : exp.getProbeResponses()) {
                List<Float> responses = pr.getResponses();
                if (responses != null && i < responses.size()) {
                    responsesForDose.add(responses.get(i));
                }
            }
            
            doseResponseMap.put(dose, responsesForDose);
        }
        
        // Insert aggregated dose groups
        String sql = "INSERT INTO doseGroups (id, doseResponseExperimentId, dose, n, responseMean) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Map.Entry<Double, List<Float>> entry : doseResponseMap.entrySet()) {
                double dose = entry.getKey();
                List<Float> responses = entry.getValue();
                
                if (!responses.isEmpty()) {
                    int count = responses.size();
                    double mean = responses.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                    
                    stmt.setLong(1, getNextId());
                    stmt.setLong(2, expId);
                    stmt.setDouble(3, dose);
                    stmt.setInt(4, count);
                    stmt.setDouble(5, mean);
                    stmt.executeUpdate();
                }
            }
        }
    }
    
    private void exportBMDResult(BMDResult bmdResult, long datasetId) throws SQLException {
        // Insert BMD result set
        long bmdResultId = getNextId();
        
        // Track the mapping for foreign key references
        bmdResultIdMap.put(bmdResult, bmdResultId);
        String sql = "INSERT INTO bmdResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, prefilterResultSetId, bmdMethod, wAUC, logwAUC, wAUCList, logwAUCList, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bmdResultId);
            stmt.setString(2, bmdResult.getName());
            
            // Metadata inference from BMD result
            stmt.setString(3, inferSex(bmdResult.getName()));
            stmt.setString(4, inferOrgan(bmdResult.getName()));
            stmt.setString(5, inferSpecies(bmdResult.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U"); // Platform unknown for BMD results
            
            // Link to experiment if available (simplified for now)
            stmt.setNull(8, java.sql.Types.BIGINT);
            
            stmt.setNull(9, java.sql.Types.BIGINT); // prefilter link
            stmt.setString(10, "ORIGINAL"); // Default BMD method
            stmt.setNull(11, java.sql.Types.DOUBLE); // wAUC
            stmt.setNull(12, java.sql.Types.DOUBLE); // logwAUC
            stmt.setNull(13, java.sql.Types.VARCHAR); // wAUC list
            stmt.setNull(14, java.sql.Types.VARCHAR); // logwAUC list
            stmt.setLong(15, datasetId);
            
            stmt.executeUpdate();
        }
        
        // Export probe stat results and individual model results
        if (bmdResult.getProbeStatResults() != null) {
            exportProbeStatResults(bmdResult.getProbeStatResults(), bmdResultId);
        }
    }
    
    private void exportProbeStatResults(List<ProbeStatResult> probeStatResults, long bmdResultId) throws SQLException {
        for (ProbeStatResult psr : probeStatResults) {
            long psrId = getNextId();
            
            // Find best stat result IDs
            Long bestStatId = null;
            Long bestPolyStatId = null;
            
            if (psr.getStatResults() != null) {
                // Insert all individual stat results first
                Map<StatResult, Long> statResultIds = new HashMap<>();
                for (StatResult sr : psr.getStatResults()) {
                    long statId = insertStatResult(sr, psrId);
                    statResultIds.put(sr, statId);
                }
                
                // Determine best results (simplified logic)
                StatResult best = psr.getBestStatResult();
                if (best != null && statResultIds.containsKey(best)) {
                    bestStatId = statResultIds.get(best);
                }
                
                StatResult bestPoly = psr.getBestPolyStatResult();
                if (bestPoly != null && statResultIds.containsKey(bestPoly)) {
                    bestPolyStatId = statResultIds.get(bestPoly);
                }
            }
            
            // Insert probe stat result
            String sql = "INSERT INTO probe_stat_results (id, bmd_result_id, probe_response_id, best_stat_result_id, best_poly_stat_result_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, psrId);
                stmt.setLong(2, bmdResultId);
                stmt.setNull(3, java.sql.Types.BIGINT); // probe_response_id - would need lookup
                if (bestStatId != null) stmt.setLong(4, bestStatId); else stmt.setNull(4, java.sql.Types.BIGINT);
                if (bestPolyStatId != null) stmt.setLong(5, bestPolyStatId); else stmt.setNull(5, java.sql.Types.BIGINT);
                stmt.executeUpdate();
            }
        }
    }
    
    private long insertStatResult(StatResult sr, long probeStatResultId) throws SQLException {
        long statId = getNextId();
        String sql = """
            INSERT INTO stat_results (
                id, probe_stat_result_id, model_type, fit_p_value, adverse_direction, 
                fit_log_likelihood, aic, bmd, bmdl, bmdu, success, r_squared, 
                is_step_function, is_step_with_bmd_less_lowest, residuals, curve_parameters
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, statId);
            stmt.setLong(2, probeStatResultId);
            stmt.setString(3, sr.getClass().getSimpleName()); // Model type from class name
            stmt.setDouble(4, sr.getFitPValue());
            stmt.setInt(5, sr.getAdverseDirection());
            stmt.setDouble(6, sr.getFitLogLikelihood());
            stmt.setDouble(7, sr.getAIC());
            stmt.setDouble(8, sr.getBMD());
            stmt.setDouble(9, sr.getBMDL());
            stmt.setDouble(10, sr.getBMDU());
            stmt.setString(11, sr.getSuccess());
            stmt.setDouble(12, sr.getrSquared());
            stmt.setBoolean(13, sr.getIsStepFunction());
            stmt.setBoolean(14, sr.isStepWithBMDLessLowest());
            
            // Store arrays as JSON
            try {
                if (sr.getResiduals() != null) {
                    stmt.setString(15, objectMapper.writeValueAsString(sr.getResiduals()));
                } else {
                    stmt.setNull(15, java.sql.Types.VARCHAR);
                }
                
                if (sr.getCurveParameters() != null) {
                    stmt.setString(16, objectMapper.writeValueAsString(sr.getCurveParameters()));
                } else {
                    stmt.setNull(16, java.sql.Types.VARCHAR);
                }
            } catch (Exception e) {
                stmt.setNull(15, java.sql.Types.VARCHAR);
                stmt.setNull(16, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
        }
        
        return statId;
    }
    
    private void exportCategoryAnalysis(CategoryAnalysisResults catResults, long datasetId) throws SQLException {
        // Insert category analysis results set
        long catResultsSetId = getNextId();
        String sql = "INSERT INTO categoryAnalysisResultsSets (id, name, sex, organ, species, dataType, platform, bmdResultId, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, catResultsSetId);
            stmt.setString(2, catResults.getName());
            stmt.setString(3, inferSex(catResults.getName()));
            stmt.setString(4, inferOrgan(catResults.getName()));
            stmt.setString(5, inferSpecies(catResults.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U");
            // Look up BMD result ID if it exists
            BMDResult linkedBmdResult = catResults.getBmdResult();
            if (linkedBmdResult != null && bmdResultIdMap.containsKey(linkedBmdResult)) {
                stmt.setLong(8, bmdResultIdMap.get(linkedBmdResult));
            } else {
                stmt.setNull(8, java.sql.Types.BIGINT);
            }
            stmt.setLong(9, datasetId);
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
        // First normalize category identifier
        CategoryIdentifier catId = result.getCategoryIdentifier();
        if (catId != null) {
            insertCategoryIdentifier(catId);
        }
        
        long catResultId = getNextId();
        String sql = """
            INSERT INTO categoryAnalysisResults (
                id, categoryAnalysisResultsId, categoryIdentifierId, modelType,
                geneAllCount, percentage, genesThatPassedAllFilters,
                fishersExactRightPValue, bmdMean, bmdMedian, bmdFifthPercentileTotalGenes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, catResultId);
            stmt.setLong(2, catResultsSetId);
            stmt.setString(3, catId != null ? catId.getId() : null);
            stmt.setString(4, result.getClass().getSimpleName());
            
            // Key fields that were requested
            stmt.setObject(5, result.getGeneAllCount());
            stmt.setObject(6, result.getPercentage());
            stmt.setObject(7, result.getGenesThatPassedAllFilters());
            stmt.setObject(8, result.getFishersExactRightPValue());
            stmt.setObject(9, result.getBmdMean());
            stmt.setObject(10, result.getBmdMedian());
            
            // The specific field that was missing in the original query!
            stmt.setObject(11, result.getBmdFifthPercentileTotalGenes());
            
            stmt.executeUpdate();
        }
        
        // Export reference gene probe stat results (the critical missing relationship)
        if (result.getReferenceGeneProbeStatResults() != null) {
            exportReferenceGeneProbeStatResults(result.getReferenceGeneProbeStatResults(), catResultId);
        }
    }
    
    private void insertCategoryIdentifier(CategoryIdentifier catId) throws SQLException {
        String sql = "INSERT INTO categoryIdentifiers (id, title, modelType, goLevel) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, catId.getId());
            stmt.setString(2, catId.getTitle());
            stmt.setString(3, catId.getClass().getSimpleName());
            stmt.setNull(4, java.sql.Types.VARCHAR); // GoLevel - simplified
            stmt.executeUpdate();
        }
    }
    
    private void exportReferenceGeneProbeStatResults(List<ReferenceGeneProbeStatResult> refGeneResults, long catResultId) throws SQLException {
        for (ReferenceGeneProbeStatResult refResult : refGeneResults) {
            long refResultId = getNextId();
            
            // Insert reference gene probe stat result
            String sql = "INSERT INTO reference_gene_probe_stat_results (id, category_analysis_result_id, reference_gene_id, adverse_direction, conflict_min_correlation) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, refResultId);
                stmt.setLong(2, catResultId);
                stmt.setString(3, refResult.getReferenceGene().getId());
                stmt.setString(4, refResult.getAdverseDirection() != null ? refResult.getAdverseDirection().toString() : null);
                stmt.setObject(5, refResult.getConflictMinCorrelation());
                stmt.executeUpdate();
            }
            
            // Insert many-to-many relationship (the critical join table!)
            if (refResult.getProbeStatResults() != null) {
                String joinSql = "INSERT INTO ref_gene_probe_stat_probe_stat_join (id, ref_gene_probe_stat_result_id, probe_stat_result_id) VALUES (?, ?, ?)";
                try (PreparedStatement joinStmt = connection.prepareStatement(joinSql)) {
                    for (ProbeStatResult psr : refResult.getProbeStatResults()) {
                        joinStmt.setLong(1, getNextId());
                        joinStmt.setLong(2, refResultId);
                        // Would need to look up PSR ID from previous insertions
                        joinStmt.setNull(3, java.sql.Types.BIGINT); // Simplified for now
                        joinStmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    private void exportPrefilterResults(BMDProject project, long datasetId) throws SQLException {
        // Export OneWayANOVA results to specific table
        if (project.getOneWayANOVAResults() != null) {
            for (OneWayANOVAResults anovaResults : project.getOneWayANOVAResults()) {
                exportOneWayANOVAResults(anovaResults, datasetId);
                // Also export to generic prefilter table for backward compatibility
                exportPrefilterResultSet(anovaResults, "OneWayANOVAResults", datasetId);
            }
        }
        
        // Export Williams trend results to specific table
        if (project.getWilliamsTrendResults() != null) {
            for (WilliamsTrendResults williamsResults : project.getWilliamsTrendResults()) {
                exportWilliamsTrendResults(williamsResults, datasetId);
                // Also export to generic prefilter table for backward compatibility
                exportPrefilterResultSet(williamsResults, "WilliamsTrendResults", datasetId);
            }
        }
        
        // Export CurveFit prefilter results to specific table
        if (project.getCurveFitPrefilterResults() != null) {
            for (CurveFitPrefilterResults curveFitResults : project.getCurveFitPrefilterResults()) {
                exportCurveFitPrefilterResults(curveFitResults, datasetId);
                exportPrefilterResultSet(curveFitResults, "CurveFitPrefilterResults", datasetId);
            }
        }
        
        // Export Oriogen results to specific table
        if (project.getOriogenResults() != null) {
            for (OriogenResults oriogenResults : project.getOriogenResults()) {
                exportOriogenResults(oriogenResults, datasetId);
                // Also export to generic prefilter table for backward compatibility
                exportPrefilterResultSet(oriogenResults, "OriogenResults", datasetId);
            }
        }
    }
    
    private void exportPrefilterResultSet(Object prefilterResults, String prefilterType, long datasetId) throws SQLException {
        // This would need specific implementation for each prefilter type
        // Showing structure for OneWayANOVAResults as example
        if (prefilterResults instanceof OneWayANOVAResults) {
            OneWayANOVAResults anovaResults = (OneWayANOVAResults) prefilterResults;
            
            long prefilterSetId = getNextId();
            String sql = "INSERT INTO prefilter_result_sets (id, name, sex, organ, species, data_type, platform, prefilter_type, dose_response_experiment_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, prefilterSetId);
                stmt.setString(2, anovaResults.getName());
                stmt.setString(3, inferSex(anovaResults.getName()));
                stmt.setString(4, inferOrgan(anovaResults.getName()));
                stmt.setString(5, inferSpecies(anovaResults.getName()));
                stmt.setString(6, "genomic");
                stmt.setString(7, "U");
                stmt.setString(8, prefilterType);
                stmt.setNull(9, java.sql.Types.BIGINT); // experiment link
                stmt.executeUpdate();
            }
            
            // Export individual prefilter results
            if (anovaResults.getOneWayANOVAResults() != null) {
                for (PrefilterResult result : anovaResults.getOneWayANOVAResults()) {
                    exportPrefilterResult(result, prefilterSetId);
                }
            }
        }
    }
    
    private void exportPrefilterResult(PrefilterResult result, long prefilterSetId) throws SQLException {
        String sql = """
            INSERT INTO prefilter_results (
                id, prefilter_result_set_id, probe_id, p_value, adjusted_p_value, 
                best_fold_change, fold_changes, loel_dose, noel_dose, noel_loel_p_values,
                f_value, df1, df2
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, getNextId());
            stmt.setLong(2, prefilterSetId);
            stmt.setString(3, result.getProbeResponse().getProbe().getId());
            stmt.setDouble(4, result.getpValue());
            stmt.setDouble(5, result.getAdjustedPValue());
            stmt.setObject(6, result.getBestFoldChange());
            
            // Store arrays as JSON
            try {
                if (result.getFoldChanges() != null) {
                    stmt.setString(7, objectMapper.writeValueAsString(result.getFoldChanges()));
                } else {
                    stmt.setNull(7, java.sql.Types.VARCHAR);
                }
                
                // NoelLoelPValues - simplified since not all prefilter types have this
                stmt.setNull(10, java.sql.Types.VARCHAR);
            } catch (Exception e) {
                stmt.setNull(7, java.sql.Types.VARCHAR);
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }
            
            stmt.setObject(8, result.getLoelDose());
            stmt.setObject(9, result.getNoelDose());
            
            // ANOVA-specific fields
            if (result instanceof com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) {
                com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult anovaResult = 
                    (com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) result;
                stmt.setDouble(11, anovaResult.getfValue());
                stmt.setInt(12, anovaResult.getDegreesOfFreedomOne());
                stmt.setInt(13, anovaResult.getDegreesOfFreedomTwo());
            } else {
                stmt.setNull(11, java.sql.Types.DOUBLE);
                stmt.setNull(12, java.sql.Types.INTEGER);
                stmt.setNull(13, java.sql.Types.INTEGER);
            }
            
            stmt.executeUpdate();
        }
    }
    
    // Specific export methods for each prefilter type (following TypeScript schema)
    
    private void exportOneWayANOVAResults(OneWayANOVAResults anovaResults, long datasetId) throws SQLException {
        long anovaResultsId = getNextId();
        String sql = "INSERT INTO oneWayANOVAResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, oneWayANOVAResults, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, anovaResultsId);
            stmt.setString(2, anovaResults.getName());
            stmt.setString(3, inferSex(anovaResults.getName()));
            stmt.setString(4, inferOrgan(anovaResults.getName()));
            stmt.setString(5, inferSpecies(anovaResults.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U");
            stmt.setNull(8, java.sql.Types.BIGINT);
            
            // Store individual results as JSON array
            try {
                if (anovaResults.getOneWayANOVAResults() != null) {
                    stmt.setString(9, objectMapper.writeValueAsString(anovaResults.getOneWayANOVAResults()));
                } else {
                    stmt.setString(9, "[]");
                }
            } catch (Exception e) {
                stmt.setString(9, "[]");
            }
            
            stmt.setLong(10, datasetId);
            stmt.executeUpdate();
        }
        
        // Export individual ANOVA results to normalized table
        if (anovaResults.getOneWayANOVAResults() != null) {
            for (com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult result : anovaResults.getOneWayANOVAResults()) {
                exportOneWayANOVAResult(result, anovaResultsId);
            }
        }
    }
    
    private void exportOneWayANOVAResult(com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult result, long anovaResultsId) throws SQLException {
        long resultId = getNextId();
        String sql = "INSERT INTO oneWayANOVAResult (id, oneWayANOVAResultsId, probeId, pValue, adjustedPValue, fValue, df1, df2, foldChanges, bestFoldChange) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resultId);
            stmt.setLong(2, anovaResultsId);
            stmt.setString(3, result.getProbeResponse().getProbe().getId());
            stmt.setObject(4, result.getpValue());
            stmt.setObject(5, result.getAdjustedPValue());
            stmt.setDouble(6, result.getfValue());
            stmt.setInt(7, result.getDegreesOfFreedomOne());
            stmt.setInt(8, result.getDegreesOfFreedomTwo());
            
            try {
                if (result.getFoldChanges() != null) {
                    stmt.setString(9, objectMapper.writeValueAsString(result.getFoldChanges()));
                } else {
                    stmt.setNull(9, java.sql.Types.VARCHAR);
                }
            } catch (Exception e) {
                stmt.setNull(9, java.sql.Types.VARCHAR);
            }
            
            stmt.setObject(10, result.getBestFoldChange());
            stmt.executeUpdate();
        }
    }
    
    private void exportWilliamsTrendResults(WilliamsTrendResults williamsResults, long datasetId) throws SQLException {
        long williamsResultsId = getNextId();
        String sql = "INSERT INTO williamsTrendResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, williamsTrendResults, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, williamsResultsId);
            stmt.setString(2, williamsResults.getName());
            stmt.setString(3, inferSex(williamsResults.getName()));
            stmt.setString(4, inferOrgan(williamsResults.getName()));
            stmt.setString(5, inferSpecies(williamsResults.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U");
            stmt.setNull(8, java.sql.Types.BIGINT);
            
            try {
                if (williamsResults.getWilliamsTrendResults() != null) {
                    stmt.setString(9, objectMapper.writeValueAsString(williamsResults.getWilliamsTrendResults()));
                } else {
                    stmt.setString(9, "[]");
                }
            } catch (Exception e) {
                stmt.setString(9, "[]");
            }
            
            stmt.setLong(10, datasetId);
            stmt.executeUpdate();
        }
        
        // Export individual Williams results to normalized table
        if (williamsResults.getWilliamsTrendResults() != null) {
            for (WilliamsTrendResult result : williamsResults.getWilliamsTrendResults()) {
                exportWilliamsTrendResult(result, williamsResultsId);
            }
        }
    }
    
    private void exportWilliamsTrendResult(WilliamsTrendResult result, long williamsResultsId) throws SQLException {
        long resultId = getNextId();
        String sql = "INSERT INTO williamsTrendResult (id, williamsTrendResultsId, probeId, pValue, adjustedPValue, foldChanges, bestFoldChange, loelDose, noelDose, noelLoelPValues, tStatistic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resultId);
            stmt.setLong(2, williamsResultsId);
            stmt.setString(3, result.getProbeResponse().getProbe().getId());
            stmt.setObject(4, result.getpValue());
            stmt.setObject(5, result.getAdjustedPValue());
            
            try {
                if (result.getFoldChanges() != null) {
                    stmt.setString(6, objectMapper.writeValueAsString(result.getFoldChanges()));
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }
                
                if (result.getNoelLoelPValues() != null) {
                    stmt.setString(10, objectMapper.writeValueAsString(result.getNoelLoelPValues()));
                } else {
                    stmt.setNull(10, java.sql.Types.VARCHAR);
                }
            } catch (Exception e) {
                stmt.setNull(6, java.sql.Types.VARCHAR);
                stmt.setNull(10, java.sql.Types.VARCHAR);
            }
            
            stmt.setObject(7, result.getBestFoldChange());
            stmt.setObject(8, result.getLoelDose());
            stmt.setObject(9, result.getNoelDose());
            stmt.setNull(11, java.sql.Types.DOUBLE); // tStatistic not available in WilliamsTrendResult
            stmt.executeUpdate();
        }
    }
    
    private void exportCurveFitPrefilterResults(CurveFitPrefilterResults curveFitResults, long datasetId) throws SQLException {
        long curveFitResultsId = getNextId();
        String sql = "INSERT INTO curveFitPrefilterResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, curveFitPrefilterResults, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, curveFitResultsId);
            stmt.setString(2, curveFitResults.getName());
            stmt.setString(3, inferSex(curveFitResults.getName()));
            stmt.setString(4, inferOrgan(curveFitResults.getName()));
            stmt.setString(5, inferSpecies(curveFitResults.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U");
            stmt.setNull(8, java.sql.Types.BIGINT);
            
            try {
                if (curveFitResults.getCurveFitPrefilterResults() != null) {
                    stmt.setString(9, objectMapper.writeValueAsString(curveFitResults.getCurveFitPrefilterResults()));
                } else {
                    stmt.setString(9, "[]");
                }
            } catch (Exception e) {
                stmt.setString(9, "[]");
            }
            
            stmt.setLong(10, datasetId);
            stmt.executeUpdate();
        }
        
        // Export individual curve fit results to normalized table
        if (curveFitResults.getCurveFitPrefilterResults() != null) {
            for (CurveFitPrefilterResult result : curveFitResults.getCurveFitPrefilterResults()) {
                exportCurveFitPrefilterResult(result, curveFitResultsId);
            }
        }
    }
    
    private void exportCurveFitPrefilterResult(CurveFitPrefilterResult result, long curveFitResultsId) throws SQLException {
        long resultId = getNextId();
        String sql = "INSERT INTO curveFitPrefilterResult (id, curveFitPrefilterResultsId, probeId, pValue, adjustedPValue, foldChanges, bestFoldChange, bestModel, fitPValue, fitLogLikelihood, aic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resultId);
            stmt.setLong(2, curveFitResultsId);
            stmt.setString(3, result.getProbeResponse().getProbe().getId());
            stmt.setObject(4, result.getpValue());
            stmt.setObject(5, result.getAdjustedPValue());
            
            try {
                if (result.getFoldChanges() != null) {
                    stmt.setString(6, objectMapper.writeValueAsString(result.getFoldChanges()));
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }
            } catch (Exception e) {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }
            
            stmt.setObject(7, result.getBestFoldChange());
            stmt.setNull(8, java.sql.Types.VARCHAR); // bestModel not directly available
            stmt.setNull(9, java.sql.Types.DOUBLE); // fitPValue not directly available  
            stmt.setNull(10, java.sql.Types.DOUBLE); // fitLogLikelihood not directly available
            stmt.setNull(11, java.sql.Types.DOUBLE); // aic not directly available
            stmt.executeUpdate();
        }
    }
    
    private void exportOriogenResults(OriogenResults oriogenResults, long datasetId) throws SQLException {
        long oriogenResultsId = getNextId();
        String sql = "INSERT INTO oriogenResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, oriogenResults, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, oriogenResultsId);
            stmt.setString(2, oriogenResults.getName());
            stmt.setString(3, inferSex(oriogenResults.getName()));
            stmt.setString(4, inferOrgan(oriogenResults.getName()));
            stmt.setString(5, inferSpecies(oriogenResults.getName()));
            stmt.setString(6, "genomic");
            stmt.setString(7, "U");
            stmt.setNull(8, java.sql.Types.BIGINT);
            
            try {
                if (oriogenResults.getOriogenResults() != null) {
                    stmt.setString(9, objectMapper.writeValueAsString(oriogenResults.getOriogenResults()));
                } else {
                    stmt.setString(9, "[]");
                }
            } catch (Exception e) {
                stmt.setString(9, "[]");
            }
            
            stmt.setLong(10, datasetId);
            stmt.executeUpdate();
        }
        
        // Export individual Oriogen results to normalized table
        if (oriogenResults.getOriogenResults() != null) {
            for (OriogenResult result : oriogenResults.getOriogenResults()) {
                exportOriogenResult(result, oriogenResultsId);
            }
        }
    }
    
    private void exportOriogenResult(OriogenResult result, long oriogenResultsId) throws SQLException {
        long resultId = getNextId();
        String sql = "INSERT INTO oriogenResult (id, oriogenResultsId, probeId, pValue, adjustedPValue, foldChanges, bestFoldChange, testDirection, testStatistic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, resultId);
            stmt.setLong(2, oriogenResultsId);
            stmt.setString(3, result.getProbeResponse().getProbe().getId());
            stmt.setObject(4, result.getpValue());
            stmt.setObject(5, result.getAdjustedPValue());
            
            try {
                if (result.getFoldChanges() != null) {
                    stmt.setString(6, objectMapper.writeValueAsString(result.getFoldChanges()));
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }
            } catch (Exception e) {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }
            
            stmt.setObject(7, result.getBestFoldChange());
            stmt.setNull(8, java.sql.Types.INTEGER); // testDirection not directly available
            stmt.setNull(9, java.sql.Types.DOUBLE); // testStatistic not directly available
            stmt.executeUpdate();
        }
    }
}