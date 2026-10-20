package com.sciome.bmdexpress2.mvp.model.tpod;

import java.util.List;

public class TPODInputParameters
{

	private boolean isGeneSet = true;

	private List<TPODMethodParameter> methodParameters;

	private List<TPODInputFilter> inputFilters;

	private List<BMDEndpointType> bmdEndpointTypes;

	public List<TPODMethodParameter> getMethodParameters()
	{
		return methodParameters;
	}

	public void setMethodParameters(List<TPODMethodParameter> methodParameters)
	{
		this.methodParameters = methodParameters;
	}

	public List<TPODInputFilter> getInputFilters()
	{
		return inputFilters;
	}

	public void setInputFilters(List<TPODInputFilter> inputFilters)
	{
		this.inputFilters = inputFilters;
	}

	public List<BMDEndpointType> getBmdEndpointTypes()
	{
		return bmdEndpointTypes;
	}

	public void setBmdEndpointTypes(List<BMDEndpointType> bmdEndpointTypes)
	{
		this.bmdEndpointTypes = bmdEndpointTypes;
	}

	public boolean isGeneSet()
	{
		return isGeneSet;
	}

	public void setGeneSet(boolean isGeneSet)
	{
		this.isGeneSet = isGeneSet;
	}

}
