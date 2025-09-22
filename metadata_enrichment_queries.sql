-- BMD Express Database Metadata Enrichment Queries
-- This script enriches the V4 database with parsed metadata from categoryAnalysisResultsSets names
-- Example name: P2_Perfluoro_3_methoxypropanoic_acid_Male_Lung-expression1_curvefitprefilter_foldfilter1.25_BMD_S1500_Plus_Rat_GO_BP_true_rsquared0.6_ratio10_conf0.5

-- =====================================================================
-- PHASE 1: Update categoryAnalysisResultsSets (primary source table)
-- =====================================================================

-- Update SEX in categoryAnalysisResultsSets
UPDATE categoryAnalysisResultsSets SET sex = 'Male' WHERE name LIKE '%_Male_%';
UPDATE categoryAnalysisResultsSets SET sex = 'Female' WHERE name LIKE '%_Female_%';

-- Update ORGAN in categoryAnalysisResultsSets
UPDATE categoryAnalysisResultsSets SET organ = 'Lung' WHERE name LIKE '%_Lung%';
UPDATE categoryAnalysisResultsSets SET organ = 'Heart' WHERE name LIKE '%_Heart%';
UPDATE categoryAnalysisResultsSets SET organ = 'Kidney' WHERE name LIKE '%_Kidney%';
UPDATE categoryAnalysisResultsSets SET organ = 'Spleen' WHERE name LIKE '%_Spleen%';
UPDATE categoryAnalysisResultsSets SET organ = 'Testis' WHERE name LIKE '%_Testis%';
UPDATE categoryAnalysisResultsSets SET organ = 'Ovary' WHERE name LIKE '%_Ovary%';
UPDATE categoryAnalysisResultsSets SET organ = 'Uterus' WHERE name LIKE '%_Uterus%';
UPDATE categoryAnalysisResultsSets SET organ = 'Liver' WHERE name LIKE '%_Liver%';
UPDATE categoryAnalysisResultsSets SET organ = 'Brain' WHERE name LIKE '%_Brain%';
UPDATE categoryAnalysisResultsSets SET organ = 'Thyroid' WHERE name LIKE '%_Thyroid%';
UPDATE categoryAnalysisResultsSets SET organ = 'Thymus' WHERE name LIKE '%_Thymus%';
UPDATE categoryAnalysisResultsSets SET organ = 'Adrenal' WHERE name LIKE '%_Adrenal%';

-- Update SPECIES in categoryAnalysisResultsSets
UPDATE categoryAnalysisResultsSets SET species = 'Rat' WHERE name LIKE '%_Rat_%';

-- Update PLATFORM in categoryAnalysisResultsSets
UPDATE categoryAnalysisResultsSets SET platform = 'S1500_Plus' WHERE name LIKE '%S1500_Plus%';

-- Update DATATYPE in categoryAnalysisResultsSets (based on filename - manual for now)
-- Note: This would need to be updated based on actual filename containing 'genomic' or 'clinical endpoint'
UPDATE categoryAnalysisResultsSets SET dataType = 'genomic' WHERE name IS NOT NULL;
-- UPDATE categoryAnalysisResultsSets SET dataType = 'clinical endpoint' WHERE <filename_condition>;

-- =====================================================================
-- PHASE 2: Propagate to bmdResults via categoryAnalysisResultsSets.bmdResultId
-- =====================================================================

-- Update SEX in bmdResults
UPDATE bmdResults
SET sex = cars.sex
FROM categoryAnalysisResultsSets cars
WHERE bmdResults.id = cars.bmdResultId AND cars.sex IS NOT NULL;

-- Update ORGAN in bmdResults
UPDATE bmdResults
SET organ = cars.organ
FROM categoryAnalysisResultsSets cars
WHERE bmdResults.id = cars.bmdResultId AND cars.organ IS NOT NULL;

-- Update SPECIES in bmdResults
UPDATE bmdResults
SET species = cars.species
FROM categoryAnalysisResultsSets cars
WHERE bmdResults.id = cars.bmdResultId AND cars.species IS NOT NULL;

-- Update PLATFORM in bmdResults
UPDATE bmdResults
SET platform = cars.platform
FROM categoryAnalysisResultsSets cars
WHERE bmdResults.id = cars.bmdResultId AND cars.platform IS NOT NULL;

-- Update DATATYPE in bmdResults
UPDATE bmdResults
SET dataType = cars.dataType
FROM categoryAnalysisResultsSets cars
WHERE bmdResults.id = cars.bmdResultId AND cars.dataType IS NOT NULL;

-- =====================================================================
-- PHASE 3: Propagate to doseResponseExperiments via bmdResults.doseResponseExperimentId
-- =====================================================================

