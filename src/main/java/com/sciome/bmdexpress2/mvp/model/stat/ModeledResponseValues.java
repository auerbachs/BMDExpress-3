package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.List;

public class ModeledResponseValues
{
	String probeId;
	List<Double> modeledResponses;

	public String getProbeId()
	{
		return probeId;
	}

	public void setProbeId(String probeId)
	{
		this.probeId = probeId;
	}

	public List<Double> getModeledResponses()
	{
		return modeledResponses;
	}

	public void setModeledResponses(List<Double> modeledResponses)
	{
		this.modeledResponses = modeledResponses;
	}

}
