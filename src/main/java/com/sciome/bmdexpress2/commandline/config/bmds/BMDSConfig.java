package com.sciome.bmdexpress2.commandline.config.bmds;

import java.util.ArrayList;
import java.util.List;

public class BMDSConfig
{

	// private Integer method = 2; // 1 for 2.7.x; 2 for 3.x.x. default to 2 which uses the dlls
	private List<BMDSModelConfig> modelConfigs = new ArrayList<>();
	private BMDSBestModelSelectionConfig bmdsBestModelSelection;
	private BMDSInputConfig bmdsInputConfig;

	// this could be dose response data or prefiltered data.
	private String inputCategory;
	// name of the data set that is being input to bmds analysis.
	private String inputName;

	// name of output for the analysis
	private String outputName;

	// private Integer killTime = 600;

	private Integer numberOfThreads = 1;

	// private String tmpFolder;

	public String getInputCategory()
	{
		return inputCategory;
	}

	public void setInputCategory(String inputCategory)
	{
		this.inputCategory = inputCategory;
	}

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

	public List<BMDSModelConfig> getModelConfigs()
	{
		return modelConfigs;
	}

	public void setModelConfigs(List<BMDSModelConfig> modelConfigs)
	{
		this.modelConfigs = modelConfigs;
	}

	public BMDSBestModelSelectionConfig getBmdsBestModelSelection()
	{
		return bmdsBestModelSelection;
	}

	public void setBmdsBestModelSelection(BMDSBestModelSelectionConfig bmdsBestModelSelection)
	{
		this.bmdsBestModelSelection = bmdsBestModelSelection;
	}

	public BMDSInputConfig getBmdsInputConfig()
	{
		return bmdsInputConfig;
	}

	public void setBmdsInputConfig(BMDSInputConfig bmdsInputConfig)
	{
		this.bmdsInputConfig = bmdsInputConfig;
	}

	public Integer getNumberOfThreads()
	{
		return numberOfThreads;
	}

	public void setNumberOfThreads(Integer numberOfThreads)
	{
		this.numberOfThreads = numberOfThreads;
	}

	// public Integer getKillTime()
	// {
	// return killTime;
	// }

	// public void setKillTime(Integer killTime)
	// {
	// this.killTime = killTime;
	// }

	// public String getTmpFolder()
	// {
	// return tmpFolder;
	// }

	// public void setTmpFolder(String f)
	// {
	// this.tmpFolder = f;
	// }

	// public Integer getMethod()
	// {
	// return method;
	// }

	// public void setMethod(Integer method)
	// {
	// this.method = method;
	// }

}
