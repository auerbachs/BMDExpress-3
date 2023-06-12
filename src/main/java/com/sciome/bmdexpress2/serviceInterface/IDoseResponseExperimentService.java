package com.sciome.bmdexpress2.serviceInterface;

import java.util.List;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;

public interface IDoseResponseExperimentService
{

	public boolean isStepFunction(List<Float> responses, DoseResponseExperiment doseResponseExperiment,
			double threshold);

}
