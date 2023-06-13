package com.sciome.bmdexpress2.serviceInterface;

import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;

import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponse;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;

public interface IBMDStatisticsService
{

	public double[] calculateResiduals(StatResult result, List<Double> means, List<Double> doses)
			throws Exception;

	public double calculateRSquared(double[] residuals, List<Double> means) throws Exception;

	public double calculateZScore(StatResult result, List<Double> doses) throws Exception;

	public double calculateZScore(StatResult result, List<Double> doses, RealMatrix covarianceMatrix);

	public ModeledResponse generateResponsesBetweenDoseGroups(BMDResult bmdResults, int betweenDose);

}
