package com.sciome.bmdexpress2.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisDataSet;
import com.sciome.bmdexpress2.mvp.model.BMDExpressAnalysisRow;
import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.CombinedDataSet;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.chip.ChipInfo;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.probe.Probe;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.refgene.EntrezGene;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGene;
import com.sciome.bmdexpress2.mvp.model.refgene.ReferenceGeneAnnotation;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.HillResult;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponse;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponseValues;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IProjectNavigationService;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpress2.shared.eventbus.project.ShowErrorEvent;
import com.sciome.bmdexpress2.util.annotation.FileAnnotation;
import com.sciome.filter.DataFilter;
import com.sciome.filter.DataFilterPack;
import com.sciome.filter.DataFilterType;

import javafx.collections.transformation.FilteredList;

public class ProjectNavigationService implements IProjectNavigationService
{

	private final int MAX_FILES_FOR_MULTI_EXPORT = 10;

	@Override
	@SuppressWarnings("unchecked")
	public void assignArrayAnnotations(ChipInfo chipInfo, List<DoseResponseExperiment> experiments,
			FileAnnotation fileAnnotation)
	{

		// set up the analysis information Export All Modeled
		for (DoseResponseExperiment doseResponseExperiment : experiments)
		{
			AnalysisInfo analysisInfo = new AnalysisInfo();
			List<String> notes = new ArrayList<>();

			if (chipInfo == null)
			{
				notes.add("Chip: Generic");
				chipInfo = new ChipInfo();
				chipInfo.setName("Generic");
				chipInfo.setSpecies("Generic");
				chipInfo.setProvider("Generic");
				chipInfo.setId("Generic");

			}
			else
			{
				notes.add("Chip: " + chipInfo.getGeoName());
				notes.add("Provider: " + chipInfo.getProvider());

			}

			analysisInfo.setNotes(notes);
			doseResponseExperiment.setAnalysisInfo(analysisInfo);

			doseResponseExperiment.setChip(chipInfo);

			// try to avoid storing duplicate genes.
			Map<String, ReferenceGene> refCache = new HashMap<>();
			List<ReferenceGeneAnnotation> referenceGeneAnnotations = new ArrayList<>();
			// if there is no chip selected, the set it as Generic and load empty
			// referencegeneannotation DateFormat
			if (chipInfo.getName().equals("Generic"))
			{
				doseResponseExperiment.setReferenceGeneAnnotations(referenceGeneAnnotations);
				continue;
			}
			fileAnnotation.setChip(chipInfo.getGeoID());
			fileAnnotation.arrayProbesGenes();
			fileAnnotation.arrayGenesSymbols();

			doseResponseExperiment.setChipCreationDate(fileAnnotation.timeStamp);
			Date date = new Date(fileAnnotation.timeStamp);
			SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy");

			notes.add("Chip File Creation Date: " + df2.format(date));

			notes.add("Log Transformation: " + doseResponseExperiment.getLogTransformation());
			notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
			notes.add("Timestamp: " + BMDExpressProperties.getInstance().getTimeStamp());
			fileAnnotation.getGene2ProbeHash();

			Hashtable<String, Vector> probesToGene = fileAnnotation.getProbe2GeneHash();
			Hashtable<String, String> geneSymbolHash = fileAnnotation.getGene2SymbolHash();

			try
			{

				// let's create referenceGeneAnnotations
				for (ProbeResponse probeResponse : doseResponseExperiment.getProbeResponses())
				{
					Probe probe = probeResponse.getProbe();
					Vector<String> genes = probesToGene.get(probe.getId());
					ReferenceGeneAnnotation referenceGeneAnnotation = new ReferenceGeneAnnotation();
					List<ReferenceGene> referenceGenes = new ArrayList<>();
					if (genes == null)
						continue;
					for (String gene : genes)
					{

						ReferenceGene refGene = refCache.get(gene);
						if (refGene == null)
						{
							refGene = new EntrezGene();
							refGene.setId(gene);
							refGene.setGeneSymbol(geneSymbolHash.get(gene));
							refCache.put(gene, refGene);
						}
						referenceGenes.add(refGene);
					}
					referenceGeneAnnotation.setReferenceGenes(referenceGenes);
					referenceGeneAnnotation.setProbe(probe);

					referenceGeneAnnotations.add(referenceGeneAnnotation);
				}

				doseResponseExperiment.setReferenceGeneAnnotations(referenceGeneAnnotations);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	@Override
	public String exportMultipleFiles(Map<String, Set<BMDExpressAnalysisDataSet>> header2Rows,
			File selectedFile, boolean getParentNotes)
	{
		if (header2Rows.keySet().size() > MAX_FILES_FOR_MULTI_EXPORT)
		{
			BMDExpressEventBus.getInstance().post(new ShowErrorEvent(
					"There are too many distinct data sets being created due to varying column headers.  There are "
							+ header2Rows.keySet().size()
							+ " files to be created but there can only be a maximum of "
							+ MAX_FILES_FOR_MULTI_EXPORT
							+ ".  Please reduce the number of distinct datasets that you wish to export."));
			return "";
		}
		String filesCreateString = "The following file was created: ";
		if (header2Rows.keySet().size() > 1)
			filesCreateString = "The following files were created (please be aware the that multiple files were generated due to varying column headers : ";

		String fileName = selectedFile.getAbsolutePath();
		String fileNameWOExtension = fileName.replaceAll("\\.txt$", "");
		List<String> filesThatWereCreated = new ArrayList<>();
		int i = 0;
		for (String key : header2Rows.keySet())
		{
			BufferedWriter writer = null;
			i++;
			try
			{
				// if there are datasets with multiple headers, then we need to create separate files for each
				if (header2Rows.keySet().size() > 1)
					selectedFile = new File(fileNameWOExtension + "-" + i + ".txt");
				writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
				Set<BMDExpressAnalysisDataSet> dataSets = header2Rows.get(key);
				filesThatWereCreated.add(selectedFile.getName());
				boolean started = false;
				for (BMDExpressAnalysisDataSet dataSet : dataSets)
				{
					if (dataSet instanceof BMDExpressAnalysisDataSet)
					{
						if (!started) // this will only allow the unique header to be written once.
						{
							// this ensures the row data is filled.
							List<String> header = dataSet.getColumnHeader();
							// write the type of data being exported.
							// write the header.
							writer.write("Analysis\t");
							writer.write(String.join("\t", header) + "\n");
						}
						writer.write(getRowsToWrite(dataSet.getAnalysisRows(), dataSet.getName()));
					}
					else if (dataSet instanceof DoseResponseExperiment)
					{
						writer.write(getExperimentToWrite((DoseResponseExperiment) dataSet, true));
					}
					started = true;
				}
				writer.close();

			}
			catch (IOException e)
			{
				BMDExpressEventBus.getInstance().post(new ShowErrorEvent(
						"There are too many distinct data sets being created due to varying column headers.  There are "
								+ header2Rows.keySet().size()
								+ " files to be created but there can only be a maximum of "
								+ MAX_FILES_FOR_MULTI_EXPORT
								+ ".  Please reduce the number of distinct datasets that you wish to export."));
				e.printStackTrace();
			}

		}
		filesCreateString += String.join(",", filesThatWereCreated);
		return filesCreateString;
	}

	@Override
	public void exportBMDExpressAnalysisDataSet(BMDExpressAnalysisDataSet bmdResults, File selectedFile,
			boolean getParentNotes)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
			for (AnalysisInfo analysisInfo : bmdResults.getAnalysisInfo(getParentNotes))
			{
				writer.write(String.join("\n", analysisInfo.getNotes()));
				writer.write("\n\n");
			}
			writer.write(String.join("\t", bmdResults.getColumnHeader()) + "\n");
			writer.write(getRowsToWrite(bmdResults.getAnalysisRows(), null));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void exportFilteredResults(BMDExpressAnalysisDataSet bmdResults,
			FilteredList<BMDExpressAnalysisRow> filteredResults, File selectedFile, DataFilterPack pack,
			boolean getParentNotes)
	{
		StringBuilder filterInformation = new StringBuilder();
		filterInformation.append("Filter information: \n");
		for (DataFilter filter : pack.getDataFilters())
		{
			if (filter.getDataFilterType().equals(DataFilterType.CONTAINS)
					|| filter.getDataFilterType().equals(DataFilterType.BETWEEN))
				filterInformation.append(filter.toString() + "\n");
			else
				filterInformation.append(filter.getKey() + "  " + filter.getDataFilterType().name() + " "
						+ filter.getValues().get(0) + "\n");
		}
		filterInformation.append("\n");

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
			writer.write(filterInformation.toString());

			for (AnalysisInfo analysisInfo : bmdResults.getAnalysisInfo(getParentNotes))
				writer.write(String.join("\n", analysisInfo.getNotes()) + "\n\n");

			writer.write(String.join("\t", bmdResults.getColumnHeader()) + "\n");
			writer.write(getRowsToWrite(filteredResults, null));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	// Export BMD Analysis Data

	@Override
	public void exportDoseResponseExperiment(DoseResponseExperiment doseResponseExperiment, File selectedFile,
			boolean getParentNotes)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
			for (AnalysisInfo analysisInfo : doseResponseExperiment.getAnalysisInfo(getParentNotes))
			{
				writer.write(String.join("\n", analysisInfo.getNotes()));
				writer.write("\n\n");
			}
			writer.write(getExperimentToWrite(doseResponseExperiment, false));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void exportBMDResultBestModel(BMDResult bmdResults, File selectedFile, boolean getParentNotes)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
			for (AnalysisInfo analysisInfo : bmdResults.getAnalysisInfo(getParentNotes))
			{
				writer.write(String.join("\n", analysisInfo.getNotes()));
				writer.write("\n\n");
			}

			boolean hasHill = false;
			for (ProbeStatResult result : bmdResults.getProbeStatResults())
			{
				if (result.getBestStatResult() != null && result.getBestStatResult() instanceof HillResult)
				{
					hasHill = true;
					break;
				}
			}

			List<String> columnHeader = new ArrayList<>();
			columnHeader.add("Probe Id");
			columnHeader.add("BMDS Model");
			columnHeader.add("Genes");
			columnHeader.add("Gene Symbols");
			columnHeader.add(BMDResult.BEST_MODEL);
			columnHeader.add(BMDResult.BEST_BMD);
			columnHeader.add(BMDResult.BEST_BMDL);
			columnHeader.add(BMDResult.BEST_BMDU);
			columnHeader.add(BMDResult.BEST_FITPVALUE);
			columnHeader.add(BMDResult.BEST_LOGLIKLIHOOD);
			columnHeader.add(BMDResult.BEST_AIC);
			columnHeader.add(BMDResult.BEST_ADVERSE_DIRECTION);
			columnHeader.add(BMDResult.BEST_BMD_BMDL_RATIO);
			columnHeader.add(BMDResult.BEST_BMDU_BMDL_RATIO);
			columnHeader.add(BMDResult.BEST_BMDU_BMD_RATIO);
			columnHeader.add(BMDResult.BEST_RSQUARED);
			columnHeader.add(BMDResult.BEST_ISSTEPFUNCTION);
			columnHeader.add(BMDResult.BEST_ISSTEPFUNCTION_WITH_BMD_LESS_THAN_LOWEST);
			writer.write(String.join("\t", columnHeader));
			// if (hasHill)
			// writer.write("\tFlagged Hill");
			writer.write("\n");
			for (ProbeStatResult result : bmdResults.getProbeStatResults())
			{

				List<String> row = new ArrayList<>();

				row.add(result.getProbeResponse().getProbe().getId());

				if (result.getBestStatResult() != null)
				{
					row.add(result.getBestStatResult().toString());
				}
				else
				{
					row.add("none");
				}
				row.add(result.getGenes());
				row.add(result.getGeneSymbols());

				if (result.getBestStatResult() == null)
				{
					row.add("none");
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(Double.NaN));
					row.add(String.valueOf(false));
					row.add(String.valueOf(false));
				}
				else
				{
					row.add(String.valueOf(result.getBestStatResult().toString()));
					row.add(String.valueOf(result.getBestStatResult().getBMD()));
					row.add(String.valueOf(result.getBestStatResult().getBMDL()));
					row.add(String.valueOf(result.getBestStatResult().getBMDU()));
					row.add(String.valueOf(result.getBestStatResult().getFitPValue()));
					row.add(String.valueOf(result.getBestStatResult().getFitLogLikelihood()));
					row.add(String.valueOf(result.getBestStatResult().getAIC()));
					row.add(String.valueOf(result.getBestStatResult().getAdverseDirection()));
					row.add(String.valueOf(result.getBestStatResult().getBMDdiffBMDL()));
					row.add(String.valueOf(result.getBestStatResult().getBMDUdiffBMDL()));
					row.add(String.valueOf(result.getBestStatResult().getBMDUdiffBMD()));
					row.add(String.valueOf(result.getBestStatResult().getrSquared()));
					row.add(String.valueOf(result.getBestStatResult().getIsStepFunction()));
					row.add(String.valueOf(result.getBestStatResult().isStepWithBMDLessLowest()));
				}

				writer.write(String.join("\t", row));
				writer.write("\n");

			}
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// Export All Modeled

	@Override
	public void exportModelParameters(BMDProject bmdProject)
	{
		File selectedFile = new File("/tmp/modelParams.txt");
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);

			for (BMDResult bmdResults : bmdProject.getbMDResult())
			{

				for (ProbeStatResult result : bmdResults.getProbeStatResults())
				{
					for (StatResult statResult : result.getStatResults())
					{
						writer.write(bmdResults.getName() + "\t"
								+ result.getProbeResponse().getProbe().getId() + "\t"
								+ result.getBestStatResult().toString() + "\t" + statResult.toString());
						double[] params = statResult.getCurveParameters();

						for (int i = 0; i < params.length; i++)
						{
							writer.write("\t" + params[i]);
						}

						for (String pname : statResult.getParametersNames())
						{
							writer.write("\t" + pname);
						}

						writer.write("\n");
					}
				}

			}

			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Object[][] showProbeToGeneMatrix(DoseResponseExperiment doseResponseExperiment)
	{

		Object[][] matrixData = null;
		if (doseResponseExperiment.getReferenceGeneAnnotations() != null)
		{
			matrixData = new Object[doseResponseExperiment.getReferenceGeneAnnotations().size()][];

			int i = 0;
			for (ReferenceGeneAnnotation refGeneAnnotation : doseResponseExperiment
					.getReferenceGeneAnnotations())
			{
				StringBuilder symbolBuilder = new StringBuilder();
				StringBuilder geneBuilder = new StringBuilder();
				String probeId = refGeneAnnotation.getProbe().getId();
				for (ReferenceGene refGene : refGeneAnnotation.getReferenceGenes())
				{
					if (symbolBuilder.length() > 0)
					{
						symbolBuilder.append(";");
						geneBuilder.append(";");
					}
					symbolBuilder.append(refGene.getGeneSymbol());
					geneBuilder.append(refGene.getId());
				}
				Object[] rowData = { probeId, geneBuilder.toString(), symbolBuilder.toString() };
				matrixData[i] = rowData;
				i++;
			}
		}
		else
			matrixData = new Object[0][];

		return matrixData;
	}

	@Override
	public Object[][] showGenesToProbeMatrix(DoseResponseExperiment doseResponseExperiment)
	{

		Object[][] matrixData = null;
		if (doseResponseExperiment.getReferenceGeneAnnotations() != null)
		{
			Map<ReferenceGene, List<String>> geneProbeMap = new HashMap<>();

			for (ReferenceGeneAnnotation refGeneAnnotation : doseResponseExperiment
					.getReferenceGeneAnnotations())
			{
				for (ReferenceGene refGene : refGeneAnnotation.getReferenceGenes())
				{
					if (!geneProbeMap.containsKey(refGene))
					{
						geneProbeMap.put(refGene, new ArrayList<>());
					}
					geneProbeMap.get(refGene).add(refGeneAnnotation.getProbe().getId());
				}
			}
			matrixData = new Object[geneProbeMap.keySet().size()][];
			int i = 0;

			for (ReferenceGene refGeneKey : geneProbeMap.keySet())
			{
				Object rowData[] = { refGeneKey.getId(), refGeneKey.getGeneSymbol(),
						String.join(";", geneProbeMap.get(refGeneKey)) };
				matrixData[i] = rowData;
				i++;
			}
		}
		else
		{
			matrixData = new Object[0][];
		}
		return matrixData;
	}

	/**
	 * Creates a string using a list of rows from a data set
	 * 
	 * @param rows
	 *            The list of rows to write
	 * @param prependName
	 *            A name to prepend to each of the rows
	 * @return A string with the data from the rows
	 */
	private String getRowsToWrite(List<BMDExpressAnalysisRow> rows, String prependName)
	{
		StringBuffer sb = new StringBuffer();

		for (BMDExpressAnalysisRow result : rows)
		{
			if (prependName != null)
				sb.append(prependName + "\t");
			sb.append(joinRowData(result.getRow(), "\t") + "\n");
		}
		return sb.toString();
	}

	private String joinRowData(List<Object> datas, String delimiter)
	{
		StringBuffer bf = new StringBuffer();
		int i = 0;
		if (datas == null)
		{
			return "";
		}
		for (Object data : datas)
		{
			if (data != null)
			{
				bf.append(data);
			}

			if (i < datas.size())
			{
				bf.append(delimiter);
			}
		}

		return bf.toString();
	}

	private String getExperimentToWrite(DoseResponseExperiment doseResponseExperiment, boolean prependname)
	{
		StringBuffer sb = new StringBuffer();
		List<String> row = new ArrayList<>();
		row.add("Something");

		for (Treatment treatment : doseResponseExperiment.getTreatments())
		{
			row.add(treatment.getName());
		}
		if (prependname)
		{
			sb.append(doseResponseExperiment.getName() + "\t");
		}
		sb.append(String.join("\t", row) + "\n");
		row.clear();
		row.add("Doses");

		for (Treatment treatment : doseResponseExperiment.getTreatments())
		{
			row.add(String.valueOf(treatment.getDose()));
		}
		sb.append(String.join("\t", row) + "\n");

		for (ProbeResponse result : doseResponseExperiment.getProbeResponses())
		{
			row.clear();
			row.add(result.getProbe().getId());
			for (Float response : result.getResponses())
			{
				row.add(String.valueOf(response));
			}
			if (prependname)
			{
				sb.append(doseResponseExperiment.getName() + "\t");
			}
			sb.append(String.join("\t", row) + "\n");
		}

		return sb.toString();
	}

	@Override
	public void exportFilteredModeledResponses(BMDExpressAnalysisDataSet bmdAnalysisDataSet,
			FilteredList<BMDExpressAnalysisRow> filteredData, File selectedFile,
			DataFilterPack filterDataPack, boolean getParentNotes)
	{

		StringBuilder filterInformation = new StringBuilder();
		filterInformation.append("Filter information: \n");
		for (DataFilter filter : filterDataPack.getDataFilters())
		{
			if (filter.getDataFilterType().equals(DataFilterType.CONTAINS)
					|| filter.getDataFilterType().equals(DataFilterType.BETWEEN))
				filterInformation.append(filter.toString() + "\n");
			else
				filterInformation.append(filter.getKey() + "  " + filter.getDataFilterType().name() + " "
						+ filter.getValues().get(0) + "\n");
		}
		filterInformation.append("\n");

		exportModledResponsesFromDataView(bmdAnalysisDataSet, filteredData, selectedFile, getParentNotes);

	}

	@Override
	public void exportBMDExpressAnalysisModeledResponses(BMDExpressAnalysisDataSet bmdAnalysisDataSet,
			File selectedFile, boolean getParentNotes)
	{
		exportModledResponsesFromDataView(bmdAnalysisDataSet, bmdAnalysisDataSet.getAnalysisRows(),
				selectedFile, getParentNotes);
	}

	private void exportModledResponsesFromDataView(BMDExpressAnalysisDataSet bmdAnalysisDataSet,
			List<BMDExpressAnalysisRow> rowsOfData, File selectedFile, boolean getParentNotes)
	{
		List<BMDResult> bmdResults = new ArrayList<>();
		boolean isCombined = false;

		if (bmdAnalysisDataSet instanceof CombinedDataSet)
		{
			for (Object obj : ((CombinedDataSet) bmdAnalysisDataSet).getObjects())
				bmdResults.add((BMDResult) obj);
			isCombined = true;
		}
		else
			bmdResults.add((BMDResult) bmdAnalysisDataSet.getObject());

		Map<String, Set<String>> analysisToProbeSetMap = new HashMap<>();
		Map<String, List<ProbeStatResult>> analysisToProbeStatResultMap = new HashMap<>();
		Map<String, BMDResult> analysisToBMDResultResultMap = new HashMap<>();
		for (BMDResult result : bmdResults)
		{
			analysisToProbeSetMap.put(result.getName(),
					this.getProbeSetFromAnnotations(result.getDoseResponseExperiment()));
			analysisToProbeStatResultMap.put(result.getName(), new ArrayList<>());
			analysisToBMDResultResultMap.put(result.getName(), result);
		}

		if (isCombined)
		{
			for (BMDExpressAnalysisRow row : rowsOfData)
			{
				String analysis = row.getRow().get(0).toString();
				analysisToProbeStatResultMap.get(analysis).add((ProbeStatResult) row.getObject());
			}
		}
		else
		{
			for (BMDExpressAnalysisRow row : rowsOfData)
			{
				analysisToProbeStatResultMap.get(bmdResults.get(0).getName())
						.add((ProbeStatResult) row.getObject());
			}
		}

		BMDStatisticsService bss = new BMDStatisticsService();

		int index = 0;
		for (BMDResult bmdResult : bmdResults)
		{

			Set<String> probeSet = analysisToProbeSetMap.get(bmdResult.getName());
			ModeledResponse modeledResponse = bss.generateResponsesBetweenDoseGroups(bmdResult,
					analysisToProbeStatResultMap.get(bmdResult.getName()), 10, probeSet);

			String towrite = getModeledResponseRows(modeledResponse,
					(bmdResults.size() > 1) ? bmdResult.getName() : null);

			File theFile = selectedFile;
			if (index > 0) // use selected file
				theFile = new File(selectedFile.getAbsolutePath() + "." + index + ".txt");

			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter(theFile), 1024 * 2000);
				for (AnalysisInfo analysisInfo : bmdResult.getAnalysisInfo(getParentNotes))
				{
					writer.write(String.join("\n", analysisInfo.getNotes()));
					writer.write("\n\n");
				}
				writer.write(towrite);

				writer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			index++;
		}

	}

	@Override
	public void exportBMDResultModeledResponses(BMDResult bmdResults, File selectedFile,
			boolean getParentNotes)
	{
		try
		{
			BMDStatisticsService bss = new BMDStatisticsService();
			BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile), 1024 * 2000);
			for (AnalysisInfo analysisInfo : bmdResults.getAnalysisInfo(getParentNotes))
			{
				writer.write(String.join("\n", analysisInfo.getNotes()));
				writer.write("\n\n");
			}
			Set<String> probeSet = getProbeSetFromAnnotations(bmdResults.getDoseResponseExperiment());
			ModeledResponse modeledResponse = bss.generateResponsesBetweenDoseGroups(bmdResults,
					bmdResults.getProbeStatResults(), 10, probeSet);
			writer.write(getModeledResponseRows(modeledResponse, null));

			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String getModeledResponseRows(ModeledResponse modeledResponse, String analysis)
	{

		StringBuilder sb = new StringBuilder();
		if (analysis != null)
			sb.append("Analysis").append("\t");
		sb.append(modeledResponse.getHeader().stream().collect(Collectors.joining("\t"))).append("\n");
		for (ModeledResponseValues result : modeledResponse.getValues())
		{

			if (analysis != null)
				sb.append(analysis).append("\t");
			sb.append(result.getProbeId()).append("\t").append(result.getModeledResponses().stream()
					.map(String::valueOf).collect(Collectors.joining("\t"))).append("\n");
		}

		return sb.toString();
	}

	private Set<String> getProbeSetFromAnnotations(DoseResponseExperiment doseexp)
	{
		FileAnnotation fileAnnotation = new FileAnnotation();
		fileAnnotation.readArraysInfo();
		fileAnnotation.setChip(doseexp.getChip().getGeoID());

		fileAnnotation.arrayProbesGenes();
		fileAnnotation.arrayGenesSymbols();
		fileAnnotation.getGene2ProbeHash();
		Set<String> probeSet = fileAnnotation.getAllProbes();
		if (probeSet == null)
			probeSet = new HashSet<>();

		return probeSet;

	}

}
