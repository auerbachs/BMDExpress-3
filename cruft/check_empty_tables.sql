-- Check row counts for all tables
SELECT 'bmdResults' as table_name, COUNT(*) as row_count FROM bmdResults
UNION ALL
SELECT 'categoryAnalysisResults', COUNT(*) FROM categoryAnalysisResults
UNION ALL
SELECT 'categoryAnalysisResultsSets', COUNT(*) FROM categoryAnalysisResultsSets
UNION ALL
SELECT 'categoryIdentifiers', COUNT(*) FROM categoryIdentifiers
UNION ALL
SELECT 'chiSquaredResults', COUNT(*) FROM chiSquaredResults
UNION ALL
SELECT 'chips', COUNT(*) FROM chips
UNION ALL
SELECT 'datasets', COUNT(*) FROM datasets
UNION ALL
SELECT 'doseGroups', COUNT(*) FROM doseGroups
UNION ALL
SELECT 'doseResponseExperiments', COUNT(*) FROM doseResponseExperiments
UNION ALL
SELECT 'groups', COUNT(*) FROM groups
UNION ALL
SELECT 'prefilterResultSets', COUNT(*) FROM prefilterResultSets
UNION ALL
SELECT 'prefilterResults', COUNT(*) FROM prefilterResults
UNION ALL
SELECT 'probeResponses', COUNT(*) FROM probeResponses
UNION ALL
SELECT 'probeStatResults', COUNT(*) FROM probeStatResults
UNION ALL
SELECT 'probes', COUNT(*) FROM probes
UNION ALL
SELECT 'refGeneProbeStat_probeStat_join', COUNT(*) FROM refGeneProbeStat_probeStat_join
UNION ALL
SELECT 'referenceGeneAnnotations', COUNT(*) FROM referenceGeneAnnotations
UNION ALL
SELECT 'referenceGeneProbeStatResults', COUNT(*) FROM referenceGeneProbeStatResults
UNION ALL
SELECT 'referenceGenes', COUNT(*) FROM referenceGenes
UNION ALL
SELECT 'statResults', COUNT(*) FROM statResults
UNION ALL
SELECT 'treatments', COUNT(*) FROM treatments
UNION ALL
SELECT 'umapReferences', COUNT(*) FROM umapReferences
ORDER BY row_count, table_name;