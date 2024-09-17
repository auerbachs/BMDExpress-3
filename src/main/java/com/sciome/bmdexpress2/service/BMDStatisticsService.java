package com.sciome.bmdexpress2.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Precision;

import com.sciome.bmdexpress2.mvp.model.DoseGroup;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpress2.mvp.model.stat.ModelAveragingResult;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponse;
import com.sciome.bmdexpress2.mvp.model.stat.ModeledResponseValues;
import com.sciome.bmdexpress2.mvp.model.stat.ProbeStatResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.serviceInterface.IBMDStatisticsService;

public class BMDStatisticsService implements IBMDStatisticsService
{

	@Override
	public ModeledResponse generateResponsesBetweenDoseGroups(BMDResult bmdResults,
			List<ProbeStatResult> probeStatResults, int betweenDoses, Set<String> probeSet)
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
			{
				if (!String.valueOf(Precision.round(j, 5))
						.equals(String.valueOf(Precision.round(nextDg.getDose().doubleValue(), 5))))
					header.add(String.valueOf(Precision.round(j, 5)));
			}

		}
		header.add(String.valueOf(Precision.round(doseGroups.get(doseGroups.size() - 1).getDose(), 5)));

		for (ProbeStatResult psr : probeStatResults)
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
					if (!String.valueOf(Precision.round(j, 5))
							.equals(String.valueOf(Precision.round(nextDg.getDose().doubleValue(), 5))))
						mrv.getModeledResponses().add(bestie.getResponseAt(j));
				}

			}
			mrv.getModeledResponses()
					.add(bestie.getResponseAt(doseGroups.get(doseGroups.size() - 1).getDose()));

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
		double[] gradients1;
		double[] gradients0;
		double[] covariances;
		double mean0;
		double mean1;
		try
		{
			StatResult theResult = result;
			// must be careful with model averaging result. separate function
			if (result instanceof ModelAveragingResult)
				theResult = ((ModelAveragingResult) result).getModelWithHighestPP();

			gradients1 = calculateGradients(theResult, result.getBMD());
			gradients0 = calculateGradients(theResult, 0.0);

			// for now we can get the actuall model average result for the responses at these doses.
			mean0 = result.getResponseAt(0);
			mean1 = result.getResponseAt(result.getBMD());

			covariances = theResult.getCovariances();

		}
		catch (Exception e)
		{

		}

		return 0;
	}

	private double[] calculateGradients(StatResult result, Double atValue) throws Exception
	{

		double[] gradients = new double[result.getCurveParameters().length
				+ result.getOtherParameters().length];
		List<Double> params = new ArrayList<>();
		for (int i = 0; i < result.getCurveParameters().length; i++)
			params.add(result.getCurveParameters()[i]);

		Double mpres = Math.pow(1.0e-16, 0.33333);
		List<Double> h = new ArrayList<>();
		double[] hvector = new double[params.size()];
		for (int i = 0; i < params.size(); i++)
			hvector[i] = params.get(i);
		Double x;
		for (int i = 0; i < params.size(); i++)
		{
			x = params.get(i);
			if (Math.abs(x) > 1.0e-16)
			{
				h.add(mpres * Math.abs(x));
				Double temp = x + h.get(i);
				h.set(i, temp - x);
			}
			else
			{
				h.add(mpres);
			}
		}

		for (int i = 0; i < params.size(); i++)
		{
			x = params.get(i);
			hvector[i] = x + h.get(i);

			Double f1 = result.getResponseAt(atValue, hvector);

			hvector[i] = x - h.get(i);
			Double f2 = result.getResponseAt(atValue, hvector);

			gradients[i] = (f1 - f2) / (2.0 * h.get(i));
			hvector[i] = x;

		}

		return gradients;
	}

}
