    // Continue from DuckDBExportServiceV6.java - remaining export methods

    private void exportDoseResponseExperiments(BMDProject project, String inputFilename) throws SQLException {
        if (project.getDoseResponseExperiments() == null) return;

        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO doseResponseExperiments
            (id, name, chipId, logTransformation, columnHeader2, chipCreationDate,
             sex, organ, species, dataType, platform)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

            for (DoseResponseExperiment dre : project.getDoseResponseExperiments()) {
                long dreId = getNextId("doseResponseExperiments");
                putObjectId(dre, dreId);

                // Parse metadata from experiment name and filename
                Map<String, String> metadata = parseMetadata(inputFilename, dre.getName());

                stmt.setLong(1, dreId);
                stmt.setString(2, dre.getName());
                stmt.setObject(3, dre.getChip() != null ? getObjectId(dre.getChip()) : null);
                stmt.setString(4, dre.getLogTransformation() != null ? dre.getLogTransformation().toString() : null);

                // Handle columnHeader2 as JSON
                String columnHeader2Json = null;
                if (dre.getColumnHeader2() != null && !dre.getColumnHeader2().isEmpty()) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < dre.getColumnHeader2().size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append("\"").append(dre.getColumnHeader2().get(i)).append("\"");
                    }
                    sb.append("]");
                    columnHeader2Json = sb.toString();
                }
                stmt.setString(5, columnHeader2Json);
                stmt.setString(6, dre.getChipCreationDate());

                // Add parsed metadata
                stmt.setString(7, metadata.get("sex"));
                stmt.setString(8, metadata.get("organ"));
                stmt.setString(9, metadata.get("species"));
                stmt.setString(10, metadata.get("dataType"));
                stmt.setString(11, metadata.get("platform"));

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void exportCategoryAnalysisResultsSets(BMDProject project, String inputFilename) throws SQLException {
        if (project.getCategoryAnalysisResults() == null) return;

        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO categoryAnalysisResultsSets
            (id, name, sex, organ, species, dataType, platform, bmdResultId, datasetId)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

            for (CategoryAnalysisResults car : project.getCategoryAnalysisResults()) {
                long carId = getNextId("categoryAnalysisResultsSets");
                putObjectId(car, carId);

                // Parse metadata from category analysis result set name and filename
                Map<String, String> metadata = parseMetadata(inputFilename, car.getName());

                stmt.setLong(1, carId);
                stmt.setString(2, car.getName());
                stmt.setString(3, metadata.get("sex"));
                stmt.setString(4, metadata.get("organ"));
                stmt.setString(5, metadata.get("species"));
                stmt.setString(6, metadata.get("dataType"));
                stmt.setString(7, metadata.get("platform"));

                // Link to BMD result if available
                String bmdResultIdStr = null;
                if (car instanceof GOAnalysisResults) {
                    GOAnalysisResults goResults = (GOAnalysisResults) car;
                    if (goResults.getBmdResult() != null) {
                        bmdResultIdStr = getObjectId(goResults.getBmdResult());
                    }
                } else if (car instanceof PathwayAnalysisResults) {
                    PathwayAnalysisResults pathResults = (PathwayAnalysisResults) car;
                    if (pathResults.getBmdResult() != null) {
                        bmdResultIdStr = getObjectId(pathResults.getBmdResult());
                    }
                } else if (car instanceof DefinedCategoryAnalysisResults) {
                    DefinedCategoryAnalysisResults defResults = (DefinedCategoryAnalysisResults) car;
                    if (defResults.getBmdResult() != null) {
                        bmdResultIdStr = getObjectId(defResults.getBmdResult());
                    }
                } else if (car instanceof GeneLevelAnalysisResults) {
                    GeneLevelAnalysisResults geneResults = (GeneLevelAnalysisResults) car;
                    if (geneResults.getBmdResult() != null) {
                        bmdResultIdStr = getObjectId(geneResults.getBmdResult());
                    }
                }

                stmt.setObject(8, bmdResultIdStr != null ? Long.parseLong(bmdResultIdStr) : null);
                stmt.setLong(9, 1); // Default dataset ID

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void exportBMDResults(BMDProject project, String inputFilename) throws SQLException {
        if (project.getBmdResults() == null) return;

        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO bmdResults
            (id, name, doseResponseExperimentId, bmdMethod, sex, organ, species, dataType, platform,
             wAUC, logwAUC, aucModels, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

            for (BMDResult bmdResult : project.getBmdResults()) {
                long bmdResultId = getNextId("bmdResults");
                putObjectId(bmdResult, bmdResultId);

                // Parse metadata from BMD result name and filename
                Map<String, String> metadata = parseMetadata(inputFilename, bmdResult.getName());

                stmt.setLong(1, bmdResultId);
                stmt.setString(2, bmdResult.getName());
                stmt.setObject(3, bmdResult.getDoseResponseExperiment() != null ?
                    Long.parseLong(getObjectId(bmdResult.getDoseResponseExperiment())) : null);
                stmt.setString(4, bmdResult.getAnalysisInfo() != null ?
                    bmdResult.getAnalysisInfo().size() + " models" : null);

                // Add parsed metadata
                stmt.setString(5, metadata.get("sex"));
                stmt.setString(6, metadata.get("organ"));
                stmt.setString(7, metadata.get("species"));
                stmt.setString(8, metadata.get("dataType"));
                stmt.setString(9, metadata.get("platform"));

                // Add analysis metrics if available
                stmt.setObject(10, bmdResult.getwAUC());
                stmt.setObject(11, bmdResult.getLogwAUC());

                // Handle AUC models as JSON
                String aucModelsJson = null;
                if (bmdResult.getAucModels() != null && !bmdResult.getAucModels().isEmpty()) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < bmdResult.getAucModels().size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append("\"").append(bmdResult.getAucModels().get(i)).append("\"");
                    }
                    sb.append("]");
                    aucModelsJson = sb.toString();
                }
                stmt.setString(12, aucModelsJson);
                stmt.setString(13, bmdResult.getNotes());

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void exportPrefilterResultSets(List<PrefilterResults> allPrefilters, String inputFilename) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO prefilterResultSets
            (id, name, sex, organ, species, dataType, platform, bmdResultId, prefilterType)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """)) {

            for (PrefilterResults pf : allPrefilters) {
                long pfId = getNextId("prefilterResultSets");
                putObjectId(pf, pfId);

                // Parse metadata from prefilter name and filename
                Map<String, String> metadata = parseMetadata(inputFilename, pf.getName());

                stmt.setLong(1, pfId);
                stmt.setString(2, pf.getName());
                stmt.setString(3, metadata.get("sex"));
                stmt.setString(4, metadata.get("organ"));
                stmt.setString(5, metadata.get("species"));
                stmt.setString(6, metadata.get("dataType"));
                stmt.setString(7, metadata.get("platform"));

                // Link to associated BMD result if available
                String bmdResultIdStr = null;
                if (pf instanceof IStatModelProcessable) {
                    // Try to find linked BMD result - this would need project context
                    // For now, set to null - could be enhanced later
                }
                stmt.setObject(8, bmdResultIdStr != null ? Long.parseLong(bmdResultIdStr) : null);

                // Set prefilter type based on class
                String prefilterType = pf.getClass().getSimpleName();
                if (prefilterType.endsWith("Results")) {
                    prefilterType = prefilterType.substring(0, prefilterType.length() - "Results".length());
                }
                stmt.setString(9, prefilterType);

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // All the remaining methods from V4 with metadata parsing where applicable...
    // (copying the complete implementations from V4)

    private void exportChips(BMDProject project) throws SQLException {
        if (project.getDoseResponseExperiments() == null) return;

        Set<Chip> uniqueChips = new HashSet<>();
        for (DoseResponseExperiment dre : project.getDoseResponseExperiments()) {
            if (dre.getChip() != null) {
                uniqueChips.add(dre.getChip());
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO chips (id, name, chipTypeId, species, geneIdentifier, creationDate, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """)) {

            for (Chip chip : uniqueChips) {
                long chipId = getNextId("chips");
                putObjectId(chip, chipId);

                stmt.setLong(1, chipId);
                stmt.setString(2, chip.getName());
                stmt.setString(3, chip.getChipTypeId());
                stmt.setString(4, chip.getSpecies());
                stmt.setString(5, chip.getGeneIdentifier());
                stmt.setString(6, chip.getCreationDate());
                stmt.setString(7, chip.getNotes());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Continue with all other export methods from V4...
    // [Rest of the methods would be copied from V4 with minimal changes]
}