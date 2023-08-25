package com.sciome.bmdexpress2.commandline;

import java.util.List;

import org.apache.commons.math3.util.Precision;

import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.service.BMDAnalysisService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;

/*
 * System.out.print
 */
public class BMDAnalysisRunner implements IBMDSToolProgress
{

	public BMDResult runBMDAnalysis(IStatModelProcessable processableData,
			ModelSelectionParameters modelSelectionParameters, List<StatModel> modelsToRun,
			ModelInputParameters inputParameters, String tmpFolder)
	{
		BMDAnalysisService service = new BMDAnalysisService();
		return service.bmdAnalysis(processableData, inputParameters, modelSelectionParameters, modelsToRun,
				tmpFolder, this);
	}

	public BMDResult runMAAnalysis(IStatModelProcessable processableData, List<StatModel> modelsToRun,
			ModelInputParameters inputParameters, boolean laplace)
	{
		BMDAnalysisService service = new BMDAnalysisService();
		if (laplace)
			return service.bmdAnalysisLaPlaceMA(processableData, inputParameters, modelsToRun, this);
		else
			return service.bmdAnalysisMCMCMA(processableData, inputParameters, modelsToRun, this);
	}

	@Override
	public void updateProgress(String label, double value)
	{
		int rounded = (int) (Precision.round(value, 3) * 100.0);
		if (rounded % 10 == 0)
			System.out.printf("\r%s",
					label + ": " + String.valueOf(rounded) + "%                              ");

	}

	@Override
	public void clearProgress()
	{
		System.out.println();

	}
}
