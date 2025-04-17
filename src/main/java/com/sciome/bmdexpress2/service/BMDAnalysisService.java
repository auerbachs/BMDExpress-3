package com.sciome.bmdexpress2.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;

import com.sciome.bmdexpress2.mvp.model.DoseGroup;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.LogTransformationEnum;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.prefilter.PrefilterResults;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.probe.Treatment;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ExponentialResult;
import com.sciome.bmdexpress2.mvp.model.stat.GCurvePResult;
import com.sciome.bmdexpress2.mvp.model.stat.HillResult;
import com.sciome.bmdexpress2.mvp.model.stat.PolyResult;
import com.sciome.bmdexpress2.mvp.model.stat.PowerResult;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IBMDAnalysisService;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSMATool;
import com.sciome.bmdexpress2.util.bmds.BMDSTool;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;
import com.sciome.bmdexpress2.util.curvep.CurvePProcessor;
import com.sciome.bmdexpress2.util.curvep.GCurvePInputParameters;

public class BMDAnalysisService implements IBMDAnalysisService
{

	BMDSTool bMDSTool;
	BMDSMATool bMDSMATool;
	boolean cancel = false;

	/*
	 * Run parametric bmd analylsis via epa models on processable data
	 */
	@Override
	public BMDResult bmdAnalysis(IStatModelProcessable processableData, ModelInputParameters inputParameters,
			ModelSelectionParameters modelSelectionParameters, List<StatModel> modelsToRun, String tmpFolder,
			IBMDSToolProgress progressUpdater)
	{
		inputParameters.setObservations(
				processableData.getProcessableDoseResponseExperiment().getTreatments().size());
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();
		bMDSTool = new BMDSTool(processableData.getProcessableProbeResponses(),
				processableData.getProcessableDoseResponseExperiment().getTreatments(), inputParameters,
				modelSelectionParameters, modelsToRun, progressUpdater, processableData, tmpFolder,
				doseResponseExperiment.getLogTransformation());
		BMDResult bMDResults = bMDSTool.bmdAnalyses();

		// calculate step function
		for (ProbeStatResult psr : bMDResults.getProbeStatResults())
		{
			for (StatResult statResult : psr.getStatResults())
			{
				int isStep = isStepFunction(null, statResult, doseResponseExperiment,
						inputParameters.getStepFunctionThreshold());

				statResult.setIsStepFunction(isStep > 0);
				statResult.setStepWithBMDLessLowest(
						isStepFunctionWithBMDLessThanLowest(statResult, isStep, doseResponseExperiment));
			}

		}

		// someone canceled this. so just uncancel it before returning.
		if (cancel)
			cancel = false;
		if (bMDResults == null)
			return null;

		bMDResults.setDoseResponseExperiment(doseResponseExperiment);
		if (processableData instanceof PrefilterResults)
			bMDResults.setPrefilterResults((PrefilterResults) processableData);

		List<ProbeResponse> responses = processableData.getProcessableProbeResponses();
		List<Treatment> treatments = doseResponseExperiment.getTreatments();
		List<ArrayList<Float>> numericMatrix = new ArrayList<ArrayList<Float>>();
		List<Float> doseVector = new ArrayList<Float>();
		// Fill numeric matrix
		for (int i = 0; i < responses.size(); i++)
		{
			numericMatrix.add((ArrayList<Float>) responses.get(i).getResponses());
		}

		// Fill doseVector
		for (int i = 0; i < treatments.size(); i++)
		{
			doseVector.add(treatments.get(i).getDose());
		}

		Set<String> doseGroups = new HashSet<>();
		for (Float dose : doseVector)
			doseGroups.add(dose.toString());

		// Calculate and set wAUC values
		if (doseGroups.size() > 2)
		{
			// float currBMR = (float) inputParameters.getBmrLevel();
			List<Float> wAUCList = new ArrayList<Float>();
			for (int i = 0; i < responses.size(); i++)
			{

				StatResult stat = bMDResults.getProbeStatResults().get(i).getBestStatResult();

				if (stat == null)
				{
					wAUCList.add(0.0f);
					continue;
				}

				// below, wAUC metric is calculated based on parametric curves, as such, values will differ
				// from gcurvep-based estimates
				List<Float> udoses = CurvePProcessor.CollapseDoses(doseVector);
				List<Float> logudoses = new ArrayList<Float>();
				try
				{
					logudoses = CurvePProcessor.logBaseDoses(udoses, -24);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/*
				 * String CurrID = doseResponseExperiment.getProbeResponses().get(1).getProbe().getId(); if
				 * (CurrID.equals("1367733_at")) { List<Float> uu = CurvePProcessor.CollapseDoses( doseVector
				 * ); int ll = uu.size(); CurrID = Integer.toString(ll); }
				 */

				int type = -1; // unknown parametric curve type

				if (stat instanceof PolyResult)
					type = 0;
				// if(stat instanceof LogarithmicResult) type = 10; //reserved for future
				if (stat instanceof PowerResult)
					type = 20;
				if (stat instanceof HillResult)
					type = 30;
				if (stat instanceof ExponentialResult)
					type = 40 + ((ExponentialResult) stat).getOption();

				List<Float> coffs = new ArrayList<Float>();
				double[] dcoffs = stat.getCurveParameters();
				for (int nn = 0; nn < dcoffs.length; nn++)
				{
					coffs.add((float) dcoffs[nn]);
				}
				float aucv = CurvePProcessor.intg_log_AUC(udoses, stat, type, -24, 1000); // better implement
																							// and use
																							// getResponseAt()
																							// inside
																							// StatResult
																							// class instead
																							// of exporting &
																							// re0importing
																							// model pars
				// */

				// float aucv = CurvePProcessor.calc_AUC( logudoses,
				// CurvePProcessor.calc_WgtAvResponses(doseVector, numericMatrix.get(i)) );
				float ww = CurvePProcessor.calc_wAUC(aucv, (float) Math.log10(stat.getBMD()), logudoses);
				wAUCList.add(ww);

			}
			bMDResults.setwAUC(wAUCList);

			// Calculate and set log 2 wAUC values
			List<Float> logwAUCList = CurvePProcessor.logwAUC(wAUCList);
			bMDResults.setLogwAUC(logwAUCList);
		}

		// clean up any leftovers from this process
		bMDSTool.cleanUp();
		calculateExtraStatistics(bMDResults);

		// temporary to test loglikeliood
		calculateLikelihood(bMDResults);

		return bMDResults;
	}

	@Override
	public boolean cancel()
	{
		cancel = true;
		if (bMDSTool != null)
		{
			bMDSTool.cancel();

			return true;
		}
		if (bMDSMATool != null)
		{
			bMDSMATool.cancel();

			return true;
		}
		return false;
	}

	/*
	 * Run GcurveP on the proccessable data given
	 * 
	 */
	@Override
	public BMDResult bmdAnalysisGCurveP(IStatModelProcessable processableData,
			GCurvePInputParameters inputParameters, IBMDSToolProgress me)
	{

		BMDResult bMDResults = new BMDResult();

		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();
		bMDResults.setDoseResponseExperiment(doseResponseExperiment);
		if (processableData instanceof PrefilterResults)
			bMDResults.setPrefilterResults((PrefilterResults) processableData);

		List<ProbeResponse> responses = processableData.getProcessableProbeResponses();
		List<Treatment> treatments = doseResponseExperiment.getTreatments();
		List<ArrayList<Float>> numericMatrix = new ArrayList<ArrayList<Float>>();
		List<Float> doseVector = new ArrayList<Float>();
		// Fill numeric matrix
		for (int i = 0; i < responses.size(); i++)
			numericMatrix.add((ArrayList<Float>) responses.get(i).getResponses());

		// Fill doseVector
		for (int i = 0; i < treatments.size(); i++)
			doseVector.add(treatments.get(i).getDose());

		/* do the gcurvep processing here! */
		/*
		 * String CurrID = doseResponseExperiment.getProbeResponses().get(1).getProbe().getId(); if
		 * (CurrID.equals("1370387_at")) { //dbg int dd = 0; CurrID = Integer.toString(dd); }//
		 */

		List<ProbeStatResult> probeStatResults = new ArrayList<>();
		for (int i = 0; i < responses.size(); i++)
		{

			// someone canceled the process
			if (cancel)
			{
				cancel = false;
				return null;
			}
			List<Float> correctedPointsMinus = new ArrayList<>();
			List<Float> correctedPointsPlus = new ArrayList<>();
			// List<Float> correctedPointsNeutral = new ArrayList<>();

			/*
			 * Supply BMR directly into CurveP calls! 07.16.2019
			 */

			List<Float> collapsedDoses = CurvePProcessor.CollapseDoses(doseVector);
			Float firstNonControlDose = collapsedDoses.get(1);
			if (inputParameters.getControlDoseAdjustment() != null)
				firstNonControlDose *= inputParameters.getControlDoseAdjustment().floatValue();
			else
				firstNonControlDose *= collapsedDoses.get(1) / collapsedDoses.get(2);

			Float firstNonControlDoseLogged10 = new Float(Math.log10(firstNonControlDose.doubleValue()));

			List<Float> weightedAvgs = CurvePProcessor.calc_WgtAvResponses(doseVector, numericMatrix.get(i));
			List<Float> weightedStdDeviations = CurvePProcessor.calc_WgtSdResponses(doseVector,
					numericMatrix.get(i));

			float BMR_neg = CurvePProcessor.calc_PODR_bySD(weightedAvgs.get(0), weightedStdDeviations.get(0),
					-inputParameters.getBMR());
			float BMR_poz = CurvePProcessor.calc_PODR_bySD(weightedAvgs.get(0), weightedStdDeviations.get(0),
					inputParameters.getBMR());

			List<Float> valuesMinus = CurvePProcessor.curvePcorr(doseVector, numericMatrix.get(i),
					correctedPointsMinus, BMR_neg, -1, inputParameters.getBootStraps(),
					inputParameters.getpValueCutoff(), firstNonControlDoseLogged10);

			List<Float> valuesPlus = CurvePProcessor.curvePcorr(doseVector, numericMatrix.get(i),
					correctedPointsPlus, BMR_poz, 1, inputParameters.getBootStraps(),
					inputParameters.getpValueCutoff(), firstNonControlDoseLogged10);

			List<Float> values = valuesPlus;
			List<Float> correctedPoints = correctedPointsPlus;
			int mono = 1;

			boolean allgoodminus = Double.isFinite(valuesMinus.get(5).doubleValue())
					&& Double.isFinite(valuesMinus.get(4).doubleValue())
					&& Double.isFinite(valuesMinus.get(6).doubleValue())
					&& !Double.isNaN(valuesMinus.get(5).doubleValue())
					&& !Double.isNaN(valuesMinus.get(4).doubleValue())
					&& !Double.isNaN(valuesMinus.get(6).doubleValue());
			boolean allgoodplus = Double.isFinite(valuesPlus.get(5).doubleValue())
					&& Double.isFinite(valuesPlus.get(4).doubleValue())
					&& Double.isFinite(valuesPlus.get(6).doubleValue())
					&& !Double.isNaN(valuesPlus.get(5).doubleValue())
					&& !Double.isNaN(valuesPlus.get(4).doubleValue())
					&& !Double.isNaN(valuesPlus.get(6).doubleValue());

			// first choose the direction where fitpvalue is not 0.0
			if (valuesMinus.get(0).doubleValue() == 0.0 && valuesPlus.get(0).doubleValue() != 0.0)
			{
				values = valuesPlus;
				correctedPoints = correctedPointsPlus;
				mono = 1;
			}
			else if (valuesPlus.get(0).doubleValue() == 0.0 && valuesMinus.get(0).doubleValue() != 0.0)
			{
				mono = -1;
				values = valuesMinus;
				correctedPoints = correctedPointsMinus;
			}
			// then after fit pvalue choose the one with convergence on bmdl/bmd/bmdu
			else if (allgoodminus && !allgoodplus)
			{
				mono = -1;
				values = valuesMinus;
				correctedPoints = correctedPointsMinus;
			}
			else if (!allgoodminus && allgoodplus)
			{
				values = valuesPlus;
				correctedPoints = correctedPointsPlus;
				mono = 1;
			}
			// if all converge, and there is a pvalue != 0.0, pick best fit (fraction of saved signal), as the
			// direction
			else if (valuesPlus.get(0).doubleValue() < valuesMinus.get(0).doubleValue())
			{// ..choose
				mono = -1;
				values = valuesMinus;
				correctedPoints = correctedPointsMinus;
			}

			List<Float> correctedPointsOffsets = new ArrayList<>();

			for (int j = 0; j < numericMatrix.get(i).size(); j++)
				correctedPointsOffsets.add(numericMatrix.get(i).get(j) - correctedPoints.get(j));

			ProbeStatResult psR = new ProbeStatResult();
			GCurvePResult gResult = new GCurvePResult();
			gResult.setFitPValue(values.get(0).doubleValue());
			gResult.setAIC(Double.NaN);
			gResult.setCorrectedDoseResponseOffsetValues(correctedPointsOffsets);
			gResult.setCurveParameters(null);
			gResult.setFitLogLikelihood(Double.NaN);
			gResult.setSuccess("true");
			gResult.setBMDL(Math.pow(10.0, values.get(4).doubleValue()));
			gResult.setBMD(Math.pow(10.0, values.get(5).doubleValue()));
			gResult.setBMDU(Math.pow(10.0, values.get(6).doubleValue()));
			gResult.setBMDLauc(values.get(1).doubleValue());
			gResult.setBMDauc(values.get(2).doubleValue());
			gResult.setBMDUauc(values.get(3).doubleValue());
			gResult.setBMDLwAuc(values.get(7).doubleValue());
			gResult.setBMDwAuc(values.get(8).doubleValue());
			gResult.setBMDUwAuc(values.get(9).doubleValue());
			gResult.setAdjustedControlDoseValue(firstNonControlDose.doubleValue());
			if (mono > 0)
				gResult.setBmr(BMR_poz);
			else
				gResult.setBmr(BMR_neg);

			gResult.setWeightedAverages(weightedAvgs);
			gResult.setWeightedStdDeviations(weightedStdDeviations);

			gResult.setAdverseDirection((short) mono);
			psR.setBestPolyStatResult(null);

			psR.setBestPolyStatResult(null);
			psR.setBestStatResult(gResult);
			psR.setChiSquaredResults(null);
			psR.setProbeResponse(responses.get(i));
			psR.setStatResults(new ArrayList<>(Arrays.asList(gResult)));
			probeStatResults.add(psR);
			float percentComplete = (float) i / (float) processableData.getProcessableProbeResponses().size();
			me.updateProgress("Progress: " + Precision.round(100 * percentComplete, 2) + "% complete for "
					+ processableData.getDataSetName(), percentComplete);

		}

		bMDResults.setName(processableData.toString() + "_SciomeGCurveP");
		bMDResults.setProbeStatResults(probeStatResults);
		if (processableData instanceof PrefilterResults)
			bMDResults.setPrefilterResults((PrefilterResults) processableData);

		bMDResults.setDoseResponseExperiment(processableData.getProcessableDoseResponseExperiment());
		bMDResults.setwAUC(null);
		bMDResults.setLogwAUC(null);

		AnalysisInfo analysisInfo = new AnalysisInfo();

		analysisInfo.setNotes(new ArrayList<>());
		analysisInfo.getNotes().add("Benchmark Dose Analyses With Sciome GCurveP");
		analysisInfo.getNotes().add("Data Source: " + processableData.getParentDataSetName());
		analysisInfo.getNotes().add("Work Source: " + processableData.toString());
		analysisInfo.getNotes()
				.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		analysisInfo.getNotes()
				.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());
		analysisInfo.getNotes().add("Operating System: " + System.getProperty("os.name"));
		analysisInfo.getNotes().add("Number of bootstraps: " + inputParameters.getBootStraps());
		analysisInfo.getNotes().add("BMR: " + inputParameters.getBMR());
		analysisInfo.getNotes().add("pValue for intervals: " + inputParameters.getpValueCutoff());
		if (inputParameters.getControlDoseAdjustment() != null)
			analysisInfo.getNotes()
					.add("Control Dose Adjustment: " + inputParameters.getControlDoseAdjustment());
		bMDResults.setAnalysisInfo(analysisInfo);
		// calculate step function
		for (ProbeStatResult psr : bMDResults.getProbeStatResults())
		{
			for (StatResult statResult : psr.getStatResults())
			{
				int isStep = isStepFunction(null, statResult, doseResponseExperiment,
						inputParameters.getStepFunctionThreshold());
				statResult.setIsStepFunction(isStep > 0);
				statResult.setStepWithBMDLessLowest(isStep == 1);
			}

		}

		return bMDResults;

	}

