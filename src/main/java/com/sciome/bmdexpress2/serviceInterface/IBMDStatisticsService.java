package com.sciome.bmdexpress2.serviceInterface;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.stat.StatResult;

public interface IBMDStatisticsService
{

	public double[] calculateResiduals(StatResult result, List<Double> means, List<Double> doses);

	public double calculateRSquared(double[] residuals, List<Double> means);

	public double calculateZScore(StatResult result, List<Double> doses);

}
