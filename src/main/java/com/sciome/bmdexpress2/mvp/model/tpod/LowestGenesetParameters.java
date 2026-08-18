package com.sciome.bmdexpress2.mvp.model.tpod;

public class LowestGenesetParameters extends TPODMethodParameter
{

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.FIRST_GENESET;
	}

	@Override
	public String getParameterString()
	{
		return "first gene set";
	}

}