	@Override
	public BMDResult bmdAnalysisLaPlaceMA(IStatModelProcessable processableData,
			ModelInputParameters inputParameters, List<StatModel> modelsToRun,
			IBMDSToolProgress progressUpdater)
	{
		return bmdAnalysisMA(processableData, inputParameters, modelsToRun, progressUpdater, false);
	}

	@Override
	public BMDResult bmdAnalysisMCMCMA(IStatModelProcessable processableData,
			ModelInputParameters inputParameters, List<StatModel> modelsToRun,
			IBMDSToolProgress progressUpdater)
	{
		return bmdAnalysisMA(processableData, inputParameters, modelsToRun, progressUpdater, true);
	}

	@Override
	public int isStepFunction(List<Float> responses, StatResult bestResult,
			DoseResponseExperiment doseResponseExp, double threshold)
	{
		if (bestResult != null)
		{
			List<DoseGroup> dosegroups = doseResponseExp.getDoseGroups(null);
			double control = transform(bestResult.getResponseAt(dosegroups.get(0).getDose()),
					doseResponseExp);
			double last = transform(bestResult.getResponseAt(dosegroups.get(dosegroups.size() - 1).getDose()),
					doseResponseExp);

			double totalchange = Math.abs(last - control);

			for (int i = 1; i < dosegroups.size(); i++)
			{
				double dg1 = transform(bestResult.getResponseAt(dosegroups.get(i - 1).getDose()),
						doseResponseExp);
				double dg2 = transform(bestResult.getResponseAt(dosegroups.get(i).getDose()),
						doseResponseExp);

				double change = Math.abs(dg2 - dg1);
				if (change / totalchange >= threshold)
					return i;
			}
		}

		return 0;
	}

