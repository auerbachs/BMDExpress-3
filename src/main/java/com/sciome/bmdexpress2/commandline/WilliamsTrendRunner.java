package com.sciome.bmdexpress2.commandline;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.prefilter.WilliamsTrendResults;
import com.sciome.bmdexpress2.service.PrefilterService;
import com.sciome.commons.interfaces.SimpleProgressUpdater;

public class WilliamsTrendRunner implements SimpleProgressUpdater
{
	String message = "";

	public WilliamsTrendResults runWilliamsTrendFilter(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, boolean filterOutControlGenes, boolean useFoldFilter,
			double foldFilterValue, int numPermutations, double pValueLoel, double foldChangeLoel,
			String outputName, int numThreads, boolean tTest, BMDProject project)
	{
		PrefilterService service = new PrefilterService();
		WilliamsTrendResults results = service.williamsTrendAnalysis(processableData, pCutOff,
				multipleTestingCorrection, filterOutControlGenes, useFoldFilter, foldFilterValue,
				numPermutations, pValueLoel, foldChangeLoel, numThreads, this, tTest);

		if (outputName != null)
			results.setName(outputName);
		else
			project.giveBMDAnalysisUniqueName(results, results.getName());
		return results;

	}

	@Override
	public void setMessage(String arg0)
	{
		message = arg0;

	}

	@Override
	public void setProgress(double value)
	{
		// System.out.printf("\r%s", message + ": " + String.valueOf(value) + " ");

	}
}
