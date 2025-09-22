-- Category Analysis Results Query with Dose Information (Fixed)
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
    car.genesThatPassedAllFilters,
    car.fishersExactTwoTailPValue,
    car.bmdMean,
    car.bmdMedian,
    car.bmdlMean,
    car.bmdlMedian,

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

  WHERE ci.modelType = 'GOCategoryIdentifier'  -- Fixed: use the actual class name
    AND car.geneAllCount >= 50  -- More reasonable filter
    AND car.genesThatPassedAllFilters >= 3
    AND car.bmdMean IS NOT NULL  -- Only include results with BMD values
)
SELECT
  cad.set_name,
  cad.set_id,
  cad.bmdResultId,
  cad.sex,
  cad.organ,
  cad.species,
  cad.dataType,
  cad.platform,
  -- Results array (simplified for testing)
  json_group_array(
    json_object(
      'categoryIdentifierId', cad.categoryIdentifierId,
      'geneAllCount', cad.geneAllCount,
      'genesThatPassedAllFilters', cad.genesThatPassedAllFilters,
      'fishersExactTwoTailPValue', cad.fishersExactTwoTailPValue,
      'bmdMean', cad.bmdMean,
      'bmdMedian', cad.bmdMedian,
      'bmdlMean', cad.bmdlMean,
      'bmdlMedian', cad.bmdlMedian,
      'categoryIdentifier', json_object(
        'id', cad.category_id,
        'title', cad.category_title,
        'modelType', cad.category_model_type
      )
    )
  ) as results,
  -- BMD result (simplified)
  json_object(
    'id', cad.bmdResultId,
    'name', cad.bmd_result_name,
    'bmdMethod', cad.bmdMethod
  ) as bmdResult,
  -- Basic experiment info (without nested arrays for now)
  json_object(
    'id', cad.experiment_id,
    'name', cad.experiment_name,
    'chipId', cad.chipId,
    'logTransformation', cad.logTransformation
  ) as doseResponseExperiment
FROM category_analysis_data cad
GROUP BY
  cad.set_name,
  cad.set_id,
  cad.bmdResultId,
  cad.sex,
  cad.organ,
  cad.species,
  cad.dataType,
  cad.platform,
  cad.bmd_result_name,
  cad.bmdMethod,
  cad.experiment_id,
  cad.experiment_name,
  cad.chipId,
  cad.logTransformation
LIMIT 3;