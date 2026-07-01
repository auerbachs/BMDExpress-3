package com.sciome.bmdexpress2.service;

import java.util.Arrays;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.BMDEndpointType;
import com.sciome.bmdexpress2.mvp.model.tpod.FirstModeParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.LCRDParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.MaxCurveParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthPercentParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.NthRankParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODMethodParameter;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeAntimodeResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeDetector;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public class TPODAnalysisService implements ITPODService
{

	@Override
	public TPODAnalysisResults tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater)
	{

		// TODO: first we need to do some filtration.

		// loop through each bmdendpoint type
		for (BMDEndpointType bmdEndpointType : inputParameters.getBmdEndpointTypes())
		{

			// TODO: for the bmdendpoint type, grab that from the list of category results.

			// now loop through the tpod methods that will be used.
			for (TPODMethodParameter method : inputParameters.getMethodParameters())
			{

				// given a method, calculate the tPOD
				if (method instanceof LCRDParameters)
				{
					LCRDParameters lp = (LCRDParameters) method;
				}
				else if (method instanceof MaxCurveParameters)
				{
					MaxCurveParameters mc = (MaxCurveParameters) method;
				}
				else if (method instanceof NthRankParameters)
				{
					NthRankParameters nrp = (NthRankParameters) method;
				}
				else if (method instanceof NthPercentParameters)
				{
					NthPercentParameters npp = (NthPercentParameters) method;

				}
				else if (method instanceof FirstModeParameters)
				{
					FirstModeParameters fm = (FirstModeParameters) method;
				}

			}

		}

		return null;
	}

	@Override
	public boolean cancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public static double firstMode(double[] x)
	{
		return firstMode(x, 0.055);
	}

	public static double firstMode(double[] xRaw, double minSize)
	{
		double[] x = xRaw.clone();
		Arrays.sort(x); // order(x, decreasing = FALSE)

		double[] logX = new double[x.length];
		for (int i = 0; i < x.length; i++)
		{
			logX[i] = Math.log10(x[i]);
		}

		ModeAntimodeResult result = ModeDetector.modeAntimode(logX, minSize);

		if (result.modes.length == 0)
		{
			throw new IllegalStateException("mode.antimode returned no modes");
		}

		double firstModeLog = result.modes[0];
		return Math.pow(10, firstModeLog);
	}

}
