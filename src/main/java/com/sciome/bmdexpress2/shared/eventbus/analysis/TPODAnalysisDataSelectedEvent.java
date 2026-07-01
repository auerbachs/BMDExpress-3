package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class TPODAnalysisDataSelectedEvent extends BMDExpressEventBase<TPODAnalysisResults>
{

	public TPODAnalysisDataSelectedEvent(TPODAnalysisResults payload)
	{
		super(payload);
	}
}
