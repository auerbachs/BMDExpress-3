package com.sciome.bmdexpress2.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.prefilter.CurveFitPrefilterResults;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class CurveFitPrefilterDataSelectedEvent extends BMDExpressEventBase<CurveFitPrefilterResults>
{
	public CurveFitPrefilterDataSelectedEvent(CurveFitPrefilterResults payload)
	{
		super(payload);
	}
}