	public boolean isStepFunctionWithBMDLessThanLowest(StatResult bestResult, int isStep,
			DoseResponseExperiment doseResponseExp)
	{

		List<DoseGroup> dosegroups = doseResponseExp.getDoseGroups(null);

		if (bestResult != null && bestResult.getBMD() != -9999 && dosegroups.size() >= 2
				&& bestResult.getBMD() < dosegroups.get(1).getDose() && isStep == 1)
			return true;

		return false;
	}

	private double transform(Double responseMean, DoseResponseExperiment doseResponseExp)
	{

		// if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.BASE2))
		// return Math.pow(2.0, responseMean);
		// else if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.NONE))
		return responseMean;
		// else if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.NATURAL))
		// return Math.pow(Math.E, responseMean);

		// else
		// return Math.pow(10.0, responseMean);

	}

	private BMDResult bmdAnalysisMA(IStatModelProcessable processableData,
			ModelInputParameters inputParameters, List<StatModel> modelsToRun,
			IBMDSToolProgress progressUpdater, boolean useMCMC)
	{
		DoseResponseExperiment doseResponseExperiment = processableData
				.getProcessableDoseResponseExperiment();
		inputParameters.setObservations(
				processableData.getProcessableDoseResponseExperiment().getTreatments().size());
		bMDSMATool = new BMDSMATool(processableData.getProcessableProbeResponses(),
				processableData.getProcessableDoseResponseExperiment().getTreatments(), inputParameters,
				modelsToRun, useMCMC, progressUpdater, processableData,
				doseResponseExperiment.getLogTransformation());
		BMDResult bMDResults = bMDSMATool.bmdAnalyses();
		if (cancel)
			cancel = false;
		if (bMDResults == null)
			return null;

		// calculate step function
		for (ProbeStatResult psr : bMDResults.getProbeStatResults())
		{
			for (StatResult statResult : psr.getStatResults())
			{
				int isStep = isStepFunction(null, statResult, doseResponseExperiment,
						inputParameters.getStepFunctionThreshold());
				statResult.setIsStepFunction(isStep > 0);
				statResult.setStepWithBMDLessLowest(isStep == 1);
			}

		}

		bMDResults.setDoseResponseExperiment(doseResponseExperiment);
		if (processableData instanceof PrefilterResults)
			bMDResults.setPrefilterResults((PrefilterResults) processableData);

		// someone canceled this. so just uncancel it before returning.

		// clean up any leftovers from this process
		bMDSMATool.cleanUp();
		calculateExtraStatistics(bMDResults);
		return bMDResults;
	}

