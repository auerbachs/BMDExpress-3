package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class TPODAnalysisDataLoadedEvent extends BMDExpressEventBase<TPODAnalysisResults>
{

	public TPODAnalysisDataLoadedEvent(TPODAnalysisResults payload)
	{
		super(payload);
	}
}
