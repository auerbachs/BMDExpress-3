package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class TPODAnalysisDataSelectedForProcessingEvent extends BMDExpressEventBase<TPODAnalysisResults>
{

	public TPODAnalysisDataSelectedForProcessingEvent(TPODAnalysisResults payload)
	{
		super(payload);
	}
}
