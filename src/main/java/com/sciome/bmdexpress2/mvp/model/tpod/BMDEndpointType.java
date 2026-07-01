package com.sciome.bmdexpress2.mvp.model.tpod;

public enum BMDEndpointType
{

	BMD_5TH_PERCENTILE("BMD at 5th Percentile of Total Genes"),

	BMD_10TH_PERCENTILE("BMD at 10th Percentile of Total Genes"),

	BMD_MEDIAN("BMD Median"),

	BMD_MEAN("BMD Mean"),

	BMD_MINIMUM("BMD Minimum");

	private final String label;

	BMDEndpointType(String label)
	{
		this.label = label;
	}

	/**
	 * Human-readable label for UI components (ComboBox, dropdowns, etc.)
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Useful for UI frameworks that rely on toString() for display.
	 */
	@Override
	public String toString()
	{
		return label;
	}
}
