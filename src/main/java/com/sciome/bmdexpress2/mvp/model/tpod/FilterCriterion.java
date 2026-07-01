package com.sciome.bmdexpress2.mvp.model.tpod;

public enum FilterCriterion
{

	GENES_PASSED_ALL_FILTERS_GE("Genes That Passed All Filters >="),

	PERCENTAGE_GE("Percentage >="),

	FISHERS_EXACT_RIGHT_P_VALUE_LE("Fisher’s Exact Right P-Value <="),

	OVERALL_DIRECTION("Overall Direction");

	private final String label;

	FilterCriterion(String label)
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