-- Update SEX in doseResponseExperiments
UPDATE doseResponseExperiments
SET sex = br.sex
FROM bmdResults br
WHERE doseResponseExperiments.id = br.doseResponseExperimentId AND br.sex IS NOT NULL;

-- Update ORGAN in doseResponseExperiments
UPDATE doseResponseExperiments
SET organ = br.organ
FROM bmdResults br
WHERE doseResponseExperiments.id = br.doseResponseExperimentId AND br.organ IS NOT NULL;

-- Update SPECIES in doseResponseExperiments
UPDATE doseResponseExperiments
SET species = br.species
FROM bmdResults br
WHERE doseResponseExperiments.id = br.doseResponseExperimentId AND br.species IS NOT NULL;

-- Update PLATFORM in doseResponseExperiments
UPDATE doseResponseExperiments
SET platform = br.platform
FROM bmdResults br
WHERE doseResponseExperiments.id = br.doseResponseExperimentId AND br.platform IS NOT NULL;

-- Update DATATYPE in doseResponseExperiments
UPDATE doseResponseExperiments
SET dataType = br.dataType
FROM bmdResults br
WHERE doseResponseExperiments.id = br.doseResponseExperimentId AND br.dataType IS NOT NULL;

-- =====================================================================
-- PHASE 4: Propagate to prefilterResultSets via doseResponseExperiments relationship
-- =====================================================================
-- Note: Need to determine relationship between prefilterResultSets and doseResponseExperiments
-- This might be through prefilterResultSets.doseResponseExperimentId or similar

-- Update SEX in prefilterResultSets (assuming direct relationship)
UPDATE prefilterResultSets
SET sex = dre.sex
FROM doseResponseExperiments dre
WHERE prefilterResultSets.doseResponseExperimentId = dre.id AND dre.sex IS NOT NULL;

-- Update ORGAN in prefilterResultSets
UPDATE prefilterResultSets
SET organ = dre.organ
FROM doseResponseExperiments dre
WHERE prefilterResultSets.doseResponseExperimentId = dre.id AND dre.organ IS NOT NULL;

-- Update SPECIES in prefilterResultSets
UPDATE prefilterResultSets
SET species = dre.species
FROM doseResponseExperiments dre
WHERE prefilterResultSets.doseResponseExperimentId = dre.id AND dre.species IS NOT NULL;

-- Update PLATFORM in prefilterResultSets
UPDATE prefilterResultSets
SET platform = dre.platform
FROM doseResponseExperiments dre
WHERE prefilterResultSets.doseResponseExperimentId = dre.id AND dre.platform IS NOT NULL;

-- Update DATATYPE in prefilterResultSets
UPDATE prefilterResultSets
SET dataType = dre.dataType
FROM doseResponseExperiments dre
WHERE prefilterResultSets.doseResponseExperimentId = dre.id AND dre.dataType IS NOT NULL;

-- =====================================================================
-- PHASE 5: Update chips and datasets tables
-- =====================================================================

-- Update SPECIES in chips (from doseResponseExperiments via chipId)
UPDATE chips
SET species = dre.species
FROM doseResponseExperiments dre
WHERE chips.id = dre.chipId AND dre.species IS NOT NULL;

-- Update DATATYPE in datasets (from doseResponseExperiments via datasetId)
UPDATE datasets
SET dataType = dre.dataType
FROM doseResponseExperiments dre
WHERE datasets.id = dre.datasetId AND dre.dataType IS NOT NULL;

-- =====================================================================
-- VERIFICATION QUERIES
-- =====================================================================

-- Check results after enrichment
SELECT 'categoryAnalysisResultsSets' as table_name, sex, organ, species, platform, dataType, COUNT(*) as count
FROM categoryAnalysisResultsSets
WHERE sex IS NOT NULL OR organ IS NOT NULL OR species IS NOT NULL OR platform IS NOT NULL OR dataType IS NOT NULL
GROUP BY sex, organ, species, platform, dataType

UNION ALL

SELECT 'bmdResults' as table_name, sex, organ, species, platform, dataType, COUNT(*) as count
FROM bmdResults
WHERE sex IS NOT NULL OR organ IS NOT NULL OR species IS NOT NULL OR platform IS NOT NULL OR dataType IS NOT NULL
GROUP BY sex, organ, species, platform, dataType

UNION ALL

SELECT 'doseResponseExperiments' as table_name, sex, organ, species, platform, dataType, COUNT(*) as count
FROM doseResponseExperiments
WHERE sex IS NOT NULL OR organ IS NOT NULL OR species IS NOT NULL OR platform IS NOT NULL OR dataType IS NOT NULL
GROUP BY sex, organ, species, platform, dataType

ORDER BY table_name, count DESC;