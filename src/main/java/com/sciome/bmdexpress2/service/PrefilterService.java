package com.sciome.bmdexpress2.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.stat.inference.TTest;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.LogTransformationEnum;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResults;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResult;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IPrefilterService;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BESTMODEL_METHOD;
import com.sciome.bmdexpress2.util.bmds.BMD_METHOD;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.BestModelSelectionBMDLandBMDU;
import com.sciome.bmdexpress2.util.bmds.shared.BestPolyModelTestEnum;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;
import com.sciome.bmdexpress2.util.prefilter.FoldChange;
import com.sciome.bmdexpress2.util.prefilter.OneWayANOVAAnalysis;
import com.sciome.commons.interfaces.SimpleProgressUpdater;
import com.sciome.commons.math.MathUtil;
import com.sciome.commons.math.dunnetts.DunnettsTest;
import com.sciome.commons.math.oriogen.Origen_Data;
import com.sciome.commons.math.oriogen.OriogenTestResult;
import com.sciome.commons.math.oriogen.OriogenUtil;
import com.sciome.commons.math.williams.WilliamsTrendTestResult;
import com.sciome.commons.math.williams.WilliamsTrendTestUtil;

public class PrefilterService implements IPrefilterService
{
	private static final int DEFAULT_NUM_THREADS = 4;

	private WilliamsTrendTestUtil williamsUtil = new WilliamsTrendTestUtil();
	private OriogenUtil oriogenUtil = new OriogenUtil();
	private boolean cancel = false;
	private ExecutorService executor;

	/**
	 * Performs a william's trend analysis and returns the corresponding WilliamsTrendResult object
	 */
	@Override
	public WilliamsTrendResults williamsTrendAnalysis(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, boolean filterOutControlGenes, boolean useFoldFilter,
			double foldFilterValue, int numberOfPermutations, double loelPValue, double loelFoldChange,
			int numThreads, SimpleProgressUpdater updater, boolean tTest)
	{
		long startTime = System.currentTimeMillis();
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();

		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		notes.add("Williams Trend Test");
		notes.add("Data Source: " + processableData);
		notes.add("Work Source: " + processableData.getParentDataSetName());
		notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		notes.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());

		notes.add("Number of Permutations: " + String.valueOf(numberOfPermutations));

