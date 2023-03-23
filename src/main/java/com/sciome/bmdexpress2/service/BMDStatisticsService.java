package com.sciome.bmdexpress2.service;

import java.util.List;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealMatrix;

import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IBMDStatisticsService;

public class BMDStatisticsService implements IBMDStatisticsService
{

	@Override
	public double[] calculateResiduals(StatResult result, List<Double> means, List<Double> doses)
			throws Exception
	{
		// calculate distance from predicted value to the mean
		double[] residuals = new double[doses.size()];
		int i = 0;
		for (Double dose : doses)
		{
			residuals[i] = means.get(i).doubleValue() - result.getResponseAt(dose);
			i++;
		}
		return residuals;
	}

	@Override
	public double calculateRSquared(double[] residuals, List<Double> yvalues) throws Exception
	{
		// 1 - sum (residual)^2/sum(mean-totalmean)^2
		double sum = 0;
		for (Double y : yvalues)
			sum += y.doubleValue();

		double avg = sum / yvalues.size();

		double ssr = 0; // sum squared regression
		for (double val : residuals)
			ssr += val * val;

		double sst = 0; // total sum of squares
		for (Double y : yvalues)
		{
			double diff = y - avg;
			sst += diff * diff;
		}

		return 1 - (ssr / sst);
	}

	@Override
	public double calculateZScore(StatResult result, List<Double> doses) throws Exception
	{
		/*
		 * double[] variances = result.getVariances();
		 * if (variances == null)
		 * return 0;
		 * 
		 * double low = result.getResponseAt(doses.get(0));
		 * double high = result.getResponseAt(doses.get(doses.size() - 1));
		 * if (variances.length == 1)
		 * return (low - high) / Math.exp(0.5 * variances[0]);
		 * 
		 * if (variances.length == 2)
		 * return (low - high) / Math.sqrt(Math.exp(variances[1]) * Math.pow(low, variances[0]));
		 */

		return 0;
	}

	@Override
	public double calculateZScore(StatResult result, List<Double> doses, RealMatrix covarianceMatrix)
	{
		int params = 1;
		int order = 3;
		double xRealValue = 2.5;

		UnivariateDifferentiableFunction function = new UnivariateDifferentiableFunction() {

			@Override
			public double value(double x)
			{
				return result.getResponseAt(x);
			}

			@Override
			public DerivativeStructure value(DerivativeStructure t) throws DimensionMismatchException
			{
				// TODO Auto-generated method stub
				return null;
			}

		};

		FiniteDifferencesDifferentiator diff = new FiniteDifferencesDifferentiator(5, .25);
		// diff.differentiate(function).
		// .
		return 0;
	}

}
