package com.sciome.bmdexpress2.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.info.AnalysisInfo;
import com.sciome.bmdexpress2.mvp.model.tpod.BMDEndpointType;
import com.sciome.bmdexpress2.mvp.model.tpod.LCRDParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthPercentParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthRankParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODMethodParameter;
import com.sciome.bmdexpress2.service.tpod.CalcResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeAntimodeResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeDetector;
import com.sciome.bmdexpress2.service.tpod.lcrd.LCRD;
import com.sciome.bmdexpress2.service.tpod.nthperc.NthPercentile;
import com.sciome.bmdexpress2.service.tpod.nthrank.NthRank;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public class TPODAnalysisService implements ITPODService
{

	@Override
	public TPODAnalysisResults tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater)
	{

		// TODO: first we need to do some filtration.

		long startTime = System.currentTimeMillis();
		AnalysisInfo analysisInfo = new AnalysisInfo();
		List<String> notes = new ArrayList<>();

		analysisInfo.setNotes(notes);

		String resultsName = "TPOD";
		TPODAnalysisResults results = new TPODAnalysisResults();
		results.setName(resultsName);
		results.setAnalysisInfo(analysisInfo);
		List<TPODAnalysisResult> resultList = new ArrayList<>();
		// loop through each bmdendpoint type
		for (BMDEndpointType bmdEndpointType : inputParameters.getBmdEndpointTypes())
		{

			System.out.println(bmdEndpointType.toString());

			// TODO: for the bmdendpoint type, grab that from the list of category results.

			List<CategoryAnalysisResult> valueList = new ArrayList<>();

			for (CategoryAnalysisResult res : processableData.getCategoryAnalsyisResults())
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
					System.out.println(res.getValue());
				}

				else if (method instanceof NthRankParameters)
				{
					NthRankParameters nrp = (NthRankParameters) method;
					res = NthRank.rankSorted(valueArray, nrp.getRank());
					System.out.println(res.getValue());
				}
				else if (method instanceof NthPercentParameters)
				{
					NthPercentParameters npp = (NthPercentParameters) method;
					res = NthPercentile.percentile(valueArray, npp.getPercent());
					System.out.println(res.getValue());

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
