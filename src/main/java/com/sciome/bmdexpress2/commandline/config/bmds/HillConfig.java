package com.sciome.bmdexpress2.commandline.config.bmds;

public class HillConfig extends BMDSModelConfig
{

	private Boolean restrictPower = false;

	public Boolean isRestrictPower()
	{
		return restrictPower;
	}

	public void setRestrictPower(Boolean restrictPower)
	{
		this.restrictPower = restrictPower;
	}

}
