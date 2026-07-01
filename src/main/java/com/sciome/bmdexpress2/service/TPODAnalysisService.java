package com.sciome.bmdexpress2.service;

import java.util.Arrays;
import java.util.Random;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODAnalysisResult;
import com.sciome.bmdexpress2.mvp.model.tpod.TPODInputParameters;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeAntimodeResult;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeDetector;
import com.sciome.bmdexpress2.service.tpod.firstmode.ModeResult;
import com.sciome.bmdexpress2.serviceInterface.ITPODService;
import com.sciome.bmdexpress2.util.bmds.IBMDSToolProgress;

public class TPODAnalysisService implements ITPODService
{

	@Override
	public TPODAnalysisResult tpodAnalysis(CategoryAnalysisResults processableData,
			TPODInputParameters inputParameters, IBMDSToolProgress progressUpdater)
	{

		Random rnd = new Random(42);
		double[] x = new double[2000];
		for (int i = 0; i < x.length; i++)
		{
			x[i] = (i % 2 == 0) ? rnd.nextGaussian() * 1.0 - 4 : rnd.nextGaussian() * 1.0 + 4;
		}

		ModeResult m2 = ModeDetector.modes2(x, 0.1);
		System.out.println("Modes2:        " + m2);

		ModeAntimodeResult ma = ModeDetector.modeAntimode(x, 0.1);
		System.out.println("mode.antimode: " + ma);

		// TODO Auto-generated method stub
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
