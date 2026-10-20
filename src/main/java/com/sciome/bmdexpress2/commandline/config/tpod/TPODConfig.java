package com.sciome.bmdexpress2.commandline.config.tpod;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({ @Type(value = TPODIndividualGeneConfig.class, name = "individualgene"),
		@Type(value = TPODGeneSetConfig.class, name = "geneset") })
public abstract class TPODConfig
{

	public static String BMD5th = "fifth";
	public static String BMD10th = "tenth";
	public static String BMDMEDIAN = "median";
	public static String BMDMEAN = "mean";
	public static String BMDMIN = "min";

	// name of bmdanalysis to cateogorize
	private String inputName;

	// name of output for the analysis
	private String outputName;

	private List<TPODMethodConfig> tpodMethodConfig;

	public String getInputName()
	{
		return inputName;
	}

	public void setInputName(String inputName)
	{
		this.inputName = inputName;
	}

	public String getOutputName()
	{
		return outputName;
	}

	public void setOutputName(String outputName)
	{
		this.outputName = outputName;
	}

	public List<TPODMethodConfig> getTpodMethodConfig()
	{
		return tpodMethodConfig;
	}

	public void setTpodMethodConfig(List<TPODMethodConfig> tpodMethodConfig)
	{
		this.tpodMethodConfig = tpodMethodConfig;
	}

}
