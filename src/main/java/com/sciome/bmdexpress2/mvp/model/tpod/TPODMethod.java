package com.sciome.bmdexpress2.mvp.model.tpod;

public enum TPODMethod
{

	MAX_CURVATURE("Max Curvature (inflection point)"), LCRD(
			"Lowest Consistent Response Dose (LCRD)"), NTH_PERCENTILE("Nth Percentile"), NTH_RANK(
					"Nth Rank"), FIRST_MODE("First Mode (distribution peak)"), FIRST_GENESET("First Geneset");

	private final String label;

	TPODMethod(String label)
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