package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.CombinedDataSet;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class TPODAnalysisDataCombinedSelectedEvent extends BMDExpressEventBase<CombinedDataSet>
{

	public TPODAnalysisDataCombinedSelectedEvent(CombinedDataSet payload)
	{
		super(payload);
	}
}
