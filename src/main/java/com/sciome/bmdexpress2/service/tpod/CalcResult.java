package com.sciome.bmdexpress2.service.tpod;

public class CalcResult
{
	private int index;
	private Double value;

	public CalcResult(int index, Double value)
	{
		super();
		this.index = index;
		this.value = value;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public Double getValue()
	{
		return value;
	}

	public void setValue(Double value)
	{
		this.value = value;
	}

}
