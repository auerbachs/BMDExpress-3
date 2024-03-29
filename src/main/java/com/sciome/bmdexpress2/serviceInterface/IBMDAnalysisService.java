package com.sciome.bmdexpress2.serviceInterface;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.IStatModelProcessable;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.sciome.bmdexpress2.util.bmds.ModelSelectionParameters;
import com.sciome.bmdexpress2.util.bmds.shared.StatModel;
import com.sciome.bmdexpress2.util.curvep.GCurvePInputParameters;

public interface IBMDAnalysisService
{
	public BMDResult bmdAnalysis(IStatModelProcessable processableData, ModelInputParameters inputParameters,
			ModelSelectionParameters modelSelectionParameters, List<StatModel> modelsToRun, String tmpFolder,
			IBMDSToolProgress progressUpdater);

	public boolean cancel();

	public BMDResult bmdAnalysisGCurveP(IStatModelProcessable processableData,
			GCurvePInputParameters inputParameters, IBMDSToolProgress me);

	public BMDResult bmdAnalysisLaPlaceMA(IStatModelProcessable processableData,
			ModelInputParameters inputParameters, List<StatModel> modelsToRun, IBMDSToolProgress me);

	public BMDResult bmdAnalysisMCMCMA(IStatModelProcessable processableData,
			ModelInputParameters inputParameters, List<StatModel> modelsToRun, IBMDSToolProgress me);

	int isStepFunction(List<Float> responses, StatResult bestResult, DoseResponseExperiment doseResponseExp,
			double threshold);
}
