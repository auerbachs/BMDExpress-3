package com.sciome.bmdexpress2.commandline.config.tpod;

import java.util.List;

public class TPODGeneSetConfig extends TPODConfig
{
	// >=
	private Integer genesThatPassedFilter;

	// <=
	private Double fishersExactRightPValueFilter;

	// >=
	private Double percentageFilter;

	// -1 or 1. (down or up)
	private Integer overallDirectionFilter;

	private List<String> bmdMetricTypes;

	public List<String> getBmdMetricTypes()
	{
		return bmdMetricTypes;
	}

	public void setBmdMetricTypes(List<String> bmdMetricTypes)
	{
		this.bmdMetricTypes = bmdMetricTypes;
	}

	public Integer getGenesThatPassedFilter()
	{
		return genesThatPassedFilter;
	}

	public void setGenesThatPassedFilter(Integer genesThatPassedFilters)
	{
		this.genesThatPassedFilter = genesThatPassedFilters;
	}

	public Double getFishersExactRightPValueFilter()
	{
		return fishersExactRightPValueFilter;
	}

	public void setFishersExactRightPValueFilter(Double fishersExactRightPValue)
	{
		this.fishersExactRightPValueFilter = fishersExactRightPValue;
	}

	public Double getPercentageFilter()
	{
		return percentageFilter;
	}

	public void setPercentageFilter(Double percentage)
	{
		this.percentageFilter = percentage;
	}

	public Integer getOverallDirectionFilter()
	{
		return overallDirectionFilter;
	}

	public void setOverallDirectionFilter(Integer overallDirection)
	{
		this.overallDirectionFilter = overallDirection;
	}
}
