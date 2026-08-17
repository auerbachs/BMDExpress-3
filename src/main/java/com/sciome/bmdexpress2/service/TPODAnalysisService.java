package com.sciome.bmdexpress2.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sciome.bmdexpress2.mvp.model.category.AdverseDirectionEnum;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.tpod.BMDEndpointType;
import com.sciome.bmdexpress2.mvp.model.tpod.LCRDParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthPercentParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthRankParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisFilter;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputFilter;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODMethodParameter;
import com.sciome.bmdexpress2.service.tpod.CalcResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeAntimodeResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeDetector;
import com.sciome.bmdexpress2.service.tpod.lcrd.LCRD;
import com.sciome.bmdexpress2.service.tpod.nthperc.NthPercentile;
import com.sciome.bmdexpress2.service.tpod.nthrank.NthRank;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public class TPODAnalysisService implements ITPODService
{

	@Override
	public TPODAnalysisResults tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater)
	{

		// TODO: first we need to do some filtration.
		List<CategoryAnalysisResult> filteredResults = new ArrayList<>();
		Map<TPODAnalysisFilter, Integer> filteredByType = new HashMap<>();
		filteredByType.put(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE, 0);
		filteredByType.put(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS, 0);
		filteredByType.put(TPODAnalysisFilter.PERCENTAGE, 0);
		filteredByType.put(TPODAnalysisFilter.OVERALLDIRECTION, 0);

		int failCount = 0;
		int successCount = 0;
		for (CategoryAnalysisResult res : processableData.getCategoryAnalsyisResults())
		{

			boolean pass = true;
			for (TPODInputFilter filter : inputParameters.getInputFilters())
			{

				if (filter.getTpodFilter().equals(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE))
				{
					if (res.getFishersExactRightPValue() == null
							|| res.getFishersExactRightPValue() > filter.getValue().doubleValue())
					{
						pass = false;
						filteredByType.put(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE,
								filteredByType.get(TPODAnalysisFilter.FISHERS_RIGHT_P_VALUE) + 1);
					}
				}

				else if (filter.getTpodFilter().equals(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS))
				{
					if (res.getGenesThatPassedAllFilters() == null
							|| res.getGenesThatPassedAllFilters() < filter.getValue().intValue())
					{
						pass = false;
						filteredByType.put(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS,
								filteredByType.get(TPODAnalysisFilter.GENES_PASS_ALL_FILTERS) + 1);

					}
				}
				else if (filter.getTpodFilter().equals(TPODAnalysisFilter.PERCENTAGE))
				{
					if (res.getPercentage() == null || res.getPercentage() < filter.getValue().doubleValue())
					{
						pass = false;
						filteredByType.put(TPODAnalysisFilter.PERCENTAGE,
								filteredByType.get(TPODAnalysisFilter.PERCENTAGE) + 1);

					}
				}
				else if (filter.getTpodFilter().equals(TPODAnalysisFilter.OVERALLDIRECTION))
				{
					if (res.getOverallDirection() == null
							|| (res.getOverallDirection().equals(AdverseDirectionEnum.UP)
									&& filter.getValue().intValue() == -1)
							|| (res.getOverallDirection().equals(AdverseDirectionEnum.DOWN)
									&& filter.getValue().intValue() == 1)
							|| res.getOverallDirection().equals(AdverseDirectionEnum.CONFLICT))
					{
						pass = false;
						filteredByType.put(TPODAnalysisFilter.OVERALLDIRECTION,
								filteredByType.get(TPODAnalysisFilter.OVERALLDIRECTION) + 1);

					}
				}

			}
			if (pass)
			{
				filteredResults.add(res);
				successCount++;

			}
			else
				failCount++;
		}

		long startTime = System.currentTimeMillis();
		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		notes.add("Data Source: " + processableData.getBmdResult().getDoseResponseExperiment().getName());
		notes.add("Work Source: " + processableData.getName());
		notes.add("BMDExpress3 Version: " + BMDExpressProperties.getInstance().getVersion());
		notes.add("Timestamp (Start Time): " + BMDExpressProperties.getInstance().getTimeStamp());

		notes.add("Gene sets in input before filters applied: "
				+ processableData.getCategoryAnalsyisResults().size());
		notes.add("Gene sets filtered out in analysis: " + failCount);
		notes.add("Gene sets used in analysis: " + successCount);
		for (TPODInputFilter filter : inputParameters.getInputFilters())
		{
			Integer filteredCount = filteredByType.get(filter.getTpodFilter());
			notes.add("Filtered out by " + filter.getTpodFilter().toString() + ": " + filteredCount);
		}

		String endpointTypes = inputParameters.getBmdEndpointTypes().stream().map(Enum::name)
				.collect(Collectors.joining(","));

		notes.add("BMD Endpoint Types: " + endpointTypes);
		analysisInfo.setNotes(notes);

		for (TPODMethodParameter method : inputParameters.getMethodParameters())
		{

			notes.add("Method used: " + method.getMethod().toString() + ": " + method.getParameterString());

		}

		String resultsName = "TPOD";
		TPODAnalysisResults results = new TPODAnalysisResults();
		results.setName(resultsName);
		results.setAnalysisInfo(analysisInfo);
		List<TPODAnalysisResult> resultList = new ArrayList<>();
		// loop through each bmdendpoint type
		for (BMDEndpointType bmdEndpointType : inputParameters.getBmdEndpointTypes())
		{

			// TODO: for the bmdendpoint type, grab that from the list of category results.

			List<CategoryAnalysisResult> valueList = new ArrayList<>();

			for (CategoryAnalysisResult res : filteredResults)
			{
				if (bmdEndpointType.equals(BMDEndpointType.BMD_10TH_PERCENTILE)
						&& res.getBmdTenthPercentileTotalGenes() != null)
					valueList.add(res);
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_5TH_PERCENTILE)
						&& res.getBmdFifthPercentileTotalGenes() != null)
					valueList.add(res);
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEAN) && res.getBmdMean() != null)
					valueList.add(res);
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEDIAN) && res.getBmdMedian() != null)
					valueList.add(res);
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MINIMUM) && res.getBmdMinimum() != null)
					valueList.add(res);

			}

			if (bmdEndpointType.equals(BMDEndpointType.BMD_10TH_PERCENTILE))
				valueList.sort((v1, v2) -> v1.getBmdTenthPercentileTotalGenes()
						.compareTo(v2.getBmdTenthPercentileTotalGenes()));
			else if (bmdEndpointType.equals(BMDEndpointType.BMD_5TH_PERCENTILE))
				valueList.sort((v1, v2) -> v1.getBmdFifthPercentileTotalGenes()
						.compareTo(v2.getBmdFifthPercentileTotalGenes()));
			else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEAN))
				valueList.sort((v1, v2) -> v1.getBmdMean().compareTo(v2.getBmdMean()));
			else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEDIAN))
				valueList.sort((v1, v2) -> v1.getBmdMedian().compareTo(v2.getBmdMedian()));
			else if (bmdEndpointType.equals(BMDEndpointType.BMD_MINIMUM))
				valueList.sort((v1, v2) -> v1.getBmdMinimum().compareTo(v2.getBmdMinimum()));

			double[] valueArray = new double[valueList.size()];
			for (int i = 0; i < valueList.size(); i++)
			{

				if (bmdEndpointType.equals(BMDEndpointType.BMD_10TH_PERCENTILE))
					valueArray[i] = valueList.get(i).getBmdTenthPercentileTotalGenes();
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_5TH_PERCENTILE))
					valueArray[i] = valueList.get(i).getBmdFifthPercentileTotalGenes();
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEAN))
					valueArray[i] = valueList.get(i).getBmdMean();
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEDIAN))
					valueArray[i] = valueList.get(i).getBmdMedian();
				else if (bmdEndpointType.equals(BMDEndpointType.BMD_MINIMUM))
					valueArray[i] = valueList.get(i).getBmdMinimum();

			}

			// now loop through the tpod methods that will be used.
			for (TPODMethodParameter method : inputParameters.getMethodParameters())
			{

				System.out.println(method.getMethod());

				TPODAnalysisResult tpodResult = new TPODAnalysisResult();

				tpodResult.setBmdEndpointType(bmdEndpointType);

				tpodResult.setTpodMethod(method.getMethod());

				CalcResult res = null;

				// given a method, calculate the tPOD
				if (method instanceof LCRDParameters)
				{
					LCRDParameters lp = (LCRDParameters) method;
					res = LCRD.calculate(valueArray, lp.getSpacingRatio(), lp.getRunLength());
				}

				else if (method instanceof NthRankParameters)
				{
					NthRankParameters nrp = (NthRankParameters) method;
					res = NthRank.rankSorted(valueArray, nrp.getRank());
				}
				else if (method instanceof NthPercentParameters)
				{
					NthPercentParameters npp = (NthPercentParameters) method;
					res = NthPercentile.percentile(valueArray, npp.getPercent());

				}

				// else if (method instanceof FirstModeParameters)
				// {
				// FirstModeParameters fm = (FirstModeParameters) method;
				// double res = firstMode(valueArray);
				// System.out.println(res);
				// }
				// else if (method instanceof MaxCurveParameters)
				// {
				// MaxCurveParameters mc = (MaxCurveParameters) method;
				// }

				if (res != null)
				{
					tpodResult.setBmd(res.getValue());

					if (bmdEndpointType.equals(BMDEndpointType.BMD_10TH_PERCENTILE))
					{
						tpodResult.setBmdl(valueList.get(res.getIndex()).getBmdlTenthPercentileTotalGenes());
						tpodResult.setBmdu(valueList.get(res.getIndex()).getBmduTenthPercentileTotalGenes());
					}
					else if (bmdEndpointType.equals(BMDEndpointType.BMD_5TH_PERCENTILE))
					{
						tpodResult.setBmdl(valueList.get(res.getIndex()).getBmdlFifthPercentileTotalGenes());
						tpodResult.setBmdu(valueList.get(res.getIndex()).getBmduFifthPercentileTotalGenes());
					}
					else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEAN))
					{
						tpodResult.setBmdl(valueList.get(res.getIndex()).getBmdlMean());
						tpodResult.setBmdu(valueList.get(res.getIndex()).getBmduMean());
					}
					else if (bmdEndpointType.equals(BMDEndpointType.BMD_MEDIAN))
					{
						tpodResult.setBmdl(valueList.get(res.getIndex()).getBmdlMedian());
						tpodResult.setBmdu(valueList.get(res.getIndex()).getBmduMedian());
					}
					else if (bmdEndpointType.equals(BMDEndpointType.BMD_MINIMUM))
					{
						tpodResult.setBmdl(valueList.get(res.getIndex()).getBmdlMinimum());
						tpodResult.setBmdu(valueList.get(res.getIndex()).getBmduMinimum());
					}

					resultList.add(tpodResult);
				}

			}

		}

		results.setTpodAnalysisResults(resultList);
		results.setCategoryAnalysisResults(processableData);

		long endTime = System.currentTimeMillis();

		long runTime = endTime - startTime;
		analysisInfo.getNotes().add("Total Run Time: " + runTime / 1000 + " seconds");

		return results;
	}

	@Override
	public boolean cancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public static double firstMode(double[] x)
	{
		return firstMode(x, 0.055);
	}

	public static double firstMode(double[] xRaw, double minSize)
	{
		double[] x = xRaw.clone();
		Arrays.sort(x); // order(x, decreasing = FALSE)

		double[] logX = new double[x.length];
		for (int i = 0; i < x.length; i++)
		{
			logX[i] = Math.log10(x[i]);
		}

		ModeAntimodeResult result = ModeDetector.modeAntimode(logX, minSize);

		if (result.modes.length == 0)
		{
			throw new IllegalStateException("mode.antimode returned no modes");
		}

		double firstModeLog = result.modes[0];
		return Math.pow(10, firstModeLog);
	}

}
