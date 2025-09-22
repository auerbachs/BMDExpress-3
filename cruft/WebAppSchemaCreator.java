import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * Creates database schema that matches the web application's unified schema exactly.
 * This ensures complete compatibility with the web application.
 */
public class WebAppSchemaCreator {
    
    private final Connection connection;
    
    public WebAppSchemaCreator(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Creates all tables according to the web application's unified schema.
     * Tables are created in dependency order to respect foreign key constraints.
     */
    public void createSchema() throws SQLException {
        System.out.println("Creating web application schema...");
        
        // Create tables in dependency order
        createDatasetsTable();
        createGroupsTable();
        createChipsTable();
        createProbesTable();
        createReferenceGenesTable();
        createDoseResponseExperimentsTable();
        createDoseGroupsTable();
        createTreatmentsTable();
        createProbeResponsesTable();
        createPrefilterResultSetsTable();
        createBmdResultsTable();
        createPrefilterResultsTable();
        createReferenceGeneAnnotationsTable();
        createProbeStatResultsTable();
        createStatResultsTable();
        createChiSquaredResultsTable();
        createCategoryAnalysisResultsSetsTable();
        createCategoryIdentifiersTable();
        createUmapReferencesTable();
        createCategoryAnalysisResultsTable();
        createReferenceGeneProbeStatResultsTable();
        createRefGeneProbeStatProbeStatJoinTable();
        createMetaTable(); // WASM compatibility
        
        // Create compound indexes for optimal query performance
        createCompoundIndexes();
        
        System.out.println("✅ Web application schema created successfully!");
    }
    
    private void createDatasetsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS datasets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    created TIMESTAMP,
                    groupId BIGINT,
                    dataType VARCHAR,
                    groupName VARCHAR
                )
            """);
            System.out.println("  ✅ Created datasets table");
        }
    }
    
    private void createGroupsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS groups (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR UNIQUE
                )
            """);
            System.out.println("  ✅ Created groups table");
        }
    }
    
    private void createChipsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chips (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    provider VARCHAR,
                    species VARCHAR,
                    geoID VARCHAR,
                    geoName VARCHAR
                )
            """);
            System.out.println("  ✅ Created chips table");
        }
    }
    
    private void createProbesTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probes (
                    id VARCHAR PRIMARY KEY
                )
            """);
            System.out.println("  ✅ Created probes table");
        }
    }
    
    private void createReferenceGenesTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS referenceGenes (
                    id VARCHAR PRIMARY KEY,
                    geneSymbol VARCHAR
                )
            """);
            System.out.println("  ✅ Created referenceGenes table");
        }
    }
    
    private void createDoseResponseExperimentsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
                    columnHeader2 VARCHAR,
                    chipCreationDate BIGINT
                )
            """);
            System.out.println("  ✅ Created doseResponseExperiments table");
        }
    }
    
    private void createDoseGroupsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS doseGroups (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    dose DOUBLE,
                    n BIGINT,
                    responseMean DOUBLE
                )
            """);
            System.out.println("  ✅ Created doseGroups table");
        }
    }
    
    private void createTreatmentsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS treatments (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    name VARCHAR,
                    dose DOUBLE
                )
            """);
            System.out.println("  ✅ Created treatments table");
        }
    }
    
    private void createProbeResponsesTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probeResponses (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    probeId VARCHAR,
                    responses VARCHAR
                )
            """);
            System.out.println("  ✅ Created probeResponses table");
        }
    }
    
    private void createPrefilterResultSetsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilterResultSets (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR,
                    sex VARCHAR,
                    organ VARCHAR,
                    species VARCHAR,
                    dataType VARCHAR,
                    platform VARCHAR,
                    prefilterType VARCHAR,
                    doseResponseExperimentId BIGINT
                )
            """);
            System.out.println("  ✅ Created prefilterResultSets table");
        }
    }
    
    private void createBmdResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
                    wAUCList VARCHAR,
                    logwAUCList VARCHAR,
                    datasetId BIGINT
                )
            """);
            System.out.println("  ✅ Created bmdResults table");
        }
    }
    
    private void createPrefilterResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS prefilterResults (
                    id BIGINT PRIMARY KEY,
                    prefilterResultSetId BIGINT,
                    probeId VARCHAR,
                    pValue DOUBLE,
                    adjustedPValue DOUBLE,
                    bestFoldChange DOUBLE,
                    foldChanges VARCHAR,
                    loelDose DOUBLE,
                    noelDose DOUBLE,
                    noelLoelPValues VARCHAR,
                    fValue DOUBLE,
                    df1 BIGINT,
                    df2 BIGINT,
                    williamsPValue DOUBLE,
                    williamsAdjustedPValue DOUBLE,
                    curveFitPValue DOUBLE,
                    curveFitAdjustedPValue DOUBLE,
                    profile VARCHAR
                )
            """);
            System.out.println("  ✅ Created prefilterResults table");
        }
    }
    
    private void createReferenceGeneAnnotationsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS referenceGeneAnnotations (
                    id BIGINT PRIMARY KEY,
                    doseResponseExperimentId BIGINT,
                    probeId VARCHAR,
                    referenceGeneId VARCHAR
                )
            """);
            System.out.println("  ✅ Created referenceGeneAnnotations table");
        }
    }
    
    private void createProbeStatResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS probeStatResults (
                    id BIGINT PRIMARY KEY,
                    bmdResultId BIGINT,
                    probeResponseId BIGINT,
                    bestStatResultId BIGINT,
                    bestPolyStatResultId BIGINT
                )
            """);
            System.out.println("  ✅ Created probeStatResults table");
        }
    }
    
    private void createStatResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS statResults (
                    id BIGINT PRIMARY KEY,
                    probeStatResultId BIGINT,
                    modelType VARCHAR,
                    fitPValue DOUBLE,
                    adverseDirection BIGINT,
                    fitLogLikelihood DOUBLE,
                    AIC DOUBLE,
                    BMD DOUBLE,
                    BMDL DOUBLE,
                    BMDU DOUBLE,
                    success VARCHAR,
                    rSquared DOUBLE,
                    isStepFunction BOOLEAN,
                    isStepWithBMDLessLowest BOOLEAN,
                    residuals VARCHAR,
                    curveParameters VARCHAR,
                    otherParameters VARCHAR,
                    covariances VARCHAR,
                    zscore DOUBLE,
                    bmrCountsToTop BIGINT,
                    foldChangeToTop DOUBLE,
                    bmdLowDoseRatio DOUBLE,
                    bmdHighDoseRatio DOUBLE,
                    bmdResponseLowDoseResponseRatio DOUBLE,
                    bmdResponseHighDoseResponseRatio DOUBLE,
                    kFlag BIGINT,
                    option BIGINT,
                    degree BIGINT,
                    vertext VARCHAR,
                    bmr DOUBLE,
                    weightedAverages VARCHAR,
                    weightedStdDeviations VARCHAR,
                    adjustedControlDoseValue DOUBLE,
                    modelWeights VARCHAR
                )
            """);
            System.out.println("  ✅ Created statResults table");
        }
    }
    
    private void createChiSquaredResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chiSquaredResults (
                    id BIGINT PRIMARY KEY,
                    probeStatResultId BIGINT,
                    degree1 BIGINT,
                    degree2 BIGINT,
                    value DOUBLE,
                    pValue DOUBLE
                )
            """);
            System.out.println("  ✅ Created chiSquaredResults table");
        }
    }
    
    private void createCategoryAnalysisResultsSetsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
            System.out.println("  ✅ Created categoryAnalysisResultsSets table");
        }
    }
    
    private void createCategoryIdentifiersTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryIdentifiers (
                    id VARCHAR PRIMARY KEY,
                    title VARCHAR,
                    modelType VARCHAR,
                    goLevel VARCHAR
                )
            """);
            System.out.println("  ✅ Created categoryIdentifiers table");
        }
    }
    
    private void createUmapReferencesTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS umapReferences (
                    id VARCHAR PRIMARY KEY,
                    categoryIdentifierId VARCHAR,
                    umap1 DOUBLE,
                    umap2 DOUBLE,
                    clusterId VARCHAR,
                    goTerm VARCHAR,
                    source VARCHAR,
                    createdAt TIMESTAMP
                )
            """);
            System.out.println("  ✅ Created umapReferences table");
        }
    }
    
    private void createCategoryAnalysisResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categoryAnalysisResults (
                    id BIGINT PRIMARY KEY,
                    categoryAnalysisResultsId BIGINT,
                    categoryIdentifierId VARCHAR,
                    modelType VARCHAR,
                    geneAllCount BIGINT,
                    geneAllCountFromExperiment BIGINT,
                    geneCountSignificantANOVA BIGINT,
                    percentage DOUBLE,
                    genesWithBMDLessEqualHighDose BIGINT,
                    genesWithBMDpValueGreaterEqualValue BIGINT,
                    genesWithBMDRsquaredValueGreaterEqualValue BIGINT,
                    genesWithBMDBMDLRatioBelowValue BIGINT,
                    genesWithBMDUBMDLRatioBelowValue BIGINT,
                    genesWithBMDUBMDRatioBelowValue BIGINT,
                    genesWithNFoldBelowLowPostiveDoseValue BIGINT,
                    genesWithFoldChangeAboveValue BIGINT,
                    genesWithPrefilterPValueAboveValue BIGINT,
                    genesWithPrefilterAdjustedPValueAboveValue BIGINT,
                    genesNotStepFunction BIGINT,
                    genesNotStepFunctionWithBMDLower BIGINT,
                    genesNotAdverseDirection BIGINT,
                    genesThatPassedAllFilters BIGINT,
                    fishersA BIGINT,
                    fishersB BIGINT,
                    fishersC BIGINT,
                    fishersD BIGINT,
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
                    overallDirection BIGINT,
                    meanFoldChange DOUBLE,
                    bmdFifthPercentile DOUBLE,
                    bmdlFifthPercentile DOUBLE,
                    bmduFifthPercentile DOUBLE,
                    bmdTenthPercentile DOUBLE,
                    bmdlTenthPercentile DOUBLE,
                    bmduTenthPercentile DOUBLE,
                    fifthPercentileIndex BIGINT,
                    bmdFifthPercentileTotalGenes BIGINT,
                    tenthPercentileIndex BIGINT,
                    bmdTenthPercentileTotalGenes BIGINT,
                    bmdlFifthPercentileTotalGenes BIGINT,
                    bmdlTenthPercentileTotalGenes BIGINT,
                    bmduFifthPercentileTotalGenes BIGINT,
                    bmduTenthPercentileTotalGenes BIGINT,
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
                    statResultCounts VARCHAR,
                    ivive VARCHAR,
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
            System.out.println("  ✅ Created categoryAnalysisResults table");
        }
    }
    
    private void createReferenceGeneProbeStatResultsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS referenceGeneProbeStatResults (
                    id BIGINT PRIMARY KEY,
                    categoryAnalysisResultId BIGINT,
                    referenceGeneId VARCHAR,
                    adverseDirection VARCHAR,
                    conflictMinCorrelation DOUBLE
                )
            """);
            System.out.println("  ✅ Created referenceGeneProbeStatResults table");
        }
    }
    
    private void createRefGeneProbeStatProbeStatJoinTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS refGeneProbeStat_probeStat_join (
                    id BIGINT PRIMARY KEY,
                    refGeneProbeStatResultId BIGINT,
                    probeStatResultId BIGINT
                )
            """);
            System.out.println("  ✅ Created refGeneProbeStat_probeStat_join table");
        }
    }
    
    private void createMetaTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS meta (
                    key VARCHAR PRIMARY KEY,
                    value BIGINT
                )
            """);
            stmt.execute("INSERT OR IGNORE INTO meta (key, value) VALUES ('schema_version', 1)");
            System.out.println("  ✅ Created meta table (WASM compatibility)");
        }
    }
    
    /**
     * Get the list of all table names in the correct dependency order
     */
    public static List<String> getTableNames() {
        return Arrays.asList(
            "datasets", "groups", "chips", "probes", "referenceGenes",
            "doseResponseExperiments", "doseGroups", "treatments", "probeResponses",
            "prefilterResultSets", "bmdResults", "prefilterResults", 
            "referenceGeneAnnotations", "probeStatResults", "statResults",
            "chiSquaredResults", "categoryAnalysisResultsSets", "categoryIdentifiers",
            "umapReferences", "categoryAnalysisResults", "referenceGeneProbeStatResults",
            "refGeneProbeStat_probeStat_join", "meta"
        );
    }

    /**
     * Creates compound indexes for optimal query performance.
     * These indexes match the compound indexes defined in the TypeScript unified schema.
     */
    private void createCompoundIndexes() throws SQLException {
        System.out.println("Creating compound indexes for optimal performance...");
        
        try (Statement stmt = connection.createStatement()) {
            // Category Analysis Results Sets compound indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResultsSets_bmdResultId_organ_species ON categoryAnalysisResultsSets (bmdResultId, organ, species)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResultsSets_datasetId_dataType_platform ON categoryAnalysisResultsSets (datasetId, dataType, platform)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResultsSets_sex_organ_species ON categoryAnalysisResultsSets (sex, organ, species)");
            
            // Category Analysis Results compound indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResults_categoryAnalysisResultsId_categoryIdentifierId ON categoryAnalysisResults (categoryAnalysisResultsId, categoryIdentifierId)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResults_categoryIdentifierId_modelType ON categoryAnalysisResults (categoryIdentifierId, modelType)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResults_percentage_geneAllCount_genesThatPassedAllFilters ON categoryAnalysisResults (percentage, geneAllCount, genesThatPassedAllFilters)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryAnalysisResults_modelType_geneAllCount ON categoryAnalysisResults (modelType, geneAllCount)");
            
            // Category Identifiers compound indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryIdentifiers_modelType_goLevel ON categoryIdentifiers (modelType, goLevel)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_categoryIdentifiers_title_modelType ON categoryIdentifiers (title, modelType)");
            
            // BMD Results compound indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_bmdResults_doseResponseExperimentId_datasetId ON bmdResults (doseResponseExperimentId, datasetId)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_bmdResults_organ_species_dataType ON bmdResults (organ, species, dataType)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_bmdResults_bmdMethod_datasetId ON bmdResults (bmdMethod, datasetId)");
            
            // Dose Response Experiments compound indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_doseResponseExperiments_sex_organ_species ON doseResponseExperiments (sex, organ, species)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_doseResponseExperiments_dataType_platform ON doseResponseExperiments (dataType, platform)");
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_doseResponseExperiments_chipId_species ON doseResponseExperiments (chipId, species)");
            
            // Reference Gene Annotations compound index (already exists but ensuring it's there)
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_referenceGeneAnnotations_doseResponseExperimentId_probeId_referenceGeneId ON referenceGeneAnnotations (doseResponseExperimentId, probeId, referenceGeneId)");
            
            // UMAP References compound index
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_umapReferences_categoryIdentifierId_clusterId ON umapReferences (categoryIdentifierId, clusterId)");
            
            // RefGeneProbeStat Join compound index
            stmt.execute("CREATE INDEX IF NOT EXISTS cidx_refGeneProbeStat_probeStat_join_refGeneProbeStatResultId_probeStatResultId ON refGeneProbeStat_probeStat_join (refGeneProbeStatResultId, probeStatResultId)");
            
            System.out.println("  ✅ Created compound indexes for optimal query performance");
        }
    }
}
