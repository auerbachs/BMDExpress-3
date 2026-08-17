package com.sciome.bmdexpress2.mvp.model.tpod;

public class TPODInputFilter
{

	TPODAnalysisFilter tpodFilter;
	Number value;

	public TPODInputFilter(TPODAnalysisFilter tpodFilter, Number value)
	{
		super();
		this.tpodFilter = tpodFilter;
		this.value = value;
	}

	public TPODAnalysisFilter getTpodFilter()
	{
		return tpodFilter;
	}

	public void setTpodFilter(TPODAnalysisFilter tpodFilter)
	{
		this.tpodFilter = tpodFilter;
	}

	public Number getValue()
	{
		return value;
	}

	public void setValue(Number value)
	{
		this.value = value;
	}

}
