-- Category Analysis Results Query with Dose Information
-- This query provides comprehensive category analysis results with metadata and dose information
-- for plotting dose vs ranked BMD values

WITH category_analysis_data AS (
  SELECT
    -- Set-level information
    cars.id as set_id,
    cars.name as set_name,
    cars.bmdResultId,
    -- Add the new metadata fields from categoryAnalysisResultsSets
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
),
dose_groups_data AS (
  SELECT
    dre.id as experiment_id,
    json_group_array(
      json_object(
        'id', dg.id,
        'doseResponseExperimentId', dg.doseResponseExperimentId,
        'dose', dg.dose,
        'count', dg.count,
        'responseMean', dg.responseMean
      )
    ) as doseGroups
  FROM doseResponseExperiments dre
  JOIN doseGroups dg ON dre.id = dg.doseResponseExperimentId
  GROUP BY dre.id
),
treatments_data AS (
  SELECT
    dre.id as experiment_id,
    json_group_array(
      json_object(
        'id', t.id,
        'doseResponseExperimentId', t.doseResponseExperimentId,
        'name', t.name,
        'dose', t.dose
      )
    ) as treatments
  FROM doseResponseExperiments dre
  JOIN treatments t ON dre.id = t.doseResponseExperimentId
  GROUP BY dre.id
)
SELECT
  cad.set_name,
  cad.set_id,
  cad.bmdResultId,
  -- Add the new metadata fields to the main result
  cad.sex,
  cad.organ,
  cad.species,
  cad.dataType,
  cad.platform,
  -- Results array
  json_group_array(
    json_object(
      'categoryIdentifierId', cad.categoryIdentifierId,
      'percentage', cad.percentage,
      'geneAllCount', cad.geneAllCount,
      'genesThatPassedAllFilters', cad.genesThatPassedAllFilters,
      'bmdFifthPercentileTotalGenes', cad.bmdFifthPercentileTotalGenes,
      'categoryIdentifier', json_object(
        'id', cad.category_id,
        'title', cad.category_title,
        'modelType', cad.category_model_type
      )
    )
  ) as results,
  -- BMD result
  json_object(
    'id', cad.bmdResultId,
    'name', cad.bmd_result_name,
    'bmdMethod', cad.bmdMethod,
    'sex', cad.sex,
    'organ', cad.organ,
    'species', cad.species,
    'dataType', cad.dataType,
    'platform', cad.platform
  ) as bmdResult,
  -- Dose response experiment with nested arrays
  json_object(
    'id', cad.experiment_id,
    'name', cad.experiment_name,
    'chipId', cad.chipId,
    'logTransformation', cad.logTransformation,
    'columnHeader2', cad.columnHeader2,
    'chipCreationDate', cad.chipCreationDate,
    'doseGroups', COALESCE(dgd.doseGroups, '[]'),
    'treatments', COALESCE(td.treatments, '[]')
  ) as doseResponseExperiment
FROM category_analysis_data cad
LEFT JOIN dose_groups_data dgd ON cad.experiment_id = dgd.experiment_id
LEFT JOIN treatments_data td ON cad.experiment_id = td.experiment_id
GROUP BY
  cad.set_name,
  cad.set_id,
  cad.bmdResultId,
  -- Add the new metadata fields to GROUP BY
  cad.sex,
  cad.organ,
  cad.species,
  cad.dataType,
  cad.platform,
  cad.bmd_result_name,
  cad.bmdMethod,
  cad.wAUC,
  cad.logwAUC,
  cad.experiment_id,
  cad.experiment_name,
  cad.chipId,
  cad.logTransformation,
  cad.columnHeader2,
  cad.chipCreationDate,
  dgd.doseGroups,
  td.treatments
LIMIT 1;