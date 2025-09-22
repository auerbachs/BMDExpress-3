import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Probe;
import com.sciome.bmdexpress2.mvp.model.chip.ChipInfo;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGene;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;

/**
 * Maps BMDProject data to the web application's unified schema.
 * This ensures data is stored in the exact format expected by the web application.
 */
public class BMDDataMapper {
    
    private final Connection connection;
    private long nextId = 1;
    
    public BMDDataMapper(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Maps all data from BMDProject to the web application schema
     */
    public void mapProjectData(BMDProject project) throws SQLException {
        System.out.println("Mapping BMDProject data to web application schema...");
        
        // Create datasets and groups first
        long datasetId = insertDataset(project);
        long groupId = insertGroup("Default Group");
        
        // Map core experiment data
        Map<Long, Long> experimentIdMap = new HashMap<>();
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            long newExpId = insertDoseResponseExperiment(exp, datasetId);
            experimentIdMap.put(exp.getID(), newExpId);
        }
        
        // Map treatments and dose groups
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            long newExpId = experimentIdMap.get(exp.getID());
            mapTreatments(exp, newExpId);
            mapDoseGroups(exp, newExpId);
            mapProbeResponses(exp, newExpId);
        }
        
        // Map probes
        mapProbes(project);
        
        // Map chips
        mapChips(project);
        
        // Map prefilter results
        mapPrefilterResults(project, experimentIdMap);
        
        // Map BMD results
        Map<BMDResult, Long> bmdResultIdMap = mapBmdResults(project, experimentIdMap, datasetId);
        
        // Map category analysis results
        mapCategoryAnalysisResults(project, datasetId, bmdResultIdMap);
        
        // Map reference genes and annotations
        mapReferenceGenes(project);
        mapReferenceGeneAnnotations(project, experimentIdMap);
        
