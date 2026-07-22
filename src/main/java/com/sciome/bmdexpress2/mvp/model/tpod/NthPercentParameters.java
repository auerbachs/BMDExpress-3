package com.sciome.bmdexpress2.mvp.model.tpod;

public class NthPercentParameters extends TPODMethodParameter
{
	private double percent;

	public double getPercent()
	{
		return percent;
	}

	public void setPercent(double percent)
	{
		this.percent = percent;
	}

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.NTH_PERCENTILE;
	}

}
