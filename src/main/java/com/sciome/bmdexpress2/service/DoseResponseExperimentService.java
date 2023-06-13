package com.sciome.bmdexpress2.service;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.DoseGroup;
import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpress2.serviceInterface.IDoseResponseExperimentService;

public class DoseResponseExperimentService implements IDoseResponseExperimentService
{

	@Override
	public boolean isStepFunction(List<Float> responses, DoseResponseExperiment doseResponseExp,
			double threshold)
	{
		List<DoseGroup> dosegroups = doseResponseExp.getDoseGroups(responses);
		double control = transform(dosegroups.get(0).getResponseMean(), doseResponseExp);
		double last = transform(dosegroups.get(dosegroups.size() - 1).getResponseMean(), doseResponseExp);

		double totalchange = Math.abs(last - control);

		for (int i = 1; i < dosegroups.size(); i++)
		{
			double dg1 = transform(dosegroups.get(i - 1).getResponseMean(), doseResponseExp);
			double dg2 = transform(dosegroups.get(i).getResponseMean(), doseResponseExp);

			double change = Math.abs(dg2 - dg1);
			if (change / totalchange >= threshold)
				return true;
		}

		return false;
	}

	private double transform(Double responseMean, DoseResponseExperiment doseResponseExp)
	{

		// if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.BASE2))
		// return Math.pow(2.0, responseMean);
		// else if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.NONE))
		return responseMean;
		// else if (doseResponseExp.getLogTransformation().equals(LogTransformationEnum.NATURAL))
		// return Math.pow(Math.E, responseMean);

		// else
		// return Math.pow(10.0, responseMean);

	}

}