        System.out.println("✅ Data mapping completed successfully!");
    }
    
    private long insertDataset(BMDProject project) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO datasets (id, name, created, groupId, dataType, groupName) VALUES (?, ?, ?, ?, ?, ?)")) {
            long id = nextId++;
            stmt.setLong(1, id);
            stmt.setString(2, project.getName());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(4, 1L); // Default group
            stmt.setString(5, "BMDExpress");
            stmt.setString(6, "Default Group");
            stmt.executeUpdate();
            System.out.println("    ✅ Inserted dataset: " + project.getName());
            return id;
        }
    }
    
    private long insertGroup(String groupName) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO groups (id, name) VALUES (?, ?)")) {
            long id = 1L;
            stmt.setLong(1, id);
            stmt.setString(2, groupName);
            stmt.executeUpdate();
            System.out.println("    ✅ Inserted group: " + groupName);
            return id;
        }
    }
    
    private long insertDoseResponseExperiment(DoseResponseExperiment exp, long datasetId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO doseResponseExperiments (id, name, sex, organ, species, dataType, platform, chipId, logTransformation, columnHeader2, chipCreationDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            long id = nextId++;
            stmt.setLong(1, id);
            stmt.setString(2, exp.getName());
            stmt.setString(3, "Unknown"); // sex - not available in BMDExpress
            stmt.setString(4, "Unknown"); // organ - not available in BMDExpress
            stmt.setString(5, "Unknown"); // species - not available in BMDExpress
            stmt.setString(6, "Unknown"); // dataType - not available in BMDExpress
            stmt.setString(7, "Unknown"); // platform - not available in BMDExpress
            long chipId = 0L;
            if (exp.getChip() != null && exp.getChip().getId() != null) {
                try {
                    chipId = Long.parseLong(exp.getChip().getId());
                } catch (NumberFormatException e) {
                    chipId = 0L;
                }
            }
            stmt.setLong(8, chipId);
            stmt.setString(9, exp.getLogTransformation() != null ? exp.getLogTransformation().toString() : null);
            stmt.setString(10, exp.getColumnHeader2() != null ? exp.getColumnHeader2().toString() : null);
            stmt.setLong(11, exp.getChipCreationDate() != null ? exp.getChipCreationDate() : 0L);
            stmt.executeUpdate();
            return id;
        }
    }
    
    private void mapTreatments(DoseResponseExperiment exp, long experimentId) throws SQLException {
        for (Treatment treatment : exp.getTreatments()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO treatments (id, doseResponseExperimentId, name, dose) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, nextId++);
                stmt.setLong(2, experimentId);
                stmt.setString(3, treatment.getName());
                stmt.setDouble(4, treatment.getDose());
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + exp.getTreatments().size() + " treatments");
    }
    
    private void mapDoseGroups(DoseResponseExperiment exp, long experimentId) throws SQLException {
        for (var doseGroup : exp.getDoseGroups()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO doseGroups (id, doseResponseExperimentId, dose, n, responseMean) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setLong(1, nextId++);
                stmt.setLong(2, experimentId);
                stmt.setDouble(3, doseGroup.getDose() != null ? doseGroup.getDose() : 0.0);
                stmt.setLong(4, doseGroup.getCount());
                stmt.setDouble(5, doseGroup.getResponseMean() != null ? doseGroup.getResponseMean() : 0.0);
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + exp.getDoseGroups().size() + " dose groups");
    }
    
    private void mapProbeResponses(DoseResponseExperiment exp, long experimentId) throws SQLException {
        for (ProbeResponse probeResponse : exp.getProbeResponses()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO probeResponses (id, doseResponseExperimentId, probeId, responses) VALUES (?, ?, ?, ?)")) {
                stmt.setLong(1, nextId++);
                stmt.setLong(2, experimentId);
                stmt.setString(3, probeResponse.getProbe().getId());
                stmt.setString(4, probeResponse.getResponses() != null ? probeResponse.getResponses().toString() : null);
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + exp.getProbeResponses().size() + " probe responses");
    }
    
    private void mapProbes(BMDProject project) throws SQLException {
        Set<String> probeIds = new HashSet<>();
        
        // Collect all probe IDs from experiments
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            for (ProbeResponse probeResponse : exp.getProbeResponses()) {
                probeIds.add(probeResponse.getProbe().getId());
            }
        }
        
        for (String probeId : probeIds) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO probes (id) VALUES (?)")) {
                stmt.setString(1, probeId);
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + probeIds.size() + " probes");
    }
    
    private void mapChips(BMDProject project) throws SQLException {
        Set<ChipInfo> chips = new HashSet<>();
        
        // Collect all unique chips from experiments
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            if (exp.getChip() != null) {
                chips.add(exp.getChip());
            }
        }
        
        for (ChipInfo chip : chips) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO chips (id, name, provider, species, geoID, geoName) VALUES (?, ?, ?, ?, ?, ?)")) {
                long chipId = 0L;
                if (chip.getId() != null) {
                    try {
                        chipId = Long.parseLong(chip.getId());
                    } catch (NumberFormatException e) {
                        chipId = 0L;
                    }
                }
                stmt.setLong(1, chipId);
                stmt.setString(2, chip.getName());
                stmt.setString(3, chip.getProvider());
                stmt.setString(4, chip.getSpecies());
                stmt.setString(5, chip.getGeoID());
                stmt.setString(6, chip.getGeoName());
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + chips.size() + " chips");
    }
    
    private void mapPrefilterResults(BMDProject project, Map<Long, Long> experimentIdMap) throws SQLException {
        // Map One-Way ANOVA results
        for (OneWayANOVAResults anovaResults : project.getOneWayANOVAResults()) {
            insertPrefilterResultSet(anovaResults, "OneWayANOVA", experimentIdMap);
        }
        
        // Map Williams Trend results
        for (WilliamsTrendResults williamsResults : project.getWilliamsTrendResults()) {
            insertPrefilterResultSet(williamsResults, "WilliamsTrend", experimentIdMap);
        }
        
        // Map Curve Fit Prefilter results
        for (CurveFitPrefilterResults curveFitResults : project.getCurveFitPrefilterResults()) {
            insertPrefilterResultSet(curveFitResults, "CurveFitPrefilter", experimentIdMap);
        }
        
        // Map Oriogen results
        for (OriogenResults oriogenResults : project.getOriogenResults()) {
            insertPrefilterResultSet(oriogenResults, "Oriogen", experimentIdMap);
        }
    }
    
    private void insertPrefilterResultSet(Object resultSet, String prefilterType, Map<Long, Long> experimentIdMap) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO prefilterResultSets (id, name, sex, organ, species, dataType, platform, prefilterType, doseResponseExperimentId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            long id = nextId++;
            stmt.setLong(1, id);
            stmt.setString(2, "Prefilter Result Set " + id);
            stmt.setString(3, "Unknown");
            stmt.setString(4, "Unknown");
            stmt.setString(5, "Unknown");
            stmt.setString(6, "Unknown");
            stmt.setString(7, "Unknown");
            stmt.setString(8, prefilterType);
            stmt.setLong(9, experimentIdMap.values().iterator().next()); // Use first experiment
            stmt.executeUpdate();
            System.out.println("    ✅ Inserted prefilter result set: " + prefilterType);
        }
    }
    
    private Map<BMDResult, Long> mapBmdResults(BMDProject project, Map<Long, Long> experimentIdMap, long datasetId) throws SQLException {
        Map<BMDResult, Long> bmdResultIdMap = new HashMap<>();
        
        for (BMDResult bmdResult : project.getbMDResult()) {
            long bmdResultId = nextId++;
            bmdResultIdMap.put(bmdResult, bmdResultId);
            
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO bmdResults (id, name, sex, organ, species, dataType, platform, doseResponseExperimentId, prefilterResultSetId, bmdMethod, wAUC, logwAUC, wAUCList, logwAUCList, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, bmdResultId);
                stmt.setString(2, bmdResult.getName());
                stmt.setString(3, "Unknown"); // sex - not available in BMDExpress
                stmt.setString(4, "Unknown"); // organ - not available in BMDExpress
                stmt.setString(5, "Unknown"); // species - not available in BMDExpress
                stmt.setString(6, "Unknown"); // dataType - not available in BMDExpress
                stmt.setString(7, "Unknown"); // platform - not available in BMDExpress
                stmt.setLong(8, experimentIdMap.values().iterator().next()); // Use first experiment
                stmt.setLong(9, 0L); // No prefilter result set
                stmt.setString(10, bmdResult.getBmdMethod() != null ? bmdResult.getBmdMethod().toString() : "Unknown");
                stmt.setDouble(11, 0.0); // wAUC - not available in BMDExpress
                stmt.setDouble(12, 0.0); // logwAUC - not available in BMDExpress
                stmt.setString(13, null); // wAUCList
                stmt.setString(14, null); // logwAUCList
                stmt.setLong(15, datasetId);
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + project.getbMDResult().size() + " BMD results");
        return bmdResultIdMap;
    }
    
    private void mapCategoryAnalysisResults(BMDProject project, long datasetId, Map<BMDResult, Long> bmdResultIdMap) throws SQLException {
        // Get the first BMD result ID to use for category analysis results sets
        long firstBmdResultId = 0L;
        if (!bmdResultIdMap.isEmpty()) {
            firstBmdResultId = bmdResultIdMap.values().iterator().next();
        }
        
        for (CategoryAnalysisResults categoryResults : project.getCategoryAnalysisResults()) {
            // Insert category analysis results set
            long resultsSetId = insertCategoryAnalysisResultsSet(categoryResults, datasetId, firstBmdResultId);
            
            // Insert category identifiers first
            insertCategoryIdentifiers(categoryResults);
            
            // Insert individual category analysis results
            insertCategoryAnalysisResults(categoryResults, resultsSetId);
        }
    }
    
    private long insertCategoryAnalysisResultsSet(CategoryAnalysisResults categoryResults, long datasetId, long bmdResultId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO categoryAnalysisResultsSets (id, name, sex, organ, species, dataType, platform, bmdResultId, datasetId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            long id = nextId++;
            stmt.setLong(1, id);
            stmt.setString(2, categoryResults.getName());
            stmt.setString(3, "Unknown"); // sex - not available in BMDExpress
            stmt.setString(4, "Unknown"); // organ - not available in BMDExpress
            stmt.setString(5, "Unknown"); // species - not available in BMDExpress
            stmt.setString(6, "Unknown"); // dataType - not available in BMDExpress
            stmt.setString(7, "Unknown"); // platform - not available in BMDExpress
            stmt.setLong(8, bmdResultId);
            stmt.setLong(9, datasetId);
            stmt.executeUpdate();
            return id;
        }
    }
    
    private void insertCategoryIdentifiers(CategoryAnalysisResults categoryResults) throws SQLException {
        Set<String> categoryIds = new HashSet<>();
        
        // Collect unique category IDs from results
        for (CategoryAnalysisResult result : categoryResults.getCategoryAnalsyisResults()) {
            categoryIds.add(result.getCategoryID());
        }
        
        for (String categoryId : categoryIds) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT OR IGNORE INTO categoryIdentifiers (id, title, modelType, goLevel) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, categoryId);
                stmt.setString(2, "Category " + categoryId); // Use ID as title
                stmt.setString(3, "go"); // Set modelType to 'go' as expected by query
                stmt.setString(4, "Unknown"); // goLevel - not available
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + categoryIds.size() + " category identifiers");
    }
    
    private void insertCategoryAnalysisResults(CategoryAnalysisResults categoryResults, long resultsSetId) throws SQLException {
        for (CategoryAnalysisResult result : categoryResults.getCategoryAnalsyisResults()) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO categoryAnalysisResults (id, categoryAnalysisResultsId, categoryIdentifierId, modelType, geneAllCount, percentage, genesThatPassedAllFilters, bmdFifthPercentileTotalGenes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setLong(1, nextId++);
                stmt.setLong(2, resultsSetId);
                stmt.setString(3, result.getCategoryID()); // Use getCategoryID() instead
                stmt.setString(4, "go"); // Set modelType to 'go' as expected by query
                stmt.setLong(5, result.getGeneAllCount() != null ? result.getGeneAllCount() : 0L);
                stmt.setDouble(6, result.getPercentage() != null ? result.getPercentage() : 0.0);
                stmt.setLong(7, result.getGenesThatPassedAllFilters() != null ? result.getGenesThatPassedAllFilters() : 0L);
                stmt.setDouble(8, result.getBmdFifthPercentileTotalGenes() != null ? result.getBmdFifthPercentileTotalGenes() : 0.0);
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + categoryResults.getCategoryAnalsyisResults().size() + " category analysis results");
    }
    
    private void mapReferenceGenes(BMDProject project) throws SQLException {
        Set<String> geneIds = new HashSet<>();
        
        // Collect reference gene IDs from annotations
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            for (ReferenceGeneAnnotation annotation : exp.getReferenceGeneAnnotations()) {
                for (ReferenceGene gene : annotation.getReferenceGenes()) {
                    geneIds.add(gene.getId());
                }
            }
        }
        
        for (String geneId : geneIds) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO referenceGenes (id, geneSymbol) VALUES (?, ?)")) {
                stmt.setString(1, geneId);
                stmt.setString(2, geneId); // Use ID as symbol for now
                stmt.executeUpdate();
            }
        }
        System.out.println("    ✅ Mapped " + geneIds.size() + " reference genes");
    }
    
    private void mapReferenceGeneAnnotations(BMDProject project, Map<Long, Long> experimentIdMap) throws SQLException {
        for (DoseResponseExperiment exp : project.getDoseResponseExperiments()) {
            long newExpId = experimentIdMap.get(exp.getID());
            for (ReferenceGeneAnnotation annotation : exp.getReferenceGeneAnnotations()) {
                for (ReferenceGene gene : annotation.getReferenceGenes()) {
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO referenceGeneAnnotations (id, doseResponseExperimentId, probeId, referenceGeneId) VALUES (?, ?, ?, ?)")) {
                        stmt.setLong(1, nextId++);
                        stmt.setLong(2, newExpId);
                        stmt.setString(3, annotation.getProbe().getId());
                        stmt.setString(4, gene.getId());
                        stmt.executeUpdate();
                    }
                }
            }
        }
        System.out.println("    ✅ Mapped reference gene annotations");
    }
}
