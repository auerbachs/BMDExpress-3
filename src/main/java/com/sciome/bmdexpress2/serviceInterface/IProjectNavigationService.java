package com.sciome.bmdexpress2.serviceInterface;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisRow;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.chip.ChipInfo;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.util.annotation.FileAnnotation;
import com.sciome.filter.DataFilterPack;

import javafx.collections.transformation.FilteredList;

public interface IProjectNavigationService
{
	public void assignArrayAnnotations(ChipInfo chipInfo, List<DoseResponseExperiment> experiments,
			FileAnnotation fileAnnotation);

	public String exportMultipleFiles(Map<String, Set<BMDExpressAnalysisDataSet>> header2rows,
			File selectedFile, boolean getParentNotes);

	public void exportBMDExpressAnalysisDataSet(BMDExpressAnalysisDataSet bmdResults, File selectedFile,
			boolean getParentNotes);

	public void exportDoseResponseExperiment(DoseResponseExperiment doseResponseExperiment, File selectedFile,
			boolean getParentNotes);

	public void exportFilteredResults(BMDExpressAnalysisDataSet bmdResults,
			FilteredList<BMDExpressAnalysisRow> filteredResults, File selectedFile, DataFilterPack pack,
			boolean getParentNotes);

	public void exportBMDResultBestModel(BMDResult bmdResults, File selectedFile, boolean getParentNotes);

	public Object[][] showGenesToProbeMatrix(DoseResponseExperiment doseResponseExperiment);

	public Object[][] showProbeToGeneMatrix(DoseResponseExperiment doseResponseExperiment);

	public void exportModelParameters(BMDProject bmdProject);

	void exportBMDResultModeledResponses(BMDResult bmdResults, File selectedFile, boolean getParentNotes);

	public void exportFilteredModeledResponses(BMDExpressAnalysisDataSet bmdAnalysisDataSet,
			FilteredList<BMDExpressAnalysisRow> filteredData, File selectedFile,
			DataFilterPack filterDataPack, boolean getParentNotes);

	public void exportBMDExpressAnalysisModeledResponses(BMDExpressAnalysisDataSet bmdAnalysisDataSet,
			File selectedFile, boolean getParentNotes);
}
