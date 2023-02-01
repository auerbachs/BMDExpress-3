package com.sciome.bmdexpress2.service;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IBMDStatisticsService;

public class BMDStatisticsService implements IBMDStatisticsService
{

	@Override
	public double[] calculateResiduals(StatResult result, List<Double> means, List<Double> doses)
	{
		// calculate distance from predicted value to the mean
		return null;
	}

	@Override
	public double calculateRSquared(double[] residuals, List<Double> means)
	{
		// 1 - sum (residual)^2/sum(mean-totalmean)^2
		return 0;
	}

	@Override
	public double calculateZScore(StatResult result, List<Double> doses)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
