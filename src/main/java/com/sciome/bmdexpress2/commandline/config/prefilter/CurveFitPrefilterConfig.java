package com.sciome.bmdexpress2.commandline.config.prefilter;

import java.util.ArrayList;
import java.util.List;

import com.sciome.bmdexpress2.commandline.config.bmds.BMDSModelConfig;

public class CurveFitPrefilterConfig extends PrefilterConfig
{
	private List<BMDSModelConfig> modelConfigs = new ArrayList<>();
	private Boolean constantVariance = true;

	private Double bmrFactor = 1.021;
	private Double poly2BmrFactor = 1.021;

	// this could be dose response data or prefiltered data.
	private String inputCategory;

	public List<BMDSModelConfig> getModelConfigs()
	{
		return modelConfigs;
	}

	public void setModelConfigs(List<BMDSModelConfig> modelConfigs)
	{
		this.modelConfigs = modelConfigs;
	}

	public Boolean getConstantVariance()
	{
		return constantVariance;
	}

	public void setConstantVariance(Boolean constantVariance)
	{
		this.constantVariance = constantVariance;
	}

	public Double getBmrFactor()
	{
		return bmrFactor;
	}

	public void setBmrFactor(Double bmrFactor)
	{
		this.bmrFactor = bmrFactor;
	}

	public Double getPoly2BmrFactor()
	{
		return poly2BmrFactor;
	}

	public void setPoly2BmrFactor(Double poly2BmrFactor)
	{
		this.poly2BmrFactor = poly2BmrFactor;
	}

	public String getInputCategory()
	{
		return inputCategory;
	}

	public void setInputCategory(String inputCategory)
	{
		this.inputCategory = inputCategory;
	}

}