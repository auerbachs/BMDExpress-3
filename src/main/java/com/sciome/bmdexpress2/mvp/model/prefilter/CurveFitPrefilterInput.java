package com.sciome.bmdexpress2.mvp.model.prefilter;

import com.sciome.bmdexpress2.util.bmds.shared.BMRFactor;

public class CurveFitPrefilterInput extends PrefilterInput
{

	private Boolean isHill = true;
	private Boolean isPower = true;
	private Boolean isExp3 = true;
	private Boolean isExp5 = true;
	private Boolean isLinear = true;
	private Boolean isPoly2 = true;

	private Boolean constantVariance;
	private BMRFactor BMRFactor;
	private BMRFactor poly2BMRFactor;

	public CurveFitPrefilterInput()
	{
		super();
	}

	public BMRFactor getPoly2BMRFactor()
	{
		return poly2BMRFactor;
	}

	public void setPoly2BMRFactor(BMRFactor poly2bmrFactor)
	{
		poly2BMRFactor = poly2bmrFactor;
	}

	public Boolean getIsHill()
	{
		return isHill;
	}

	public void setIsHill(Boolean isHill)
	{
		this.isHill = isHill;
	}

	public Boolean getIsPower()
	{
		return isPower;
	}

	public void setIsPower(Boolean isPower)
	{
		this.isPower = isPower;
	}

	public Boolean getIsExp3()
	{
		return isExp3;
	}

	public void setIsExp3(Boolean isExp3)
	{
		this.isExp3 = isExp3;
	}

	public Boolean getIsExp5()
	{
		return isExp5;
	}

	public void setIsExp5(Boolean isExp5)
	{
		this.isExp5 = isExp5;
	}

	public Boolean getIsLinear()
	{
		return isLinear;
	}

	public void setIsLinear(Boolean isLinear)
	{
		this.isLinear = isLinear;
	}

	public Boolean getIsPoly2()
	{
		return isPoly2;
	}

	public void setIsPoly2(Boolean isPoly2)
	{
		this.isPoly2 = isPoly2;
	}

	public Boolean isConstantVariance()
	{
		return constantVariance;
	}

	public void setConstantVariance(Boolean constantVariance)
	{
		this.constantVariance = constantVariance;
	}

	public BMRFactor getBMRFactor()
	{
		return BMRFactor;
	}

	public void setBMRFactor(BMRFactor bMRFactor)
	{
		BMRFactor = bMRFactor;
	}

}
