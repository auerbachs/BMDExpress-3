package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.shared.TPODAnalysisEnum;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class TPODAnalysisRequestEvent extends BMDExpressEventBase<TPODAnalysisEnum>
{

	public TPODAnalysisRequestEvent(TPODAnalysisEnum payload)
	{
		super(payload);
	}
}
