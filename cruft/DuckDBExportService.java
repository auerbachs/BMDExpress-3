package com.sciome.bmdexpress2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGene;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.mvp.model.probe.Probe;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;

public class DuckDBExportService {
    
    private Connection connection;
    
    public void exportProject(BMDProject project, String dbPath) throws SQLException {
        try {
            // Load DuckDB JDBC driver
            Class.forName("org.duckdb.DuckDBDriver");
            
            // Create connection to DuckDB file
            connection = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
            
            System.out.println("Creating database schema...");
            createSchema();
            
            System.out.println("Exporting project: " + project.getName());
            
            // Insert project
            long projectId = insertProject(project);
            
            // Export dose response experiments
            System.out.println("Exporting " + project.getDoseResponseExperiments().size() + " dose response experiments...");
            for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
                insertDoseResponseExperiment(exp, projectId);
            }
            
            // Export BMD results
            System.out.println("Exporting " + project.getbMDResult().size() + " BMD analysis results...");
            for (BMDResult bmd : project.getbMDResult()) {
                insertBMDResult(bmd, projectId);
            }
            
            // Export category analysis results
            System.out.println("Exporting " + project.getCategoryAnalysisResults().size() + " category analysis results...");
            for (CategoryAnalysisResults cat : project.getCategoryAnalysisResults()) {
                insertCategoryAnalysisResults(cat, projectId);
            }
            
            // Export prefilter results
            System.out.println("Exporting prefilter results...");
            exportPrefilterResults(project, projectId);
            
            System.out.println("Export completed successfully!");
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("DuckDB JDBC driver not found", e);
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
    
    private void createSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Projects table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Analysis info table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS analysis_info (
                    id BIGINT PRIMARY KEY,
                    project_id BIGINT,
                    analysis_type VARCHAR,
                    analysis_name VARCHAR,
                    notes VARCHAR,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Chips table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chips (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    geo_name VARCHAR,
                    provider VARCHAR,
                    species VARCHAR,
                    creation_date BIGINT
                )
            """);
            
            // Dose response experiments
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dose_response_experiments (
                    id BIGINT PRIMARY KEY,
                    project_id BIGINT,
                    name VARCHAR,
                    chip_id BIGINT,
                    log_transformation VARCHAR,
                    analysis_info_id BIGINT
                )
            """);
            
            // Treatments
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS treatments (
                    id BIGINT PRIMARY KEY,
                    experiment_id BIGINT,
                    name VARCHAR,
                    dose DOUBLE
                )
            """);
            
            // Probes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probes (
                    id BIGINT PRIMARY KEY,
                    probe_id VARCHAR,
                    symbol VARCHAR,
                    title VARCHAR
                )
            """);
            
