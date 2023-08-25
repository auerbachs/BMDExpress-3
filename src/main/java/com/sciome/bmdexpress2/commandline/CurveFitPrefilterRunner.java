package com.sciome.bmdexpress2.commandline;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.math3.util.Precision;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.service.PrefilterService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;

public class CurveFitPrefilterRunner implements IBMDSToolProgress
{
	public CurveFitPrefilterResults runCurveFitPrefilter(IStatModelProcessable processableData,
			boolean useFoldFilter, double foldFilterValue, double pValueLoel, double foldChangeLoel,
			String outputName, int numThreads, boolean tTest, List<StatModel> modelsToRun, Double bmrFactor,
			Double poly2BmrFactor, int constantVariance, BMDProject project)
	{
		PrefilterService service = new PrefilterService();
		CurveFitPrefilterResults results = service.curveFitPrefilterAnalysis(processableData, useFoldFilter,
				foldFilterValue, pValueLoel, foldChangeLoel, numThreads, this, tTest, modelsToRun, bmrFactor,
				poly2BmrFactor, constantVariance);

		if (outputName != null)
			results.setName(outputName);
		else
			project.giveBMDAnalysisUniqueName(results, results.getName());
		return results;

	}

	@Override
	public void updateProgress(String label, double value)
	{

		double pround = Precision.round(value, 2, BigDecimal.ROUND_UP);
		int rounded = (int) (pround * 100.0);
		if (rounded % 10 == 0)
			System.out.printf("\r%s",
					label + ": " + String.valueOf(rounded) + "%                               ");

	}

	@Override
	public void clearProgress()
	{
		System.out.println();

	}
}
