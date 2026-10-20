package com.sciome.bmdexpress2.mvp.model.tpod;

public class TPODInput
{

	private Boolean lcrdMethod = true;
	private Double spacingRatio = 1.67;
	private Integer runLength = 10;

	private Boolean nthRankMethod = true;
	private Integer rank = 25;

	private Boolean nthPercentileMethod = true;
	private Double percent = 5.0;
	private Boolean allGeneSets = false;

	private Boolean lowestGeneSetMethod = true;

	public TPODInput()
	{
		super();

	}

	public Boolean getLcrdMethod()
	{
		return lcrdMethod;
	}

	public void setLcrdMethod(Boolean lcrdMethod)
	{
		this.lcrdMethod = lcrdMethod;
	}

	public Double getSpacingRatio()
	{
		return spacingRatio;
	}

	public void setSpacingRatio(Double spacingRatio)
	{
		this.spacingRatio = spacingRatio;
	}

	public Integer getRunLength()
	{
		return runLength;
	}

	public void setRunLength(Integer runLength)
	{
		this.runLength = runLength;
	}

	public Boolean getNthRankMethod()
	{
		return nthRankMethod;
	}

	public void setNthRankMethod(Boolean nthRankMethod)
	{
		this.nthRankMethod = nthRankMethod;
	}

	public Integer getRank()
	{
		return rank;
	}

	public void setRank(Integer rank)
	{
		this.rank = rank;
	}

	public Boolean getNthPercentileMethod()
	{
		return nthPercentileMethod;
	}

	public void setNthPercentileMethod(Boolean nthPercentileMethod)
	{
		this.nthPercentileMethod = nthPercentileMethod;
	}

	public Double getPercent()
	{
		return percent;
	}

	public void setPercent(Double percent)
	{
		this.percent = percent;
	}

	public Boolean getAllGeneSets()
	{
		return allGeneSets;
	}

	public void setAllGeneSets(Boolean allGeneSets)
	{
		this.allGeneSets = allGeneSets;
	}

	public Boolean getLowestGeneSetMethod()
	{
		return lowestGeneSetMethod;
	}

	public void setLowestGeneSetMethod(Boolean lowestGeneSetMethod)
	{
		this.lowestGeneSetMethod = lowestGeneSetMethod;
	}

}