            // Reference gene annotations
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reference_gene_annotations (
                    id BIGINT PRIMARY KEY,
                    experiment_id BIGINT,
                    probe_id BIGINT,
                    gene_id VARCHAR,
                    gene_symbol VARCHAR,
                    gene_title VARCHAR
                )
            """);
            
            // Probe responses (expression data)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probe_responses (
                    id BIGINT PRIMARY KEY,
                    experiment_id BIGINT,
                    probe_id BIGINT,
                    treatment_id BIGINT,
                    response_value FLOAT
                )
            """);
            
            // BMD results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bmd_results (
                    id BIGINT PRIMARY KEY,
                    project_id BIGINT,
                    name VARCHAR,
                    experiment_id BIGINT,
                    bmd_method VARCHAR,
                    analysis_info_id BIGINT
                )
            """);
            
            // Probe statistical results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probe_stat_results (
                    id BIGINT PRIMARY KEY,
                    bmd_result_id BIGINT,
                    probe_id BIGINT,
                    
                    -- Best model results
                    best_model VARCHAR,
                    best_bmd DOUBLE,
                    best_bmdl DOUBLE,
                    best_bmdu DOUBLE,
                    best_aic DOUBLE,
                    best_p_value DOUBLE,
                    best_fit_log_likelihood DOUBLE,
                    
                    -- Best poly model results
                    best_poly_model VARCHAR,
                    best_poly_bmd DOUBLE,
                    best_poly_bmdl DOUBLE,
                    best_poly_bmdu DOUBLE,
                    best_poly_aic DOUBLE,
                    best_poly_p_value DOUBLE,
                    
                    -- Prefilter statistics
                    prefilter_adjusted_p_value DOUBLE,
                    prefilter_p_value DOUBLE,
                    prefilter_best_fold_change DOUBLE,
                    prefilter_best_abs_fold_change DOUBLE,
                    prefilter_noel DOUBLE,
                    prefilter_loel DOUBLE,
                    
                    -- Gene information
                    genes VARCHAR,
                    gene_symbols VARCHAR
                )
            """);
            
            // Individual model fit results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS stat_results (
                    id BIGINT PRIMARY KEY,
                    probe_stat_result_id BIGINT,
                    model_name VARCHAR,
                    
                    -- BMD values
                    bmd DOUBLE,
                    bmdl DOUBLE,
                    bmdu DOUBLE,
                    
                    -- Model fit statistics
                    aic DOUBLE,
                    p_value DOUBLE,
                    fit_log_likelihood DOUBLE,
                    r_squared DOUBLE,
                    
                    -- BMD ratios
                    bmd_diff_bmdl DOUBLE,
                    bmdu_diff_bmdl DOUBLE,
                    bmdu_diff_bmd DOUBLE,
                    
                    -- Model characteristics
                    adverse_direction INTEGER,
                    is_step_function BOOLEAN,
                    success VARCHAR,
                    
                    -- Model parameters (as JSON string to avoid array issues)
                    curve_parameters VARCHAR,
                    residuals VARCHAR
                )
            """);
            
            // Category analysis results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_analysis_results (
                    id BIGINT PRIMARY KEY,
                    project_id BIGINT,
                    name VARCHAR,
                    bmd_result_id BIGINT,
                    analysis_info_id BIGINT
                )
            """);
            
            // Individual category results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS category_results (
                    id BIGINT PRIMARY KEY,
                    category_analysis_id BIGINT,
                    category_type VARCHAR,
                    category_id VARCHAR,
                    category_description VARCHAR,
                    goterm_level INTEGER,
                    genes VARCHAR,
                    gene_symbols VARCHAR,
                    
                    -- Count statistics
                    genes_in_category INTEGER,
                    total_genes INTEGER,
                    gene_count_significant_anova INTEGER,
                    genes_that_passed_all_filters INTEGER,
                    percentage DOUBLE,
                    
                    -- BMD statistics
                    bmd_mean DOUBLE,
                    bmd_median DOUBLE,
                    bmd_minimum DOUBLE,
                    bmd_maximum DOUBLE,
                    bmd_sd DOUBLE,
                    bmd_wmean DOUBLE,
                    bmd_wsd DOUBLE,
                    
                    -- BMDL statistics
                    bmdl_mean DOUBLE,
                    bmdl_median DOUBLE,
                    bmdl_minimum DOUBLE,
                    bmdl_maximum DOUBLE,
                    bmdl_sd DOUBLE,
                    bmdl_wmean DOUBLE,
                    bmdl_wsd DOUBLE,
                    
                    -- BMDU statistics
                    bmdu_mean DOUBLE,
                    bmdu_median DOUBLE,
                    bmdu_minimum DOUBLE,
                    bmdu_sd DOUBLE,
                    bmdu_wmean DOUBLE,
                    bmdu_wsd DOUBLE,
                    
                    -- Fisher's exact test
                    fishers_left_p_value DOUBLE,
                    fishers_right_p_value DOUBLE,
                    fishers_two_tail_p_value DOUBLE,
                    fishers_a INTEGER,
                    fishers_b INTEGER,
                    fishers_c INTEGER,
                    fishers_d INTEGER,
                    
                    -- Direction-based statistics
                    genes_up_bmd_mean DOUBLE,
                    genes_up_bmd_median DOUBLE,
                    genes_up_bmd_sd DOUBLE,
                    genes_up_bmdl_mean DOUBLE,
                    genes_up_bmdl_median DOUBLE,
                    genes_up_bmdl_sd DOUBLE,
                    genes_up_bmdu_mean DOUBLE,
                    genes_up_bmdu_median DOUBLE,
                    genes_up_bmdu_sd DOUBLE,
                    genes_down_bmd_mean DOUBLE,
                    genes_down_bmd_median DOUBLE,
                    genes_down_bmd_sd DOUBLE,
                    genes_down_bmdl_mean DOUBLE,
                    genes_down_bmdl_median DOUBLE,
                    genes_down_bmdl_sd DOUBLE,
                    genes_down_bmdu_mean DOUBLE,
                    genes_down_bmdu_median DOUBLE,
                    genes_down_bmdu_sd DOUBLE,
                    
                    -- Percentile statistics
                    bmd_fifth_percentile DOUBLE,
                    bmd_tenth_percentile DOUBLE,
                    bmdl_fifth_percentile DOUBLE,
                    bmdl_tenth_percentile DOUBLE,
                    bmdu_fifth_percentile DOUBLE,
                    bmdu_tenth_percentile DOUBLE,
                    
                    -- Ratio statistics
                    bmdu_div_bmdl_median DOUBLE,
                    bmd_div_bmdl_median DOUBLE,
                    bmdu_div_bmd_median DOUBLE,
                    bmdu_div_bmdl_mean DOUBLE,
                    bmd_div_bmdl_mean DOUBLE,
                    bmdu_div_bmd_mean DOUBLE,
                    
                    -- Filter counts
                    genes_with_bmd_less_equal_high_dose INTEGER,
                    genes_with_bmd_p_value_greater_equal_value INTEGER,
                    genes_with_bmd_r_squared_value_greater_equal_value INTEGER,
                    genes_with_bmd_bmdl_ratio_below_value INTEGER,
                    genes_with_bmdu_bmdl_ratio_below_value INTEGER,
                    genes_with_bmdu_bmd_ratio_below_value INTEGER,
                    genes_with_n_fold_below_low_positive_dose_value INTEGER,
                    genes_with_fold_change_above_value INTEGER,
                    genes_with_prefilter_p_value_above_value INTEGER,
                    genes_with_prefilter_adjusted_p_value_above_value INTEGER,
                    genes_not_step_function INTEGER,
                    genes_not_step_function_with_bmd_lower INTEGER,
                    genes_not_adverse_direction INTEGER,
                    
                    -- Fold change statistics
                    total_fold_change DOUBLE,
                    mean_fold_change DOUBLE,
                    median_fold_change DOUBLE,
                    max_fold_change DOUBLE,
                    min_fold_change DOUBLE,
                    std_dev_fold_change DOUBLE,
                    
                    -- Confidence intervals
                    bmd_lower_95 DOUBLE,
                    bmd_upper_95 DOUBLE,
                    
                    -- Additional fields
                    genes_with_conflicting_probe_sets VARCHAR,
                    neg_log_fishers_2_tail DOUBLE,
                    overall_direction VARCHAR
                )
            """);
            
            // Prefilter results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilter_results (
                    id BIGINT PRIMARY KEY,
                    project_id BIGINT,
                    name VARCHAR,
                    prefilter_type VARCHAR,
                    experiment_id BIGINT,
                    analysis_info_id BIGINT
                )
            """);
            
            // Prefilter probe results
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilter_probe_results (
                    id BIGINT PRIMARY KEY,
                    prefilter_result_id BIGINT,
                    probe_id BIGINT,
                    
                    -- Statistical values
                    p_value DOUBLE,
                    adjusted_p_value DOUBLE,
                    negative_log10_p_value DOUBLE,
                    negative_log_adjusted_p_value DOUBLE,
                    
                    -- ANOVA specific (if applicable)
                    f_value DOUBLE,
                    degrees_of_freedom_one INTEGER,
                    degrees_of_freedom_two INTEGER,
                    
                    -- Fold change statistics
                    best_fold_change DOUBLE,
                    best_fold_change_abs DOUBLE,
                    fold_changes VARCHAR, -- JSON array of fold changes
                    
                    -- NOEL/LOEL values
                    noel_dose DOUBLE,
                    loel_dose DOUBLE,
                    noel_loel_p_values VARCHAR, -- JSON array
                    
                    -- Gene information
                    genes VARCHAR,
                    gene_symbols VARCHAR
                )
            """);
        }
    }
    
    private long insertProject(BMDProject project) throws SQLException {
        String sql = "INSERT INTO projects (id, name) VALUES (?, ?)";
        long projectId = System.currentTimeMillis(); // Simple ID generation
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setString(2, project.getName());
            stmt.executeUpdate();
        }
        
        return projectId;
    }
    
    private void insertDoseResponseExperiment(DoseResponseExperiment exp, long projectId) throws SQLException {
        // Insert chip info first if it exists
        Long chipId = null;
        if (exp.getChip() != null) {
            chipId = insertChip(exp.getChip());
        }
        
        // Insert analysis info
        Long analysisInfoId = null;
        List<AnalysisInfo> analysisInfos = exp.getAnalysisInfo(true);
        if (analysisInfos != null && !analysisInfos.isEmpty()) {
            analysisInfoId = insertAnalysisInfo(analysisInfos.get(0), projectId, "experiment", exp.getName());
        }
        
        // Insert experiment
        String sql = "INSERT INTO dose_response_experiments (id, project_id, name, chip_id, log_transformation, analysis_info_id) VALUES (?, ?, ?, ?, ?, ?)";
        long expId = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, expId);
            stmt.setLong(2, projectId);
            stmt.setString(3, exp.getName());
            if (chipId != null) stmt.setLong(4, chipId); else stmt.setNull(4, java.sql.Types.BIGINT);
            stmt.setString(5, exp.getLogTransformation() != null ? exp.getLogTransformation().toString() : null);
            if (analysisInfoId != null) stmt.setLong(6, analysisInfoId); else stmt.setNull(6, java.sql.Types.BIGINT);
            stmt.executeUpdate();
        }
        
        // Insert treatments
        if (exp.getTreatments() != null) {
            insertTreatments(exp.getTreatments(), expId);
        }
        
        // Insert probes and responses
        if (exp.getProbeResponses() != null && !exp.getProbeResponses().isEmpty()) {
            insertProbeResponses(exp.getProbeResponses(), expId, exp.getTreatments());
        }
        
        // Insert gene annotations
        if (exp.getReferenceGeneAnnotations() != null) {
            insertReferenceGeneAnnotations(exp.getReferenceGeneAnnotations(), expId);
        }
    }
    
    private Long insertChip(com.sciome.bmdexpress2.mvp.model.chip.ChipInfo chip) throws SQLException {
        String sql = "INSERT INTO chips (id, name, geo_name, provider, species, creation_date) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        Long chipId = (long) chip.hashCode(); // Simple ID generation
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, chipId);
            stmt.setString(2, chip.getName());
            stmt.setString(3, chip.getGeoName());
            stmt.setString(4, chip.getProvider());
            stmt.setString(5, chip.getSpecies());
            stmt.setLong(6, System.currentTimeMillis());
            stmt.executeUpdate();
        }
        
        return chipId;
    }
    
    private Long insertAnalysisInfo(AnalysisInfo analysisInfo, long projectId, String type, String name) throws SQLException {
        String sql = "INSERT INTO analysis_info (id, project_id, analysis_type, analysis_name, notes) VALUES (?, ?, ?, ?, ?)";
        long infoId = System.currentTimeMillis() + (int)(Math.random() * 1000);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, infoId);
            stmt.setLong(2, projectId);
            stmt.setString(3, type);
            stmt.setString(4, name);
            
            // Convert notes list to JSON string (DuckDB doesn't support createArrayOf)
            if (analysisInfo.getNotes() != null && !analysisInfo.getNotes().isEmpty()) {
                String notesJson = String.join(",", analysisInfo.getNotes());
                stmt.setString(5, notesJson);
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
        }
        
        return infoId;
    }
    
    private void insertTreatments(List<Treatment> treatments, long expId) throws SQLException {
        String sql = "INSERT INTO treatments (id, experiment_id, name, dose) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < treatments.size(); i++) {
                Treatment treatment = treatments.get(i);
                stmt.setLong(1, expId * 1000 + i); // Generate unique ID
                stmt.setLong(2, expId);
                stmt.setString(3, treatment.getName());
                stmt.setDouble(4, treatment.getDose());
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertProbeResponses(List<ProbeResponse> probeResponses, long expId, List<Treatment> treatments) throws SQLException {
        // First insert probes
        String probeSQL = "INSERT INTO probes (id, probe_id, symbol, title) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        String responseSQL = "INSERT INTO probe_responses (id, experiment_id, probe_id, treatment_id, response_value) VALUES (?, ?, ?, ?, ?)";
        
        long responseCounter = 0;
        
        try (PreparedStatement probeStmt = connection.prepareStatement(probeSQL);
             PreparedStatement responseStmt = connection.prepareStatement(responseSQL)) {
            
            for (ProbeResponse pr : probeResponses) {
                Probe probe = pr.getProbe();
                long probeId = probe.getId() != null ? (long) probe.getId().hashCode() : (long) probe.hashCode();
                
                // Insert probe
                probeStmt.setLong(1, probeId);
                probeStmt.setString(2, probe.getId() != null ? probe.getId().toString() : "");
                probeStmt.setString(3, ""); // gene_symbol - not available in Probe
                probeStmt.setString(4, ""); // gene_name - not available in Probe
                probeStmt.executeUpdate();
                
                // Insert responses for each treatment
                List<Float> responses = pr.getResponses();
                if (responses != null && treatments != null) {
                    for (int i = 0; i < Math.min(responses.size(), treatments.size()); i++) {
                        responseStmt.setLong(1, ++responseCounter);
                        responseStmt.setLong(2, expId);
                        responseStmt.setLong(3, probeId);
                        responseStmt.setLong(4, expId * 1000 + i); // Treatment ID
                        responseStmt.setFloat(5, responses.get(i));
                        responseStmt.executeUpdate();
                    }
                }
            }
            
        }
    }
    
    private void insertReferenceGeneAnnotations(List<ReferenceGeneAnnotation> annotations, long expId) throws SQLException {
        String sql = "INSERT INTO reference_gene_annotations (id, experiment_id, probe_id, gene_id, gene_symbol, gene_title) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < annotations.size(); i++) {
                ReferenceGeneAnnotation annotation = annotations.get(i);
                stmt.setLong(1, expId * 10000 + i);
                stmt.setLong(2, expId);
                stmt.setLong(3, annotation.getProbe() != null ? (long) annotation.getProbe().hashCode() : 0);
                ReferenceGene gene = annotation.getReferenceGenes() != null && !annotation.getReferenceGenes().isEmpty() ? 
                    annotation.getReferenceGenes().get(0) : null;
                stmt.setString(4, gene != null ? gene.getId() : "");
                stmt.setString(5, gene != null ? gene.getGeneSymbol() : "");
                stmt.setString(6, ""); // gene name not available
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertBMDResult(BMDResult bmd, long projectId) throws SQLException {
        // Insert analysis info
        Long analysisInfoId = null;
        List<AnalysisInfo> bmdAnalysisInfos = bmd.getAnalysisInfo(true);
        if (bmdAnalysisInfos != null && !bmdAnalysisInfos.isEmpty()) {
            analysisInfoId = insertAnalysisInfo(bmdAnalysisInfos.get(0), projectId, "bmd", bmd.getName());
        }
        
        // Insert BMD result
        String sql = "INSERT INTO bmd_results (id, project_id, name, experiment_id, bmd_method, analysis_info_id) VALUES (?, ?, ?, ?, ?, ?)";
        long bmdId = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, bmdId);
            stmt.setLong(2, projectId);
            stmt.setString(3, bmd.getName());
            stmt.setLong(4, bmd.getDoseResponseExperiment() != null ? System.currentTimeMillis() : 0);
            stmt.setString(5, bmd.getBmdMethod() != null ? bmd.getBmdMethod().toString() : null);
            if (analysisInfoId != null) stmt.setLong(6, analysisInfoId); else stmt.setNull(6, java.sql.Types.BIGINT);
            stmt.executeUpdate();
        }
        
        // Insert probe statistical results
        if (bmd.getProbeStatResults() != null) {
            insertProbeStatResults(bmd.getProbeStatResults(), bmdId);
        }
    }
    
    private void insertProbeStatResults(List<ProbeStatResult> probeStatResults, long bmdId) throws SQLException {
        String probeStatSQL = "INSERT INTO probe_stat_results (" +
            "id, bmd_result_id, probe_id, " +
            "best_model, best_bmd, best_bmdl, best_bmdu, best_aic, best_p_value, best_fit_log_likelihood, " +
            "best_poly_model, best_poly_bmd, best_poly_bmdl, best_poly_bmdu, best_poly_aic, best_poly_p_value, " +
            "prefilter_adjusted_p_value, prefilter_p_value, prefilter_best_fold_change, prefilter_best_abs_fold_change, " +
            "prefilter_noel, prefilter_loel, genes, gene_symbols" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(probeStatSQL)) {
            for (int i = 0; i < probeStatResults.size(); i++) {
                ProbeStatResult psr = probeStatResults.get(i);
                long psrId = bmdId * 100000 + i;
                int paramIndex = 1;
                
                // Basic info
                stmt.setLong(paramIndex++, psrId);
                stmt.setLong(paramIndex++, bmdId);
                stmt.setLong(paramIndex++, psr.getProbeResponse() != null && psr.getProbeResponse().getProbe() != null ? 
                    (long) psr.getProbeResponse().getProbe().hashCode() : 0);
                
                // Best model info
                if (psr.getBestStatResult() != null) {
                    StatResult best = psr.getBestStatResult();
                    stmt.setString(paramIndex++, best.getClass().getSimpleName());
                    stmt.setDouble(paramIndex++, best.getBMD());
                    stmt.setDouble(paramIndex++, best.getBMDL());
                    stmt.setDouble(paramIndex++, best.getBMDU());
                    stmt.setDouble(paramIndex++, best.getAIC());
                    stmt.setDouble(paramIndex++, best.getFitPValue());
                    stmt.setDouble(paramIndex++, best.getFitLogLikelihood());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                }
                
                // Best poly model info
                if (psr.getBestPolyStatResult() != null) {
                    StatResult bestPoly = psr.getBestPolyStatResult();
                    stmt.setString(paramIndex++, bestPoly.getClass().getSimpleName());
                    stmt.setDouble(paramIndex++, bestPoly.getBMD());
                    stmt.setDouble(paramIndex++, bestPoly.getBMDL());
                    stmt.setDouble(paramIndex++, bestPoly.getBMDU());
                    stmt.setDouble(paramIndex++, bestPoly.getAIC());
                    stmt.setDouble(paramIndex++, bestPoly.getFitPValue());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                }
                
                // Prefilter statistics
                stmt.setObject(paramIndex++, psr.getPrefilterAdjustedPValue());
                stmt.setObject(paramIndex++, psr.getPrefilterPValue());
                stmt.setObject(paramIndex++, psr.getBestFoldChange());
                stmt.setObject(paramIndex++, psr.getBestABSFoldChange());
                stmt.setObject(paramIndex++, psr.getPrefilterNoel());
                stmt.setObject(paramIndex++, psr.getPrefilterLoel());
                
                // Gene information
                stmt.setString(paramIndex++, psr.getGenes());
                stmt.setString(paramIndex++, psr.getGeneSymbols());
                
                stmt.executeUpdate();
                
                // Insert individual stat results
                if (psr.getStatResults() != null) {
                    insertStatResults(psr.getStatResults(), psrId);
                }
            }
        }
    }
    
    private void insertStatResults(List<StatResult> statResults, long psrId) throws SQLException {
        String sql = "INSERT INTO stat_results (" +
            "id, probe_stat_result_id, model_name, bmd, bmdl, bmdu, aic, p_value, fit_log_likelihood, r_squared, " +
            "bmd_diff_bmdl, bmdu_diff_bmdl, bmdu_diff_bmd, adverse_direction, is_step_function, success, " +
            "curve_parameters, residuals" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < statResults.size(); i++) {
                StatResult sr = statResults.get(i);
                int paramIndex = 1;
                
                // Basic info
                stmt.setLong(paramIndex++, psrId * 100 + i);
                stmt.setLong(paramIndex++, psrId);
                stmt.setString(paramIndex++, sr.getClass().getSimpleName());
                
                // BMD values
                stmt.setDouble(paramIndex++, sr.getBMD());
                stmt.setDouble(paramIndex++, sr.getBMDL());
                stmt.setDouble(paramIndex++, sr.getBMDU());
                
                // Model fit statistics
                stmt.setDouble(paramIndex++, sr.getAIC());
                stmt.setDouble(paramIndex++, sr.getFitPValue());
                stmt.setDouble(paramIndex++, sr.getFitLogLikelihood());
                stmt.setDouble(paramIndex++, sr.getrSquared());
                
                // BMD ratios
                stmt.setDouble(paramIndex++, sr.getBMDdiffBMDL());
                stmt.setDouble(paramIndex++, sr.getBMDUdiffBMDL());
                stmt.setDouble(paramIndex++, sr.getBMDUdiffBMD());
                
                // Model characteristics
                stmt.setInt(paramIndex++, sr.getAdverseDirection());
                stmt.setBoolean(paramIndex++, sr.getIsStepFunction());
                stmt.setString(paramIndex++, sr.getSuccess());
                
                // Convert arrays to JSON strings to avoid DuckDB array issues
                if (sr.getCurveParameters() != null) {
                    StringBuilder params = new StringBuilder("[");
                    for (int j = 0; j < sr.getCurveParameters().length; j++) {
                        if (j > 0) params.append(",");
                        params.append(sr.getCurveParameters()[j]);
                    }
                    params.append("]");
                    stmt.setString(paramIndex++, params.toString());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                }
                
                if (sr.getResiduals() != null) {
                    StringBuilder residuals = new StringBuilder("[");
                    for (int j = 0; j < sr.getResiduals().length; j++) {
                        if (j > 0) residuals.append(",");
                        residuals.append(sr.getResiduals()[j]);
                    }
                    residuals.append("]");
                    stmt.setString(paramIndex++, residuals.toString());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                }
                
                stmt.executeUpdate();
            }
        }
    }
    
    private void insertCategoryAnalysisResults(CategoryAnalysisResults cat, long projectId) throws SQLException {
        // Insert analysis info
        Long analysisInfoId = null;
        List<AnalysisInfo> catAnalysisInfos = cat.getAnalysisInfo(true);
        if (catAnalysisInfos != null && !catAnalysisInfos.isEmpty()) {
            analysisInfoId = insertAnalysisInfo(catAnalysisInfos.get(0), projectId, "category", cat.getName());
        }
        
        // Insert category analysis results
        String sql = "INSERT INTO category_analysis_results (id, project_id, name, bmd_result_id, analysis_info_id) VALUES (?, ?, ?, ?, ?)";
        long catId = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, catId);
            stmt.setLong(2, projectId);
            stmt.setString(3, cat.getName());
            stmt.setLong(4, cat.getBmdResult() != null ? System.currentTimeMillis() : 0);
            if (analysisInfoId != null) stmt.setLong(5, analysisInfoId); else stmt.setNull(5, java.sql.Types.BIGINT);
            stmt.executeUpdate();
        }
        
        // Insert individual category results
        if (cat.getCategoryAnalsyisResults() != null) {
            insertCategoryResults(cat.getCategoryAnalsyisResults(), catId);
        }
    }
    
    private void insertCategoryResults(List<CategoryAnalysisResult> categoryResults, long catId) throws SQLException {
        String sql = "INSERT INTO category_results (" +
            "id, category_analysis_id, category_type, category_id, category_description, goterm_level, genes, gene_symbols, " +
            "genes_in_category, total_genes, gene_count_significant_anova, genes_that_passed_all_filters, percentage, " +
            "bmd_mean, bmd_median, bmd_minimum, bmd_maximum, bmd_sd, bmd_wmean, bmd_wsd, " +
            "bmdl_mean, bmdl_median, bmdl_minimum, bmdl_maximum, bmdl_sd, bmdl_wmean, bmdl_wsd, " +
            "bmdu_mean, bmdu_median, bmdu_minimum, bmdu_sd, bmdu_wmean, bmdu_wsd, " +
            "fishers_left_p_value, fishers_right_p_value, fishers_two_tail_p_value, fishers_a, fishers_b, fishers_c, fishers_d, " +
            "genes_up_bmd_mean, genes_up_bmd_median, genes_up_bmd_sd, genes_up_bmdl_mean, genes_up_bmdl_median, genes_up_bmdl_sd, " +
            "genes_up_bmdu_mean, genes_up_bmdu_median, genes_up_bmdu_sd, genes_down_bmd_mean, genes_down_bmd_median, genes_down_bmd_sd, " +
            "genes_down_bmdl_mean, genes_down_bmdl_median, genes_down_bmdl_sd, genes_down_bmdu_mean, genes_down_bmdu_median, genes_down_bmdu_sd, " +
            "bmd_fifth_percentile, bmd_tenth_percentile, bmdl_fifth_percentile, bmdl_tenth_percentile, bmdu_fifth_percentile, bmdu_tenth_percentile, " +
            "bmdu_div_bmdl_median, bmd_div_bmdl_median, bmdu_div_bmd_median, bmdu_div_bmdl_mean, bmd_div_bmdl_mean, bmdu_div_bmd_mean, " +
            "genes_with_bmd_less_equal_high_dose, genes_with_bmd_p_value_greater_equal_value, genes_with_bmd_r_squared_value_greater_equal_value, " +
            "genes_with_bmd_bmdl_ratio_below_value, genes_with_bmdu_bmdl_ratio_below_value, genes_with_bmdu_bmd_ratio_below_value, " +
            "genes_with_n_fold_below_low_positive_dose_value, genes_with_fold_change_above_value, genes_with_prefilter_p_value_above_value, " +
            "genes_with_prefilter_adjusted_p_value_above_value, genes_not_step_function, genes_not_step_function_with_bmd_lower, genes_not_adverse_direction, " +
            "total_fold_change, mean_fold_change, median_fold_change, max_fold_change, min_fold_change, std_dev_fold_change, " +
            "bmd_lower_95, bmd_upper_95, genes_with_conflicting_probe_sets, neg_log_fishers_2_tail, overall_direction" +
            ") VALUES (" + "?,".repeat(94).replaceAll(",$", "") + ")";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < categoryResults.size(); i++) {
                CategoryAnalysisResult car = categoryResults.get(i);
                int paramIndex = 1;
                
                // Basic info
                stmt.setLong(paramIndex++, catId * 10000 + i);
                stmt.setLong(paramIndex++, catId);
                stmt.setString(paramIndex++, car.getClass().getSimpleName());
                stmt.setString(paramIndex++, car.getCategoryIdentifier() != null ? car.getCategoryIdentifier().getId() : "");
                stmt.setString(paramIndex++, car.getCategoryIdentifier() != null ? car.getCategoryIdentifier().getTitle() : "");
                stmt.setObject(paramIndex++, car.getGotermLevel());
                stmt.setString(paramIndex++, car.getGenes());
                stmt.setString(paramIndex++, car.getGeneSymbols());
                
                // Count statistics
                stmt.setObject(paramIndex++, car.getGeneAllCountFromExperiment());
                stmt.setObject(paramIndex++, car.getGeneAllCount());
                stmt.setObject(paramIndex++, car.getGeneCountSignificantANOVA());
                stmt.setObject(paramIndex++, car.getGenesThatPassedAllFilters());
                stmt.setObject(paramIndex++, car.getPercentage());
                
                // BMD statistics
                stmt.setObject(paramIndex++, car.getBmdMean());
                stmt.setObject(paramIndex++, car.getBmdMedian());
                stmt.setObject(paramIndex++, car.getBmdMinimum());
                stmt.setObject(paramIndex++, car.getBMDMaximum());
                stmt.setObject(paramIndex++, car.getBmdSD());
                stmt.setObject(paramIndex++, car.getBmdWMean());
                stmt.setObject(paramIndex++, car.getBmdWSD());
                
                // BMDL statistics
                stmt.setObject(paramIndex++, car.getBmdlMean());
                stmt.setObject(paramIndex++, car.getBmdlMedian());
                stmt.setObject(paramIndex++, car.getBmdlMinimum());
                stmt.setObject(paramIndex++, car.getBMDLMaximum());
                stmt.setObject(paramIndex++, car.getBmdlSD());
                stmt.setObject(paramIndex++, car.getBmdlWMean());
                stmt.setObject(paramIndex++, car.getBmdlWSD());
                
                // BMDU statistics
                stmt.setObject(paramIndex++, car.getBmduMean());
                stmt.setObject(paramIndex++, car.getBmduMedian());
                stmt.setObject(paramIndex++, car.getBmduMinimum());
                stmt.setObject(paramIndex++, car.getBmduSD());
                stmt.setObject(paramIndex++, car.getBmduWMean());
                stmt.setObject(paramIndex++, car.getBmduWSD());
                
                // Fisher's exact test
                stmt.setObject(paramIndex++, car.getFishersExactLeftPValue());
                stmt.setObject(paramIndex++, car.getFishersExactRightPValue());
                stmt.setObject(paramIndex++, car.getFishersExactTwoTailPValue());
                stmt.setObject(paramIndex++, car.getFishersA());
                stmt.setObject(paramIndex++, car.getFishersB());
                stmt.setObject(paramIndex++, car.getFishersC());
                stmt.setObject(paramIndex++, car.getFishersD());
                
                // Direction-based statistics
                stmt.setObject(paramIndex++, car.getGenesUpBMDMean());
                stmt.setObject(paramIndex++, car.getGenesUpBMDMedian());
                stmt.setObject(paramIndex++, car.getGenesUpBMDSD());
                stmt.setObject(paramIndex++, car.getGenesUpBMDLMean());
                stmt.setObject(paramIndex++, car.getGenesUpBMDLMedian());
                stmt.setObject(paramIndex++, car.getGenesUpBMDLSD());
                stmt.setObject(paramIndex++, car.getGenesUpBMDUMean());
                stmt.setObject(paramIndex++, car.getGenesUpBMDUMedian());
                stmt.setObject(paramIndex++, car.getGenesUpBMDUSD());
                stmt.setObject(paramIndex++, car.getGenesDownBMDMean());
                stmt.setObject(paramIndex++, car.getGenesDownBMDMedian());
                stmt.setObject(paramIndex++, car.getGenesDownBMDSD());
                stmt.setObject(paramIndex++, car.getGenesDownBMDLMean());
                stmt.setObject(paramIndex++, car.getGenesDownBMDLMedian());
                stmt.setObject(paramIndex++, car.getGenesDownBMDLSD());
                stmt.setObject(paramIndex++, car.getGenesDownBMDUMean());
                stmt.setObject(paramIndex++, car.getGenesDownBMDUMedian());
                stmt.setObject(paramIndex++, car.getGenesDownBMDUSD());
                
                // Percentile statistics
                stmt.setObject(paramIndex++, car.getBmdFifthPercentileTotalGenes());
                stmt.setObject(paramIndex++, car.getBmdTenthPercentileTotalGenes());
                stmt.setObject(paramIndex++, car.getBmdlFifthPercentileTotalGenes());
                stmt.setObject(paramIndex++, car.getBmdlTenthPercentileTotalGenes());
                stmt.setObject(paramIndex++, car.getBmduFifthPercentileTotalGenes());
                stmt.setObject(paramIndex++, car.getBmduTenthPercentileTotalGenes());
                
                // Ratio statistics
                stmt.setObject(paramIndex++, car.getBMDUdivBMDLMEDIAN());
                stmt.setObject(paramIndex++, car.getBMDdivBMDLMEDIAN());
                stmt.setObject(paramIndex++, car.getBMDUdivBMDMEDIAN());
                stmt.setObject(paramIndex++, car.getBMDUdivBMDLMEAN());
                stmt.setObject(paramIndex++, car.getBMDdivBMDLMEAN());
                stmt.setObject(paramIndex++, car.getBMDUdivBMDMEAN());
                
                // Filter counts
                stmt.setObject(paramIndex++, car.getGenesWithBMDLessEqualHighDose());
                stmt.setObject(paramIndex++, car.getGenesWithBMDpValueGreaterEqualValue());
                stmt.setObject(paramIndex++, car.getGenesWithBMDRSquaredValueGreaterEqualValue());
                stmt.setObject(paramIndex++, car.getGenesWithBMDBMDLRatioBelowValue());
                stmt.setObject(paramIndex++, car.getGenesWithBMDUBMDLRatioBelowValue());
                stmt.setObject(paramIndex++, car.getGenesWithBMDUBMDRatioBelowValue());
                stmt.setObject(paramIndex++, car.getGenesWithNFoldBelowLowPostiveDoseValue());
                stmt.setObject(paramIndex++, car.getGenesWithFoldChangeAboveValue());
                stmt.setObject(paramIndex++, car.getGenesWithPrefilterPValueAboveValue());
                stmt.setObject(paramIndex++, car.getGenesWithPrefilterAdjustedPValueAboveValue());
                stmt.setObject(paramIndex++, car.getGenesNotStepFunction());
                stmt.setObject(paramIndex++, car.getGenesNotStepFunctionWithBMDLower());
                stmt.setObject(paramIndex++, car.getGenesNotAdverseDirection());
                
                // Fold change statistics
                stmt.setObject(paramIndex++, car.gettotalFoldChange());
                stmt.setObject(paramIndex++, car.getmeanFoldChange());
                stmt.setObject(paramIndex++, car.getmedianFoldChange());
                stmt.setObject(paramIndex++, car.getmaxFoldChange());
                stmt.setObject(paramIndex++, car.getminFoldChange());
                stmt.setObject(paramIndex++, car.getstdDevFoldChange());
                
                // Confidence intervals
                stmt.setObject(paramIndex++, car.getbmdLower95());
                stmt.setObject(paramIndex++, car.getbmdUpper95());
                
                // Additional fields
                stmt.setString(paramIndex++, car.getGenesWithConflictingProbeSets());
                stmt.setObject(paramIndex++, car.getNegLogOfFishers2Tail());
                stmt.setString(paramIndex++, car.getOverallDirection() != null ? car.getOverallDirection().toString() : null);
                
                stmt.executeUpdate();
            }
        }
    }
    
    private void exportPrefilterResults(BMDProject project, long projectId) throws SQLException {
        // Export ANOVA results
        for (OneWayANOVAResults anova : project.getOneWayANOVAResults()) {
            insertPrefilterResults(anova, projectId, "ANOVA");
        }
        
        // Export Williams Trend results
        for (WilliamsTrendResults williams : project.getWilliamsTrendResults()) {
            insertPrefilterResults(williams, projectId, "Williams");
        }
        
        // Export CurveFit results
        for (CurveFitPrefilterResults curvefit : project.getCurveFitPrefilterResults()) {
            insertPrefilterResults(curvefit, projectId, "CurveFit");
        }
        
        // Export Oriogen results
        for (OriogenResults oriogen : project.getOriogenResults()) {
            insertPrefilterResults(oriogen, projectId, "Oriogen");
        }
    }
    
    private void insertPrefilterResults(com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResults prefilterResults, long projectId, String type) throws SQLException {
        // Insert analysis info
        Long analysisInfoId = null;
        List<AnalysisInfo> prefilterAnalysisInfos = prefilterResults.getPrefilterAnalysisInfo(true);
        if (prefilterAnalysisInfos != null && !prefilterAnalysisInfos.isEmpty()) {
            analysisInfoId = insertAnalysisInfo(prefilterAnalysisInfos.get(0), projectId, "prefilter_" + type.toLowerCase(), ((BMDExpressAnalysisDataSet)prefilterResults).getName());
        }
        
        // Insert prefilter results
        String sql = "INSERT INTO prefilter_results (id, project_id, name, prefilter_type, experiment_id, analysis_info_id) VALUES (?, ?, ?, ?, ?, ?)";
        long prefilterId = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, prefilterId);
            stmt.setLong(2, projectId);
            stmt.setString(3, ((BMDExpressAnalysisDataSet)prefilterResults).getName());
            stmt.setString(4, type);
            stmt.setLong(5, prefilterResults.getDoseResponseExperiement() != null ? System.currentTimeMillis() : 0);
            if (analysisInfoId != null) stmt.setLong(6, analysisInfoId); else stmt.setNull(6, java.sql.Types.BIGINT);
            stmt.executeUpdate();
        }
        
        // Insert individual prefilter probe results
        if (prefilterResults.getPrefilterResults() != null) {
            insertPrefilterProbeResults(prefilterResults.getPrefilterResults(), prefilterId);
        }
    }
    
    private void insertPrefilterProbeResults(List<PrefilterResult> prefilterProbeResults, long prefilterId) throws SQLException {
        String sql = "INSERT INTO prefilter_probe_results (" +
            "id, prefilter_result_id, probe_id, p_value, adjusted_p_value, negative_log10_p_value, negative_log_adjusted_p_value, " +
            "f_value, degrees_of_freedom_one, degrees_of_freedom_two, best_fold_change, best_fold_change_abs, " +
            "fold_changes, noel_dose, loel_dose, noel_loel_p_values, genes, gene_symbols" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < prefilterProbeResults.size(); i++) {
                PrefilterResult pfr = prefilterProbeResults.get(i);
                int paramIndex = 1;
                
                // Basic info
                stmt.setLong(paramIndex++, prefilterId * 100000 + i);
                stmt.setLong(paramIndex++, prefilterId);
                stmt.setLong(paramIndex++, pfr.getProbeResponse() != null && pfr.getProbeResponse().getProbe() != null ? 
                    (long) pfr.getProbeResponse().getProbe().hashCode() : 0);
                
                // Statistical values
                stmt.setDouble(paramIndex++, pfr.getpValue());
                stmt.setDouble(paramIndex++, pfr.getAdjustedPValue());
                
                // Try to get more specific statistics if this is an ANOVA result
                if (pfr instanceof com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) {
                    com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult anova = 
                        (com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) pfr;
                    stmt.setDouble(paramIndex++, anova.getNegativeLog10pValue());
                    stmt.setDouble(paramIndex++, anova.getNegativeLogAdjustedPValue());
                    stmt.setDouble(paramIndex++, anova.getfValue());
                    stmt.setInt(paramIndex++, anova.getDegreesOfFreedomOne());
                    stmt.setInt(paramIndex++, anova.getDegreesOfFreedomTwo());
                } else {
                    // Use calculated values or nulls for non-ANOVA results
                    stmt.setDouble(paramIndex++, -Math.log10(pfr.getpValue()));
                    stmt.setDouble(paramIndex++, -Math.log10(pfr.getAdjustedPValue()));
                    stmt.setNull(paramIndex++, java.sql.Types.DOUBLE);
                    stmt.setNull(paramIndex++, java.sql.Types.INTEGER);
                    stmt.setNull(paramIndex++, java.sql.Types.INTEGER);
                }
                
                // Fold change statistics
                stmt.setObject(paramIndex++, pfr.getBestFoldChange());
                if (pfr instanceof com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) {
                    com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult anova = 
                        (com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) pfr;
                    stmt.setObject(paramIndex++, anova.getBestFoldChangeABS());
                } else {
                    stmt.setObject(paramIndex++, pfr.getBestFoldChange() != null ? Math.abs(pfr.getBestFoldChange()) : null);
                }
                
                // Convert fold changes list to JSON string
                if (pfr.getFoldChanges() != null && !pfr.getFoldChanges().isEmpty()) {
                    StringBuilder foldChanges = new StringBuilder("[");
                    for (int j = 0; j < pfr.getFoldChanges().size(); j++) {
                        if (j > 0) foldChanges.append(",");
                        foldChanges.append(pfr.getFoldChanges().get(j));
                    }
                    foldChanges.append("]");
                    stmt.setString(paramIndex++, foldChanges.toString());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                }
                
                // NOEL/LOEL values
                stmt.setObject(paramIndex++, pfr.getNoelDose());
                stmt.setObject(paramIndex++, pfr.getLoelDose());
                
                // Convert NOEL/LOEL p-values to JSON string (if available)
                if (pfr instanceof com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) {
                    com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult anova = 
                        (com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult) pfr;
                    if (anova.getNoelLoelPValues() != null && !anova.getNoelLoelPValues().isEmpty()) {
                        StringBuilder pValues = new StringBuilder("[");
                        for (int j = 0; j < anova.getNoelLoelPValues().size(); j++) {
                            if (j > 0) pValues.append(",");
                            pValues.append(anova.getNoelLoelPValues().get(j));
                        }
                        pValues.append("]");
                        stmt.setString(paramIndex++, pValues.toString());
                    } else {
                        stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                    }
                    
                    // Gene information
                    stmt.setString(paramIndex++, anova.getGenes());
                    stmt.setString(paramIndex++, anova.getGeneSymbols());
                } else {
                    stmt.setNull(paramIndex++, java.sql.Types.VARCHAR);
                    stmt.setString(paramIndex++, ""); // genes
                    stmt.setString(paramIndex++, ""); // gene_symbols
                }
                
                stmt.executeUpdate();
            }
        }
    }
}