package com.sciome.bmdexpress2.commandline.config.tpod;

public class NthPercentileConfig extends TPODMethodConfig
{

	private Boolean allGeneSetsPlatform = false;
	private Double percentile;

	public Boolean getAllGeneSetsPlatform()
	{
		return allGeneSetsPlatform;
	}

	public void setAllGeneSetsPlatform(Boolean allGeneSetsPlatform)
	{
		this.allGeneSetsPlatform = allGeneSetsPlatform;
	}

	public Double getPercentile()
	{
		return percentile;
	}

	public void setPercentile(Double percentile)
	{
		this.percentile = percentile;
	}

}
