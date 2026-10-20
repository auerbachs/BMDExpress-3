package com.sciome.bmdexpress2.mvp.model.tpod;

public class TPODGeneSetInput extends TPODInput
{
	private Boolean bmd5th;
	private Boolean bmd10th;
	private Boolean bmdMedian;
	private Boolean bmdMean;
	private Boolean bmdMin;

	private Boolean genesThatPassedAllFilters = true;
	private Integer genesThatPassedAllFiltersValue = 3;

	private Boolean fishersExactRightPFilter = true;
	private Double fishersExactRightPFilterValue = 0.05;

	private Boolean percentageFilter = true;
	private Double percentageFilterValue = 5.0;

	private Boolean overallDirectionFilter = true;
	private Integer overallDirectionFilterValue = 1;

	public Boolean getBmd5th()
	{
		return bmd5th;
	}

	public void setBmd5th(Boolean bmd5th)
	{
		this.bmd5th = bmd5th;
	}

	public Boolean getBmd10th()
	{
		return bmd10th;
	}

	public void setBmd10th(Boolean bmd10th)
	{
		this.bmd10th = bmd10th;
	}

	public Boolean getBmdMedian()
	{
		return bmdMedian;
	}

	public void setBmdMedian(Boolean bmdMedian)
	{
		this.bmdMedian = bmdMedian;
	}

	public Boolean getBmdMean()
	{
		return bmdMean;
	}

	public void setBmdMean(Boolean bmdMean)
	{
		this.bmdMean = bmdMean;
	}

	public Boolean getBmdMin()
	{
		return bmdMin;
	}

	public void setBmdMin(Boolean bmdMin)
	{
		this.bmdMin = bmdMin;
	}

	public Boolean getGenesThatPassedAllFilters()
	{
		return genesThatPassedAllFilters;
	}

	public void setGenesThatPassedAllFilters(Boolean genesThatPassedAllFilters)
	{
		this.genesThatPassedAllFilters = genesThatPassedAllFilters;
	}

	public Integer getGenesThatPassedAllFiltersValue()
	{
		return genesThatPassedAllFiltersValue;
	}

	public void setGenesThatPassedAllFiltersValue(Integer genesThatPassedAllFiltersValue)
	{
		this.genesThatPassedAllFiltersValue = genesThatPassedAllFiltersValue;
	}

	public Boolean getFishersExactRightPFilter()
	{
		return fishersExactRightPFilter;
	}

	public void setFishersExactRightPFilter(Boolean fishersExactRightPFilter)
	{
		this.fishersExactRightPFilter = fishersExactRightPFilter;
	}

	public Double getFishersExactRightPFilterValue()
	{
		return fishersExactRightPFilterValue;
	}

	public void setFishersExactRightPFilterValue(Double fishersExactRightPFilterValue)
	{
		this.fishersExactRightPFilterValue = fishersExactRightPFilterValue;
	}

	public Boolean getPercentageFilter()
	{
		return percentageFilter;
	}

	public void setPercentageFilter(Boolean percentageFilter)
	{
		this.percentageFilter = percentageFilter;
	}

	public Double getPercentageFilterValue()
	{
		return percentageFilterValue;
	}

	public void setPercentageFilterValue(Double percentageFilterValue)
	{
		this.percentageFilterValue = percentageFilterValue;
	}

	public Boolean getOverallDirectionFilter()
	{
		return overallDirectionFilter;
	}

	public void setOverallDirectionFilter(Boolean overallDirectionFilter)
	{
		this.overallDirectionFilter = overallDirectionFilter;
	}

	public Integer getOverallDirectionFilterValue()
	{
		return overallDirectionFilterValue;
	}

	public void setOverallDirectionFilterValue(Integer overallDirectionFilterValue)
	{
		this.overallDirectionFilterValue = overallDirectionFilterValue;
	}

}
