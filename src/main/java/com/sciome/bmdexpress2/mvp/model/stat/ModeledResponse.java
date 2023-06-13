package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.List;

public class ModeledResponse
{
	List<String> header;
	List<ModeledResponseValues> values;

	public List<String> getHeader()
	{
		return header;
	}

	public void setHeader(List<String> header)
	{
		this.header = header;
	}

	public List<ModeledResponseValues> getValues()
	{
		return values;
	}

	public void setValues(List<ModeledResponseValues> values)
	{
		this.values = values;
	}

}