		double baseValue = 2.0;
		boolean isLogTransformation = true;
		if (processableData.getLogTransformation().equals(LogTransformationEnum.BASE10))
			baseValue = 10.0f;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NATURAL))
			baseValue = 2.718281828459045;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NONE))
			isLogTransformation = false;

		// get a list of williamsTrendResult
		List<ProbeResponse> responses = doseResponseExperiment.getProbeResponses();
		List<Treatment> treatments = doseResponseExperiment.getTreatments();
		double[][] numericMatrix = new double[responses.size()][responses.get(0).getResponses().size()];
		double[] doseVector = new double[treatments.size()];

		// Fill numeric matrix
		for (int i = 0; i < numericMatrix.length; i++)
		{
			for (int j = 0; j < numericMatrix[i].length; j++)
			{
				numericMatrix[i][j] = responses.get(i).getResponses().get(j);
			}
		}

		// Fill doseVector
		for (int i = 0; i < doseVector.length; i++)
		{
			doseVector[i] = treatments.get(i).getDose();
		}

		if (updater != null)
			updater.setMessage("Williams Trend");

		WilliamsTrendTestResult result = williamsUtil.williams(MatrixUtils.createRealMatrix(numericMatrix),
				MatrixUtils.createRealVector(doseVector), 23524, Integer.valueOf(numberOfPermutations), null,
				Integer.valueOf(numThreads), updater);

		if (result == null)
		{
			if (updater != null)
				updater.setProgress(0);
			return null;
		}

		List<WilliamsTrendResult> williamsTrendResultList = new ArrayList<WilliamsTrendResult>();
		for (int i = 0; i < result.getTestStatistic().getDimension(); i++)
		{
			if (!cancel)
			{
				WilliamsTrendResult singleResult = new WilliamsTrendResult();
				singleResult.setAdjustedPValue(result.getAdjustedPValue().getEntry(i));
				singleResult.setpValue(result.getpValue().getEntry(i));
				singleResult.setProbeResponse(responses.get(i));
				williamsTrendResultList.add(singleResult);
			}
			else
			{
				return null;
			}
		}
		// now apply the filters to the list and remove items that don't match up
		int resultSize = williamsTrendResultList.size();

		for (int i = 0; i < resultSize; i++)
		{
			if (!cancel)
			{
				WilliamsTrendResult williamsTrendResult = williamsTrendResultList.get(i);

				double pValueToCheck = williamsTrendResult.getpValue();

				if (multipleTestingCorrection)
				{
					pValueToCheck = williamsTrendResult.getAdjustedPValue();
				}

				// check if control gene
				if (
				// first check the pValue
				((Double.isNaN(pValueToCheck) && pCutOff < 9999) || pValueToCheck >= pCutOff) ||
				// second check if it is a control gene
						(filterOutControlGenes && williamsTrendResult.getProbeResponse().getProbe().getId()
								.startsWith("AFFX"))

				)
				{
					williamsTrendResultList.remove(i);
					i--;
					resultSize--;
				}
			}
			else
			{
				return null;
			}
		}

		// create a new WilliamsTrendResults object and put it on the Event BuS
		WilliamsTrendResults williamsTrendResults = new WilliamsTrendResults();
		williamsTrendResults.setDoseResponseExperiement(doseResponseExperiment);
		williamsTrendResults.setWilliamsTrendResults(williamsTrendResultList);

		performFoldFilter(williamsTrendResults, processableData, foldFilterValue, isLogTransformation,
				baseValue, useFoldFilter);
		performNoelLoel(williamsTrendResults, loelPValue, loelFoldChange, tTest, numThreads, updater);

		if (cancel)
		{
			return null;
		}

		DecimalFormat df = new DecimalFormat("#.####");
		String name = doseResponseExperiment.getName() + "_williams_" + df.format(pCutOff);

		if (multipleTestingCorrection)
			notes.add("Adjusted P-Value Cutoff: " + df.format(pCutOff));
		else
			notes.add("Unadjusted P-Value Cutoff: " + df.format(pCutOff));

		notes.add("Multiple Testing Correction: " + String.valueOf(multipleTestingCorrection));
		notes.add("Filter Out Control Genes: " + String.valueOf(filterOutControlGenes));
		if (tTest)
			notes.add("NOTEL/LOTEL Test: " + "T-Test");
		else
			notes.add("NOTEL/LOTEL Test: " + "Dunnett's Test");

		notes.add("NOTEL/LOTEL p-Value Threshold: " + loelPValue);
		notes.add("NOTEL/LOTEL Fold Change Threshold: " + loelFoldChange);

		if (multipleTestingCorrection)
		{
			name += "_MTC";
		}
		else
		{
			name += "_NOMTC";
		}
		if (useFoldFilter)
		{
			notes.add("Used Fold Filter with cuttoff: " + foldFilterValue);
			notes.add("Data marked as log transformation: " + String.valueOf(isLogTransformation));
			name += "_foldfilter" + foldFilterValue;
		}
		else
		{
			name += "_nofoldfilter";
		}
		williamsTrendResults.setName(name);
		analysisInfo.setNotes(notes);
		williamsTrendResults.setAnalysisInfo(analysisInfo);

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		analysisInfo.getNotes().add("Total Run Time: " + runTime / 1000 + " seconds");
		return williamsTrendResults;
	}

	/**
	 * Performs an oriogen analysis and returns the corresponding OriogenResults object
	 */
	@Override
	public OriogenResults oriogenAnalysis(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, int initialBootstraps, int maxBootstraps, double s0Adjustment,
			boolean filterOutControlGenes, boolean useFoldFilter, double foldFilterValue, double loelPValue,
			double loelFoldChange, int numThreads, SimpleProgressUpdater updater, boolean tTest)
	{
		long startTime = System.currentTimeMillis();
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();

		DecimalFormat df = new DecimalFormat("#.####");
		String name = doseResponseExperiment.getName() + "_oriogen_" + df.format(pCutOff);

		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		notes.add("Oriogen");
		notes.add("Data Source: " + processableData);
		notes.add("Work Source: " + processableData.getParentDataSetName());
		notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		notes.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());

		if (multipleTestingCorrection)
			notes.add("Adjusted P-Value Cutoff: " + df.format(pCutOff));
		else
			notes.add("Unadjusted P-Value Cutoff: " + df.format(pCutOff));

		notes.add("Number of Initial Bootstraps: " + String.valueOf(initialBootstraps));
		notes.add("Number of Maximum Bootstraps: " + String.valueOf(maxBootstraps));
		notes.add("Shrinkage Adjustment Percentile: " + String.valueOf(s0Adjustment));
		notes.add("Multiple Testing Correction: " + String.valueOf(multipleTestingCorrection));
		notes.add("Filter Out Control Genes: " + String.valueOf(filterOutControlGenes));
		if (tTest)
			notes.add("NOTEL/LOTEL Test: " + "T-Test");
		else
			notes.add("NOTEL/LOTEL Test: " + "Dunnett's Test");

		notes.add("NOTEL/LOTEL p-Value Threshold: " + loelPValue);
		notes.add("NOTEL/LOTEL Fold Change Threshold: " + loelFoldChange);

		Origen_Data data = new Origen_Data();

		double baseValue = 2.0;
		boolean isLogTransformation = true;
		if (processableData.getLogTransformation().equals(LogTransformationEnum.BASE10))
		{
			data.setLogTransformType(4);
			baseValue = 10.0f;
		}
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NATURAL))
		{
			data.setLogTransformType(3);
			baseValue = 2.718281828459045;
		}
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NONE))
		{
			data.setLogTransformType(1);
			isLogTransformation = false;
		}
		else
		{
			data.setLogTransformType(2);
		}

		// get a list of oriogenResult
		List<ProbeResponse> responses = doseResponseExperiment.getProbeResponses();
		List<Treatment> treatments = doseResponseExperiment.getTreatments();
		double[][] numericMatrix = new double[responses.size()][responses.get(0).getResponses().size()];
		double[] doseVector = new double[treatments.size()];

		// Fill numeric matrix
		for (int i = 0; i < numericMatrix.length; i++)
		{
			for (int j = 0; j < numericMatrix[i].length; j++)
			{
				numericMatrix[i][j] = responses.get(i).getResponses().get(j);
			}
		}

		// Fill doseVector
		double current = doseVector[0];
		int count = 0;
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < doseVector.length; i++)
		{
			doseVector[i] = treatments.get(i).getDose();
			if (current == doseVector[i])
			{
				count++;
			}
			else
			{
				list.add(count);
				count = 1;
			}
			current = doseVector[i];

			if (i == doseVector.length - 1)
			{
				list.add(count);
			}
		}

		data.setInputData(MatrixUtils.createRealMatrix(numericMatrix));
		data.setNumTimePoints(MathUtil.uniqueValues(MatrixUtils.createRealVector(doseVector)));
		data.setNumInitialBootStraps(initialBootstraps);
		data.setFDRLevel(1);
		data.setRandomSeed(23524);
		data.setSubRegionPValue(.1);
		data.setTranspose(false);
		data.setMaxNumBootStraps(maxBootstraps);
		data.setLongitudinalSampling(false);
		data.setS0Percentile(s0Adjustment);
		data.setmdFdr(false);
		data.setTwoGroups(false);

		int[] values = new int[30];
		for (int i = 0; i < values.length - 1; i++)
		{
			if (i < list.size())
			{
				values[i + 1] = list.get(i);
			}
			else
			{
				values[i + 1] = 0;
			}
		}
		data.setSampleSizeDefault(values);
		data.setNumGenes(numericMatrix.length);

		ArrayList<OriogenTestResult> result = oriogenUtil.oriogen(data, updater);

		if (result == null)
		{
			if (updater != null)
				updater.setProgress(0);
			return null;
		}

		List<OriogenResult> oriogenResultList = new ArrayList<OriogenResult>();
		for (int i = 0; i < result.size(); i++)
		{
			if (!cancel)
			{
				OriogenResult singleResult = new OriogenResult();
				singleResult.setAdjustedPValue(result.get(i).getqValue());
				singleResult.setpValue(result.get(i).getpValue());
				singleResult.setProbeResponse(responses.get(i));
				singleResult.setProfile(result.get(i).getProfileString());
				oriogenResultList.add(singleResult);
			}
			else
			{
				return null;
			}
		}
		// now apply the filters to the list and remove items that don't match up
		int resultSize = oriogenResultList.size();

		for (int i = 0; i < resultSize; i++)
		{
			if (!cancel)
			{
				OriogenResult oriogenResult = oriogenResultList.get(i);

				double pValueToCheck = oriogenResult.getpValue();

				if (multipleTestingCorrection)
				{
					pValueToCheck = oriogenResult.getAdjustedPValue();
				}

				// check if control gene
				if (
				// first check the pValue
				((Double.isNaN(pValueToCheck) && pCutOff < 9999) || pValueToCheck >= pCutOff) ||
				// second check if it is a control gene
						(filterOutControlGenes
								&& oriogenResult.getProbeResponse().getProbe().getId().startsWith("AFFX"))

				)
				{
					oriogenResultList.remove(i);
					i--;
					resultSize--;
				}
			}
			else
			{
				return null;
			}
		}

		// create a new OriogenResults object and put it on the Event BuS
		OriogenResults oriogenResults = new OriogenResults();
		oriogenResults.setDoseResponseExperiement(doseResponseExperiment);
		oriogenResults.setOriogenResults(oriogenResultList);

		performFoldFilter(oriogenResults, processableData, foldFilterValue, isLogTransformation, baseValue,
				useFoldFilter);
		performNoelLoel(oriogenResults, loelPValue, loelFoldChange, tTest, DEFAULT_NUM_THREADS, updater);

		if (multipleTestingCorrection)
		{
			name += "_MTC";
		}
		else
		{
			name += "_NOMTC";
		}
		if (useFoldFilter)
		{
			notes.add("Used Fold Filter with cuttoff: " + foldFilterValue);
			notes.add("Data marked as log transformation: " + String.valueOf(isLogTransformation));
			name += "_foldfilter" + foldFilterValue;
		}
		else
		{
			name += "_nofoldfilter";
		}
		oriogenResults.setName(name);
		analysisInfo.setNotes(notes);
		oriogenResults.setAnalysisInfo(analysisInfo);

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		analysisInfo.getNotes().add("Total Run Time: " + runTime / 1000 + " seconds");

		return oriogenResults;
	}

	/**
	 * Performs a one way anova analysis and returns the corresponding OneWayANOVAResults object
	 */
	@Override
	public OneWayANOVAResults oneWayANOVAAnalysis(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, boolean filterOutControlGenes, boolean useFoldFilter,
			double foldFilterValue, double loelPValue, double loelFoldChange, int numThreads,
			SimpleProgressUpdater updater, boolean tTest)
	{
		DecimalFormat df = new DecimalFormat("#.####");

		long startTime = System.currentTimeMillis();
		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		notes.add("One-way ANOVA");
		notes.add("Data Source: " + processableData);
		notes.add("Work Source: " + processableData.getParentDataSetName());
		notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		notes.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());

		if (multipleTestingCorrection)
			notes.add("Adjusted P-Value Cutoff: " + df.format(pCutOff));
		else
			notes.add("Unadjusted P-Value Cutoff: " + df.format(pCutOff));

		notes.add("Multiple Testing Correction: " + String.valueOf(multipleTestingCorrection));
		notes.add("Filter Out Control Genes: " + String.valueOf(filterOutControlGenes));
		if (tTest)
			notes.add("NOTEL/LOTEL Test: " + "T-Test");
		else
			notes.add("NOTEL/LOTEL Test: " + "Dunnett's Test");

		notes.add("NOTEL/LOTEL p-Value Threshold: " + loelPValue);
		notes.add("NOTEL/LOTEL Fold Change Threshold: " + loelFoldChange);
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();
		// This class should eventually be moved to sciome commons
		OneWayANOVAAnalysis aNOVAAnalysis = new OneWayANOVAAnalysis();

		double baseValue = 2.0;
		boolean isLogTransformation = true;
		if (processableData.getLogTransformation().equals(LogTransformationEnum.BASE10))
			baseValue = 10.0f;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NATURAL))
			baseValue = 2.718281828459045;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NONE))
			isLogTransformation = false;

		// get a list of oneWayResults
		List<OneWayANOVAResult> oneWayResultList = aNOVAAnalysis.analyzeDoseResponseData(processableData);

		// now apply the filters to the list and remove items that don't match up
		int resultSize = oneWayResultList.size();

		for (int i = 0; i < resultSize; i++)
		{
			OneWayANOVAResult oneWayResult = oneWayResultList.get(i);

			double pValueToCheck = oneWayResult.getpValue();

			if (multipleTestingCorrection)
			{
				pValueToCheck = oneWayResult.getAdjustedPValue();
			}

			// check if control gene
			if (
			// first check the pValue
			((Double.isNaN(pValueToCheck) && pCutOff < 9999) || pValueToCheck >= pCutOff) ||
			// second check if it is a control gene
					(filterOutControlGenes
							&& oneWayResult.getProbeResponse().getProbe().getId().startsWith("AFFX"))

			)
			{
				oneWayResultList.remove(i);
				i--;
				resultSize--;
			}

		}

		// create a new OneWayANOVAAnaylisResults object and put it on the Event BuS
		OneWayANOVAResults oneWayResults = new OneWayANOVAResults();
		oneWayResults.setDoseResponseExperiement(doseResponseExperiment);
		oneWayResults.setOneWayANOVAResults(oneWayResultList);

		performFoldFilter(oneWayResults, processableData, foldFilterValue, isLogTransformation, baseValue,
				useFoldFilter);
		performNoelLoel(oneWayResults, loelPValue, loelFoldChange, tTest, DEFAULT_NUM_THREADS, updater);

		String name = doseResponseExperiment.getName() + "_oneway_" + df.format(pCutOff);

		if (multipleTestingCorrection)
		{
			name += "_MTC";
		}
		else
		{
			name += "_NOMTC";
		}
		if (useFoldFilter)
		{
			notes.add("Used Fold Filter with cuttoff: " + foldFilterValue);
			notes.add("Data marked as log transformation: " + String.valueOf(isLogTransformation));
			name += "_foldfilter" + foldFilterValue;
		}
		else
		{
			name += "_nofoldfilter";
		}
		oneWayResults.setName(name);
		analysisInfo.setNotes(notes);
		oneWayResults.setAnalysisInfo(analysisInfo);

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		analysisInfo.getNotes().add("Total Run Time: " + runTime / 1000 + " seconds");

		return oneWayResults;
	}

	/**
	 * Performs a william's trend analysis and returns the corresponding WilliamsTrendResult object
	 */
	@Override
	public CurveFitPrefilterResults curveFitPrefilterAnalysis(IStatModelProcessable processableData,
			boolean useFoldFilter, double foldFilterValue, double loelPValue, double loelFoldChange,
			int numThreads, IBMDSToolProgress updater, boolean tTest, List<StatModel> modelsToRun,
			Double bmrFactor, Double poly2BmrFactor, int constantVariance)
	{

		BMDAnalysisService bmdAnalysisService = new BMDAnalysisService();
		long startTime = System.currentTimeMillis();
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();

		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		notes.add("Curve Fit Prefilter");
		notes.add("Data Source: " + processableData);
		notes.add("Work Source: " + processableData.getParentDataSetName());
		notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		notes.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());
		notes.add("Constant Variance: " + constantVariance);
		notes.add("BMR Factor: " + bmrFactor);
		if (poly2BmrFactor != null)
			notes.add("Poly 2 BMR Factor: " + poly2BmrFactor);

		String modelsUsed = "";
		for (StatModel mod : modelsToRun)
			modelsUsed += mod.getName() + ": " + mod.getVersion() + ",";

		notes.add("Models Used: " + modelsUsed);

		double baseValue = 2.0;
		boolean isLogTransformation = true;
		if (processableData.getLogTransformation().equals(LogTransformationEnum.BASE10))
			baseValue = 10.0f;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NATURAL))
			baseValue = 2.718281828459045;
		else if (processableData.getLogTransformation().equals(LogTransformationEnum.NONE))
			isLogTransformation = false;

		List<ProbeResponse> responses = doseResponseExperiment.getProbeResponses();
		List<Treatment> treatments = doseResponseExperiment.getTreatments();
		Double maxDose = treatments.get(treatments.size() - 1).getDose().doubleValue();
		double[][] numericMatrix = new double[responses.size()][responses.get(0).getResponses().size()];
		double[] doseVector = new double[treatments.size()];

		// Fill numeric matrix
		for (int i = 0; i < numericMatrix.length; i++)
		{
			for (int j = 0; j < numericMatrix[i].length; j++)
			{
				numericMatrix[i][j] = responses.get(i).getResponses().get(j);
			}
		}

		// Fill doseVector
		for (int i = 0; i < doseVector.length; i++)
		{
			doseVector[i] = treatments.get(i).getDose();
		}

		ModelInputParameters inputParams = new ModelInputParameters();
		inputParams.setBestModelMethod(BESTMODEL_METHOD.CALCULATE);
		inputParams.setFast(true);
		inputParams.setBmrType(1);
		inputParams.setBmdCalculation(1);
		inputParams.setBmdlCalculation(1);
		inputParams.setBmrLevel(bmrFactor);
		inputParams.setPoly2BmrLevel(poly2BmrFactor);

		inputParams.setBmdMethod(BMD_METHOD.TOXICR);
		inputParams.setPolyMonotonic(false);

		// inc
		inputParams.setConstantVariance(constantVariance);
		inputParams.setRestirctPower(1);

		inputParams.setNumThreads(numThreads);
		if (numThreads == 0)
			inputParams.setNumThreads(1);

		ModelSelectionParameters modelSelectionParams = new ModelSelectionParameters();
		modelSelectionParams.setFlagHillModel(false);
		modelSelectionParams
				.setBestModelSelectionBMDLandBMDU(BestModelSelectionBMDLandBMDU.COMPUTE_BUT_IGNORE);
		modelSelectionParams.setBestPolyModelTest(BestPolyModelTestEnum.LOWEST_AIC);
		modelSelectionParams.setpValue(0.05);

		// first do the fold filter and trend test
		List<CurveFitPrefilterResult> curveFitResultList = new ArrayList<>();
		for (ProbeResponse result : processableData.getProcessableProbeResponses())
		{
			if (!cancel)
			{

				CurveFitPrefilterResult singleResult = new CurveFitPrefilterResult();
				singleResult.setProbeResponse(result);
				curveFitResultList.add(singleResult);
			}
			else
			{
				return null;
			}
		}

		// create a new curve fit prefilter object and put it on the Event BuS
		CurveFitPrefilterResults curveFitPrefilterResults = new CurveFitPrefilterResults();
		curveFitPrefilterResults.setDoseResponseExperiement(doseResponseExperiment);
		curveFitPrefilterResults.setCurveFitPrefilterResults(curveFitResultList);
		if (processableData instanceof PrefilterResults)
		{
			curveFitPrefilterResults.setPrefilterResults((PrefilterResults) processableData);
		}

		performFoldFilter(curveFitPrefilterResults, processableData, foldFilterValue, isLogTransformation,
				baseValue, useFoldFilter);
		performNoelLoel(curveFitPrefilterResults, loelPValue, loelFoldChange, tTest, numThreads, null);

		Map<String, CurveFitPrefilterResult> probesThatPassedSet = new HashMap<>();
		for (CurveFitPrefilterResult r : curveFitPrefilterResults.getCurveFitPrefilterResults())
			probesThatPassedSet.put(r.getProbeID(), r);

		if (cancel)
		{
			return null;
		}

		// create a reduced processable data input to make things more efficient
		DoseResponseExperiment de = new DoseResponseExperiment();
		de.setTreatments(processableData.getProcessableDoseResponseExperiment().getTreatments());
		de.setLogTransformation(processableData.getLogTransformation());
		de.setName(processableData.getDataSetName());
		List<ProbeResponse> responseList = new ArrayList<>();
		curveFitResultList.forEach(c -> responseList.add(c.getProbeResponse()));
		de.setProbeResponses(responseList);

		// second do the analysis
		BMDResult bmdResult = bmdAnalysisService.bmdAnalysis(de, inputParams, modelSelectionParams,
				modelsToRun, null, updater);

		List<CurveFitPrefilterResult> nextcurveFitResultList = new ArrayList<>();

		for (ProbeStatResult result : bmdResult.getProbeStatResults())
		{
			if (!cancel)
			{
				List<StatResult> statResults = new ArrayList<>();
				// only add items with convergent bmd and bmdl
				for (StatResult r : result.getStatResults())
				{
					if (r.getBMD() > -9999 && r.getBMDL() > -9999 && r.getBMD() <= maxDose
							&& r.getAIC() > -9999 && r.getBMD() != Double.NaN && r.getBMDL() != Double.NaN
							&& r.getAIC() != Double.NaN)
						statResults.add(r);
				}

				statResults.sort((r1, r2) ->
				{
					return (Double.valueOf(r1.getAIC()).compareTo(Double.valueOf(r2.getAIC())));
				});

				if (statResults.size() > 0)
				{
					CurveFitPrefilterResult singleResult = probesThatPassedSet
							.get(result.getProbeResponse().getProbe().getId());
					singleResult.setpValue(statResults.get(0).getFitPValue());
					singleResult.setProbeResponse(result.getProbeResponse());
					singleResult.setBestModel(statResults.get(0).getModel());
					singleResult.setBmd(statResults.get(0).getBMD());
					singleResult.setBmdl(statResults.get(0).getBMDL());
					nextcurveFitResultList.add(singleResult);
				}
			}
			else
				return null;

		}

		curveFitPrefilterResults.setCurveFitPrefilterResults(nextcurveFitResultList);

		DecimalFormat df = new DecimalFormat("#.####");
		String name = doseResponseExperiment.getName() + "_curvefitprefilter";

		if (tTest)
			notes.add("NOTEL/LOTEL Test: " + "T-Test");
		else
			notes.add("NOTEL/LOTEL Test: " + "Dunnett's Test");

		notes.add("NOTEL/LOTEL p-Value Threshold: " + loelPValue);
		notes.add("NOTEL/LOTEL Fold Change Threshold: " + loelFoldChange);

		if (useFoldFilter)
		{
			notes.add("Used Fold Filter with cuttoff: " + foldFilterValue);
			notes.add("Data marked as log transformation: " + String.valueOf(isLogTransformation));
			name += "_foldfilter" + foldFilterValue;
		}
		else
		{
			name += "_nofoldfilter";
		}
		curveFitPrefilterResults.setName(name);
		analysisInfo.setNotes(notes);
		curveFitPrefilterResults.setAnalysisInfo(analysisInfo);

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		analysisInfo.getNotes().add("Total Run Time: " + runTime / 1000 + " seconds");
		return curveFitPrefilterResults;
	}

	@Override
	public void cancel()
	{
		cancel = true;

		// Cancel any prefilter tests that are running
		williamsUtil.cancel();
		oriogenUtil.cancel();

		// Cancel the dunnett's test if it's running
		if (executor != null)
		{
			executor.shutdownNow();
			try
			{
				executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start()
	{
		cancel = false;
	}

	private void performFoldFilter(PrefilterResults prefilterResults, IStatModelProcessable processableData,
			double foldFilterValue, boolean isLogTransformation, double baseValue, boolean useFoldFilter)
	{
		int resultSize = prefilterResults.getPrefilterResults().size();

		FoldChange foldChange = new FoldChange(
				processableData.getProcessableDoseResponseExperiment().getTreatments(), isLogTransformation,
				baseValue);
		for (int i = 0; i < resultSize; i++)
		{
			Float bestFoldChange = foldChange.getBestFoldChangeValue(
					prefilterResults.getPrefilterResults().get(i).getProbeResponse().getResponses());
			prefilterResults.getPrefilterResults().get(i).setBestFoldChange(bestFoldChange);
			prefilterResults.getPrefilterResults().get(i).setFoldChanges(foldChange.getFoldChanges());

			if (useFoldFilter && Math.abs(bestFoldChange) < Math.abs(foldFilterValue))
			{
				prefilterResults.getPrefilterResults().remove(i);
				i--;
				resultSize--;
			}

		}
	}

	private void performNoelLoel(PrefilterResults prefilterResults, double pValue, double foldFilterValue,
			boolean tTest, int numThreads, SimpleProgressUpdater updater)
	{
		if (updater != null)
			updater.setProgress(0);

		// Remove duplicates from treatments
		List<Float> treatments = new ArrayList<Float>();
		List<Integer> doseGroups = new ArrayList<Integer>();
		Float current = prefilterResults.getDoseResponseExperiement().getTreatments().get(0).getDose();
		int count = 0;
		boolean found = false;
		for (int i = 0; i < prefilterResults.getDoseResponseExperiement().getTreatments().size(); i++)
		{
			if (current.equals(prefilterResults.getDoseResponseExperiement().getTreatments().get(i).getDose())
					&& !found)
			{
				found = true;
			}
			else if (!current
					.equals(prefilterResults.getDoseResponseExperiement().getTreatments().get(i).getDose()))
			{
				treatments.add(current);
				doseGroups.add(new Integer(count));
				current = prefilterResults.getDoseResponseExperiement().getTreatments().get(i).getDose();
				found = false;
				count = 0;
			}
			count++;
		}
		treatments.add(current);
		doseGroups.add(count);

		// create a map of probe ids to probe response
		// so we can get the prefilter probe repsonses.
		Map<String, List<Float>> probeResponseMap = new HashMap<>();
		for (ProbeResponse pr : prefilterResults.getDoseResponseExperiement().getProbeResponses())
			probeResponseMap.put(pr.getProbe().getId(), pr.getResponses());

		TTest test = new TTest();
		DunnettsTest dunnetts = new DunnettsTest();

		executor = Executors.newFixedThreadPool(numThreads);
		// Loop through the probes
		for (int i = 0; i < prefilterResults.getPrefilterResults().size(); i++)
		{
			final int index = i;
			Runnable run = new Runnable() {
				@Override
				public void run()
				{
					List<Float> pValues = new ArrayList<Float>();
					double[] control = new double[doseGroups.get(0)];
					int count = 0;
					for (int j = 0; j < doseGroups.get(0); j++)
					{
						control[j] = probeResponseMap
								.get(prefilterResults.getPrefilterResults().get(index).getProbeID())
								.get(count).doubleValue();
						count++;
					}

					if (tTest)
					{
						// compare each dose group to the control dosegroup using TTest store the
						// corresponding P values
						for (int j = 1; j < doseGroups.size(); j++)
						{
							double[] sample1 = new double[doseGroups.get(j)];
							for (int k = 0; k < doseGroups.get(j); k++)
							{
								sample1[k] = probeResponseMap
										.get(prefilterResults.getPrefilterResults().get(index).getProbeID())
										.get(count).doubleValue();
								count++;
							}
							if (control.length > 1 && sample1.length > 1)
								pValues.add(new Float((float) test.tTest(control, sample1)));
							else
								pValues.add(Float.NaN);
						}
					}
					else
					{
						if (updater != null)
							updater.setMessage("Dunnett's Test: " + index + "/"
									+ prefilterResults.getPrefilterResults().size());
						// Use Dunnett's test to calculate p values
						double[][] doses = new double[doseGroups.size() - 1][];
						for (int j = 1; j < doseGroups.size(); j++)
						{
							double[] sample1 = new double[doseGroups.get(j)];
							for (int k = 0; k < doseGroups.get(j); k++)
							{
								sample1[k] = probeResponseMap
										.get(prefilterResults.getPrefilterResults().get(index).getProbeID())
										.get(count).doubleValue();
								count++;
							}
							doses[j - 1] = sample1;
						}
						double[] pVals = dunnetts.dunnettsTest(control, doses, 15000);
						for (int j = 0; j < pVals.length; j++)
						{
							pValues.add((float) pVals[j]);
						}
					}
					prefilterResults.getPrefilterResults().get(index).setNoelLoelPValues(pValues);

					// Loop through the doses (excluding control dose)
					for (int j = 0; j < prefilterResults.getPrefilterResults().get(index).getFoldChanges()
							.size(); j++)
					{
						// If t test p value is less than parameter and fold change is above threshold, then
						// set
						// NOEL/LOEL
						// and stop.
						if (Math.abs(prefilterResults.getPrefilterResults().get(index).getFoldChanges()
								.get(j)) > foldFilterValue && pValues.get(j) < pValue)
						{
							prefilterResults.getPrefilterResults().get(index).setNoelDose(treatments.get(j));
							prefilterResults.getPrefilterResults().get(index)
									.setLoelDose(treatments.get(j + 1));
							break;
						}
					}
					if (updater != null)
					{
						double progress = index / (double) prefilterResults.getPrefilterResults().size();
						updater.setProgress(progress);
					}
				}
			};
			executor.execute(run);
		}
		// Stop accepting new runnables
		executor.shutdown();

		// Wait for all the threads to finish
		while (!executor.isTerminated())
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
