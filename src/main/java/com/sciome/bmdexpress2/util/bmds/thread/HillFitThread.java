/**
 * HillFitThread.java
 *
 *
 */

package com.sciome.bmdexpress2.util.bmds.thread;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.sciome.bmdexpress2.mvp.model.LogTransformationEnum;
import com.sciome.bmdexpress2.mvp.model.probe.ProbeResponse;
import com.sciome.bmdexpress2.mvp.model.stat.HillResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSToxicRUtils;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.toxicR.ToxicRConstants;
import com.toxicR.model.NormalDeviance;

public class HillFitThread extends Thread implements IFitThread
{
	private CountDownLatch cdLatch;

	private ModelInputParameters inputParameters;
	private boolean flagHill = false;

	private double flagDose;
	private float[] doses;

	private final int[] adversDirections = { 0, 1, -1 };
	private List<ProbeResponse> probeResponses;
	private List<StatResult> hillResults;

	private int numThreads;
	private int instanceIndex;

	private boolean cancel = false;

	private IModelProgressUpdater progressUpdater;
	private IProbeIndexGetter probeIndexGetter;

	private String tmpFolder;
	private Map<String, NormalDeviance> deviance;
	LogTransformationEnum transform;

	public HillFitThread(CountDownLatch cdLatch, List<ProbeResponse> probeResponses,
			List<StatResult> hillResults, int numThreads, int instanceIndex, int killTime, String tmpFolder,
			IModelProgressUpdater progressUpdater, IProbeIndexGetter probeIndexGetter,
			Map<String, NormalDeviance> deviance, LogTransformationEnum transform)
	{
		this.transform = transform;
		this.deviance = deviance;
		this.progressUpdater = progressUpdater;
		this.cdLatch = cdLatch;
		this.probeResponses = probeResponses;
		this.hillResults = hillResults;
		this.numThreads = numThreads;
		this.instanceIndex = instanceIndex;
		this.probeIndexGetter = probeIndexGetter;
		this.tmpFolder = tmpFolder;

	}

	/*
	 * public void setJNIHillFit(HillFit hillFit) { this.hillFit = hillFit; }
	 */

	public void setDoses(float[] doses)
	{
		this.doses = doses;
	}

	public void setObjects(ModelInputParameters inputParameters)
	{
		this.inputParameters = inputParameters;
	}

	@Override
	public void run()
	{

		toxicRFit();
		try
		{
			cdLatch.countDown();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void toxicRFit()
	{
		double[] dosesd = new double[doses.length];
		int di = 0;
		for (float d : doses)
			dosesd[di++] = d;
		Random rand = new Random(System.nanoTime());
		int randInt = Math.abs(rand.nextInt());

		Integer probeIndex = probeIndexGetter.getNextProbeIndex();
		while (probeIndex != null)
		{

			HillResult hillResult = (HillResult) hillResults.get(probeIndex);

			if (cancel)
			{
				break;
			}

			try
			{
				String id = probeResponses.get(probeIndex).getProbe().getId().replaceAll("\\s", "_");
				NormalDeviance dev = deviance.get(probeResponses.get(probeIndex).getProbe().getId());
				id = String.valueOf(randInt) + "_" + BMDExpressProperties.getInstance()
						.getNextTempFile(this.tmpFolder, String.valueOf(Math.abs(id.hashCode())), ".(d)");
				float[] responses = probeResponses.get(probeIndex).getResponseArray();
				double[] responsesD = new double[responses.length];
				int ri = 0;
				for (float r : responses)
					responsesD[ri++] = r;

				List<double[]> resultsList = BMDSToxicRUtils.calculateToxicR(ToxicRConstants.HILL, responsesD,
						dosesd, inputParameters.getBmrType(), inputParameters.getBmrLevel(),
						inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
						transform);
				double[] results = resultsList.get(0);
				double[] results1 = resultsList.get(1);

				double tmpr = results[8];
				results[8] = results[9];
				results[9] = tmpr;

				if (results != null)
				{
					fillOutput(results, results1, hillResult);
					if (flagHill)
					{
						if (results[9] < flagDose)
						{
							hillResult.setkFlag((short) 1);
						}
						else
						{
							hillResult.setkFlag((short) 0);
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			this.progressUpdater.incrementModelsComputed();
			probeIndex = probeIndexGetter.getNextProbeIndex();
		}

	}

	/*
	 * given the results double array, we need to fill up the hillResult Object with the results.
	 */
	private void fillOutput(double[] results, double[] covariates, HillResult hillResult)
	{
		hillResult.setBMD(results[0]);
		hillResult.setBMDL(results[1]);
		hillResult.setBMDU(results[2]);
		hillResult.setFitPValue(results[3]);
		hillResult.setFitLogLikelihood(results[4]);
		hillResult.setAIC(results[5]);
		// hillResult.setVariances(covariates);
		int direction = 1;

		if (results[7] < 0)
		{
			direction = -1;
		}
		hillResult.setCurveParameters(Arrays.copyOfRange(results, 6, results.length));
		hillResult.setAdverseDirection((short) direction);
		hillResult.setSuccess("true");
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}

	public void setFlag(boolean flagHill, double flagDose)
	{
		this.flagHill = flagHill;
		this.flagDose = flagDose;

	}
}
