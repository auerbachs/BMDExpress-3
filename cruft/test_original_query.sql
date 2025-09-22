-- Test the original comprehensive query with the complete schema
WITH category_analysis_data AS (
  SELECT
    -- Set-level information
    cars.id as set_id,
    cars.name as set_name,
    cars.bmdResultId,
    cars.sex,
    cars.organ,
    cars.species,
    cars.dataType,
    cars.platform,

    -- Result-level information
    car.id as result_id,
    car.categoryIdentifierId,
    car.modelType,
    car.geneAllCount,
    car.percentage,
    car.genesThatPassedAllFilters,
    car.bmdFifthPercentileTotalGenes,

    -- Category identifier information
    ci.id as category_id,
    ci.title as category_title,
    ci.modelType as category_model_type,

    -- BMD result information
    br.name as bmd_result_name,
    br.organ as bmd_organ,
    br.species as bmd_species,
    br.dataType as bmd_dataType,
    br.platform as bmd_platform,
    br.bmdMethod,
    br.wAUC,
    br.logwAUC,

    -- Dose response experiment information
    dre.id as experiment_id,
    dre.name as experiment_name,
    dre.chipId,
    dre.logTransformation,
    dre.columnHeader2,
    dre.chipCreationDate

  FROM categoryAnalysisResultsSets cars
  JOIN categoryAnalysisResults car ON cars.id = car.categoryAnalysisResultsId
  JOIN categoryIdentifiers ci ON car.categoryIdentifierId = ci.id
  JOIN bmdResults br ON cars.bmdResultId = br.id
  JOIN doseResponseExperiments dre ON br.doseResponseExperimentId = dre.id

  WHERE ci.modelType = 'GOCategoryIdentifier'
    -- Add your filters here - adjust the values as needed
    AND car.percentage >= 5
    AND car.geneAllCount BETWEEN 40 AND 500
    AND car.genesThatPassedAllFilters >= 3
)
SELECT
  COUNT(*) as matching_records,
  AVG(percentage) as avg_percentage,
  AVG(geneAllCount) as avg_gene_count,
  AVG(genesThatPassedAllFilters) as avg_genes_passed
FROM category_analysis_data;