	private void calculateExtraStatistics(BMDResult bmdResults)
	{
		BMDStatisticsService statServ = new BMDStatisticsService();

		for (ProbeStatResult psr : bmdResults.getProbeStatResults())
		{
			// build the dosegroup array.
			List<DoseGroup> doseGroups = bmdResults.getDoseResponseExperiment()
					.getDoseGroups(psr.getProbeResponse().getResponses());
			LogTransformationEnum logTrans = bmdResults.getDoseResponseExperiment().getLogTransformation();

			for (StatResult statResult : psr.getStatResults())
			{
				double[] residuals = null;
				try
				{
					residuals = statServ.calculateResiduals(statResult,
							doseGroups.stream().map(dg -> dg.getResponseMean()).collect(Collectors.toList()),
							doseGroups.stream().map(dg -> dg.getDose()).collect(Collectors.toList()));
					statResult.setResiduals(residuals);
				}
				catch (Exception e)
				{}
				try
				{
					if (residuals != null)
					{
						double rSquared = statServ.calculateRSquared(residuals, doseGroups.stream()
								.map(dg -> dg.getResponseMean()).collect(Collectors.toList()));
						statResult.setrSquared(rSquared);
					}
				}
				catch (Exception e)
				{}
				try
				{
					double zScore = statServ.calculateZScore(statResult,
							doseGroups.stream().map(dg -> dg.getDose()).collect(Collectors.toList()));
					statResult.setZscore(zScore);
				}
				catch (Exception e)
				{}
				try
				{
					double bmrCountsToTop = statServ.calculateSDBeyond(statResult,
							doseGroups.stream().map(dg -> dg.getDose()).collect(Collectors.toList()));
					statResult.setBmrCountsToTop(bmrCountsToTop);
				}
				catch (Exception e)
				{}
				try
				{
					double fcToTop = statServ.calculateFCToTop(statResult,
							doseGroups.stream().map(dg -> dg.getDose()).collect(Collectors.toList()),
							logTrans);
					statResult.setFoldChangeToTop(fcToTop);
				}
				catch (Exception e)
				{}

				double maxdose = doseGroups.get(doseGroups.size() - 1).getDose();
				double mindose = doseGroups.get(1).getDose();

				// for nonmonotonic curves find the top
				// figure out if bmd is < or > top and reset start/end for gradient
				if (statResult instanceof PolyResult && ((PolyResult) statResult).getDegree() == 2)
				{
					// assume maxdose is at vertex and bmd to the left
					maxdose = ((PolyResult) statResult).getVertext();

					// but if the vertex is beyond maxdose, just reset max dose
					if (maxdose > doseGroups.get(doseGroups.size() - 1).getDose())
						maxdose = doseGroups.get(doseGroups.size() - 1).getDose();

					// but if bmd is to the right of max dose...shift the min and max
					if (((PolyResult) statResult).getBMD() > maxdose)
					{
						// shift the mindose to the maxdose, which is probably vertext
						mindose = maxdose;
						// if the mindose (or vertex) is less than the low dose
						// set the mindose to the original
						if (mindose <= doseGroups.get(1).getDose())
							mindose = doseGroups.get(1).getDose();

						// make sure the max dose is the last dose in this situation
						maxdose = doseGroups.get(doseGroups.size() - 1).getDose();

						if (maxdose == mindose)
							mindose = doseGroups.get(1).getDose();

					}
				}

				// initialize
				statResult.setBmdLowDoseRatio(Double.NaN);
				statResult.setBmdHighDoseRatio(Double.NaN);
				statResult.setBmdResponseLowDoseResponseRatio(Double.NaN);
				statResult.setBmdResponseHighDoseResponseRatio(Double.NaN);
				try
				{
					statResult.setBmdLowDoseRatio(statResult.getBMD() / mindose);
				}
				catch (Exception e)
				{}

				try
				{
					statResult.setBmdHighDoseRatio(statResult.getBMD() / maxdose);
				}
				catch (Exception e)
				{}

				try
				{
					statResult
							.setBmdResponseLowDoseResponseRatio(statResult.getResponseAt(statResult.getBMD())
									/ statResult.getResponseAt(mindose));
				}
				catch (Exception e)
				{}

				try
				{
					statResult
							.setBmdResponseHighDoseResponseRatio(statResult.getResponseAt(statResult.getBMD())
									/ statResult.getResponseAt(maxdose));
				}
				catch (Exception e)
				{}

			}

		}

	}

