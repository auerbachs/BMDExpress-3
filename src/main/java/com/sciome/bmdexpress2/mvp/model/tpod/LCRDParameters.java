package com.sciome.bmdexpress2.mvp.model.tpod;

public class LCRDParameters extends TPODMethodParameter
{
	private double spacingRatio;
	private int runLength;

	public double getSpacingRatio()
	{
		return spacingRatio;
	}

	public void setSpacingRatio(double spacingRatio)
	{
		this.spacingRatio = spacingRatio;
	}

	public int getRunLength()
	{
		return runLength;
	}

	public void setRunLength(int runLength)
	{
		this.runLength = runLength;
	}

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.LCRD;
	}

	@Override
	public String getParameterString()
	{
		return "spacingRatio=" + spacingRatio + ", runLenght=" + runLength;
	}

}
