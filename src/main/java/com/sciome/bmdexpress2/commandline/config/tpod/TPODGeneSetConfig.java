package com.sciome.bmdexpress2.commandline.config.tpod;

import java.util.List;

public class TPODGeneSetConfig extends TPODConfig
{
	// >=
	private Integer genesThatPassedFilters;

	// <=
	private Double fishersExactRightPValue;

	// >=
	private Double percentage;

	// -1 or 1. (down or up)
	private Integer overallDirection;

	private List<String> bmdMetricTypes;

	public List<String> getBmdMetricTypes()
	{
		return bmdMetricTypes;
	}

	public void setBmdMetricTypes(List<String> bmdMetricTypes)
	{
		this.bmdMetricTypes = bmdMetricTypes;
	}

	public Integer getGenesThatPassedFilters()
	{
		return genesThatPassedFilters;
	}

	public void setGenesThatPassedFilters(Integer genesThatPassedFilters)
	{
		this.genesThatPassedFilters = genesThatPassedFilters;
	}

	public Double getFishersExactRightPValue()
	{
		return fishersExactRightPValue;
	}

	public void setFishersExactRightPValue(Double fishersExactRightPValue)
	{
		this.fishersExactRightPValue = fishersExactRightPValue;
	}

	public Double getPercentage()
	{
		return percentage;
	}

	public void setPercentage(Double percentage)
	{
		this.percentage = percentage;
	}

	public Integer getOverallDirection()
	{
		return overallDirection;
	}

	public void setOverallDirection(Integer overallDirection)
	{
		this.overallDirection = overallDirection;
	}
}
