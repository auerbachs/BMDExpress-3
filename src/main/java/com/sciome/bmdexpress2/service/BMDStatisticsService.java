package com.sciome.bmdexpress2.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Precision;

import com.sciome.bmdexpress2.mvp.model.DoseGroup;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponse;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponseValues;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IBMDStatisticsService;

public class BMDStatisticsService implements IBMDStatisticsService
{

	@Override
	public ModeledResponse generateResponsesBetweenDoseGroups(BMDResult bmdResults, int betweenDoses,
			Set<String> probeSet)
	{
		ModeledResponse result = new ModeledResponse();
		List<ModeledResponseValues> matrix = new ArrayList<>();
		result.setValues(matrix);
		DoseResponseExperiment dre = bmdResults.getDoseResponseExperiment();
		List<DoseGroup> doseGroups = dre.getDoseGroups();
		// for probes that have no best model or are not in the result set, they will need zeros output
		// for the sake of consistency for the entire dose response experiment.

		// add modelled responses to matrix for each responsive probe.
		// keep track of the responseive probes so we can
		// loop through and add entries for non-reponsonders fitPTextField
		Set<String> responsiveProbes = new HashSet<>();

		List<String> header = new ArrayList<>();

		header.add("Probe ID");

		Set<String> existingProbes = new HashSet<>();
		for (int i = 0; i < doseGroups.size() - 1; i++)
		{
			DoseGroup dg = doseGroups.get(i);
			DoseGroup nextDg = doseGroups.get(i + 1);

			double diff = nextDg.getDose() - dg.getDose();
			// calculate the steps between.
			double step = diff / betweenDoses;
			for (double j = dg.getDose(); j < nextDg.getDose(); j += step)
				header.add(String.valueOf(Precision.round(j, 5)));
		}

		for (ProbeStatResult psr : bmdResults.getProbeStatResults())
		{
			String probeid = psr.getProbeResponse().getProbe().getId();
			existingProbes.add(probeid);
			StatResult bestie = psr.getBestStatResult();
			if (bestie == null)
				continue;

			ModeledResponseValues mrv = new ModeledResponseValues();
			mrv.setProbeId(probeid);

			mrv.setModeledResponses(new ArrayList<>());
			matrix.add(mrv);
			for (int i = 0; i < doseGroups.size() - 1; i++)
			{
				DoseGroup dg = doseGroups.get(i);
				DoseGroup nextDg = doseGroups.get(i + 1);

				double diff = nextDg.getDose() - dg.getDose();
				// calculate the steps between.
				double step = diff / betweenDoses;
				for (double j = dg.getDose(); j < nextDg.getDose(); j += step)
				{
					mrv.getModeledResponses().add(bestie.getResponseAt(j));
				}

			}

			responsiveProbes.add(probeid);

		}

		// for nonresponsive probes, add 0's to the matrix
		for (ProbeResponse pr : dre.getProbeResponses())
		{
			if (responsiveProbes.contains(pr.getProbe().getId()))
				continue;

			ModeledResponseValues mrv = new ModeledResponseValues();
			mrv.setProbeId(pr.getProbe().getId());
			mrv.setModeledResponses(new ArrayList<>());
			matrix.add(mrv);

			for (int i = 0; i < doseGroups.size() - 1; i++)
			{
				DoseGroup dg = doseGroups.get(i);
				DoseGroup nextDg = doseGroups.get(i + 1);
				double diff = nextDg.getDose() - dg.getDose();
				// calculate the steps between.
				double step = diff / betweenDoses;
				for (double j = dg.getDose(); j < nextDg.getDose(); j += step)
					mrv.getModeledResponses().add(0.0);
			}
		}

		// for probes on chip but not in experiment add 0's to the matrix
		for (String probeid : probeSet)
		{
			if (existingProbes.contains(probeid))
				continue;

			ModeledResponseValues mrv = new ModeledResponseValues();
			mrv.setProbeId(probeid);
			mrv.setModeledResponses(new ArrayList<>());
			matrix.add(mrv);

			for (int i = 0; i < doseGroups.size() - 1; i++)
			{
				DoseGroup dg = doseGroups.get(i);
				DoseGroup nextDg = doseGroups.get(i + 1);
				double diff = nextDg.getDose() - dg.getDose();
				// calculate the steps between.
				double step = diff / betweenDoses;
				for (double j = dg.getDose(); j < nextDg.getDose(); j += step)
					mrv.getModeledResponses().add(0.0);
			}

		}

		result.setValues(matrix);
		result.setHeader(header);
		return result;
	}

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
