package com.sciome.bmdexpress2.commandline;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.prefilter.OriogenResults;
import com.sciome.bmdexpress2.service.PrefilterService;
import com.sciome.commons.interfaces.SimpleProgressUpdater;

public class OriogenRunner implements SimpleProgressUpdater
{

	private String message = "";

	public OriogenResults runOriogenFilter(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, boolean mpc, int initialBootstraps, int maxBootstraps,
			double s0Adjustment, boolean filterOutControlGenes, boolean useFoldFilter, double foldFilterValue,
			double pValueLoel, double foldChangeLoel, String outputName, int numThreads, boolean tTest,
			BMDProject project)
	{
		PrefilterService service = new PrefilterService();
		OriogenResults results = service.oriogenAnalysis(processableData, pCutOff, multipleTestingCorrection,
				initialBootstraps, maxBootstraps, s0Adjustment, filterOutControlGenes, useFoldFilter,
				foldFilterValue, pValueLoel, foldChangeLoel, numThreads, this, tTest);

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