	private void calculateLikelihood(BMDResult bmdResults)
	{

		for (ProbeStatResult psr : bmdResults.getProbeStatResults())
		{
			List<Float> y_j = psr.getProbeResponse().getResponses();
			// build the dosegroup array.
			List<Treatment> doses = bmdResults.getDoseResponseExperiment().getTreatments();

			Double maxconstant = doses.size() * Math.log((1 / Math.sqrt(2 * Math.PI)));
			for (StatResult statResult : psr.getStatResults())
			{
				List<Double> mu_j = new ArrayList<>();
				int i = 0;
				Double sum = 0.0;
				for (Treatment treatment : doses)
				{
					Double value = statResult.getResponseAt(treatment.getDose());
					sum += Math.pow(y_j.get(i) - value, 2.0);

					i++;
				}
				Double signma2 = sum / doses.size();
				Double loglikelihood = -(doses.size() / 2) * (Math.log(2 * Math.PI) + Math.log(signma2))
						- (doses.size() / 2);

				Double realloglikelioddfromtoxicr = statResult.getFitLogLikelihood() + maxconstant;

				String result = statResult.getModel() + "\t" + loglikelihood + "\t"
						+ realloglikelioddfromtoxicr + "\t" + statResult.getFitLogLikelihood();
				System.out.println(result);

			}

		}

	}

}
