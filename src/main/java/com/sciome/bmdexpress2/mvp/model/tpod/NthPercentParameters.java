package com.sciome.bmdexpress2.mvp.model.tpod;

public class NthPercentParameters extends TPODMethodParameter
{
	private double percent;
	private boolean allGenes;

	public double getPercent()
	{
		return percent;
	}

	public void setPercent(double percent)
	{
		this.percent = percent;
	}

	public boolean isAllGenes()
	{
		return allGenes;
	}

	public void setAllGenes(boolean allGenes)
	{
		this.allGenes = allGenes;
	}

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.NTH_PERCENTILE;
	}

	@Override
	public String getParameterString()
	{
		return "percent=" + percent + ";all_genes=" + allGenes;
	}

}
