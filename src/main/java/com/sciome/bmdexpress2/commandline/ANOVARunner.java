package com.sciome.bmdexpress2.commandline;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.prefilter.OneWayANOVAResults;
import com.sciome.bmdexpress2.service.PrefilterService;
import com.sciome.commons.interfaces.SimpleProgressUpdater;

/*
 * use the presenter to run the one way anova as it would be run from the view. FileChooser 
 */
public class ANOVARunner implements SimpleProgressUpdater
{
	String message = "";

	public OneWayANOVAResults runANOVAFilter(IStatModelProcessable processableData, double pCutOff,
			boolean multipleTestingCorrection, boolean filterOutControlGenes, boolean useFoldFilter,
			double foldFilterValue, double pValueLoel, double foldChangeLoel, String outputName,
			int numThreads, boolean tTest, BMDProject project)
	{
		PrefilterService service = new PrefilterService();
		OneWayANOVAResults results = service.oneWayANOVAAnalysis(processableData, pCutOff,
				multipleTestingCorrection, filterOutControlGenes, useFoldFilter, foldFilterValue, pValueLoel,
				foldChangeLoel, numThreads, this, tTest);

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
		// System.out.printf("\r" + message + ": " + String.valueOf(value));

	}
}
