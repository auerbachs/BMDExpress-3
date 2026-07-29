package com.sciome.bmdexpress2.mvp.model.tpod;

public enum TPODAnalysisFilter
{

	GENS_PASS_ALL_FILTERS("Genes That Passed All Filters)"), PERCENTAGE("Percentage"), FISHERS_RIGHT_P_VALUE(
			"Fisher’s Exact Right P-Value"), OVERALLDIRECTION("Overall Direction");

	private final String label;

	TPODAnalysisFilter(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}