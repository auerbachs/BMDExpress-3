package com.sciome.bmdexpress2.service;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public class TPODAnalysisService implements ITPODService
{

	@Override
	public TPODAnalysisResult tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
