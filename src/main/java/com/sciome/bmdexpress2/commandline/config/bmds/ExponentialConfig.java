package com.sciome.bmdexpress2.commandline.config.bmds;

public class ExponentialConfig extends BMDSModelConfig
{

	// 2, 3, 4, or 5
	private Integer expModel;

	private Boolean restrictPower = false;

	public Integer getExpModel()
	{
		return expModel;
	}

	public void setExpModel(Integer expModel)
	{
		this.expModel = expModel;
	}

	public Boolean isRestrictPower()
	{
		return restrictPower;
	}

	public void setRestrictPower(Boolean restrictPower)
	{
		this.restrictPower = restrictPower;
	}

}
