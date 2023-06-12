package com.sciome.bmdexpress2.mvp.model;

public class DoseGroup
{
	private Double dose;
	private int count = 0;
	private Double responseMean;

	public Double getDose()
	{
		return dose;
	}

	public void setDose(Double dose)
	{
		this.dose = dose;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public Double getResponseMean()
	{
		return responseMean;
	}

	public void setResponseMean(Double mean)
	{
		this.responseMean = mean;
	}

	public void incrementCount()
	{
		count++;

	}

}
