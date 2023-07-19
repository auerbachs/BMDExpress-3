package com.sciome.bmdexpress2.util.curvep;

public class GCurvePInputParameters
{

	private float BMR = 1.349f;
	private int bootStraps = 1000;
	private float pValueCutoff = .05f;
	private Double controlDoseAdjustment = null;
	private double stepFunctionThreshold = 0.75;

	public double getStepFunctionThreshold()
	{
		return stepFunctionThreshold;
	}

	public void setStepFunctionThreshold(double stepFunctionThreshold)
	{
		this.stepFunctionThreshold = stepFunctionThreshold;
	}

	public float getBMR()
	{
		return BMR;
	}

	public void setBMR(float bMR)
	{
		BMR = bMR;
	}

	public int getBootStraps()
	{
		return bootStraps;
	}

	public void setBootStraps(int bootStraps)
	{
		this.bootStraps = bootStraps;
	}

	public float getpValueCutoff()
	{
		return pValueCutoff;
	}

	public void setpValueCutoff(float pValueCutoff)
	{
		this.pValueCutoff = pValueCutoff;
	}

	public Double getControlDoseAdjustment()
	{
		return controlDoseAdjustment;
	}

	public void setControlDoseAdjustment(Double controlDoseAdjustment)
	{
		this.controlDoseAdjustment = controlDoseAdjustment;
	}

}
