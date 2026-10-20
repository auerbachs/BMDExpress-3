package com.sciome.bmdexpress2.commandline;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.service.TPODAnalysisService;

public class TPODAnalysisRunner
{

	public TPODAnalysisResults runTPODAnalysis(CategoryAnalysisResults catResult,
			TPODInputParameters genesetParams, TPODInputParameters geneLevelParams)
	{

		TPODAnalysisService service = new TPODAnalysisService();
		return service.tpodAnalysis(catResult, genesetParams, geneLevelParams, null);
	}

}
