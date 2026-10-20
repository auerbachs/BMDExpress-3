package com.sciome.bmdexpress2.mvp.model.tpod;

public class FirstModeParameters extends TPODMethodParameter
{
	private double minSize;

	public double getMinSize()
	{
		return minSize;
	}

	public void setMinSize(double minSize)
	{
		this.minSize = minSize;
	}

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.FIRST_MODE;
	}

	@Override
	public String getParameterString()
	{
		return "minSize=" + minSize;
	}

}
