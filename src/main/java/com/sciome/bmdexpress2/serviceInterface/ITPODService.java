package com.sciome.bmdexpress2.serviceInterface;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public interface ITPODService
{
	public TPODAnalysisResult tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater);

	public boolean cancel();

}
