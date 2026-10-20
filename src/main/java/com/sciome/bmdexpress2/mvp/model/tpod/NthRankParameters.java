package com.sciome.bmdexpress2.mvp.model.tpod;

public class NthRankParameters extends TPODMethodParameter
{
	private int rank;

	public int getRank()
	{
		return rank;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}

	@Override
	public TPODMethod getMethod()
	{
		return TPODMethod.NTH_RANK;
	}

	@Override
	public String getParameterString()
	{
		return "rank=" + rank;
	}

}
