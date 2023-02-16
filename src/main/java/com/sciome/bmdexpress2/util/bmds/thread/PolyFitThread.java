/**
 * PolyFitThread.java
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
import com.sciome.bmdexpress2.mvp.model.stat.PolyResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSToxicRUtils;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.toxicR.ToxicRConstants;
import com.toxicR.model.NormalDeviance;

public class PolyFitThread extends Thread implements IFitThread
{
	private CountDownLatch cdLatch;

	private int degree;
	private ModelInputParameters inputParameters;

	private float[] doses;

	private final int[] adversDirections = { 0, 1, -1 };

	List<ProbeResponse> probeResponses;
	List<StatResult> polyResults;
	int numThreads;
	int instanceIndex;
	private IModelProgressUpdater progressUpdater;
	private IProbeIndexGetter probeIndexGetter;

	private boolean cancel = false;

	private final double DEFAULTDOUBLE = -9999;

	private String tmpFolder;
	private Map<String, NormalDeviance> deviance;
	LogTransformationEnum transform;

	public PolyFitThread(CountDownLatch cDownLatch, int degree, List<ProbeResponse> probeResponses,
			List<StatResult> polyResults, int numThreads, int instanceIndex, int killTime, String tmpFolder,
			IModelProgressUpdater progressUpdater, IProbeIndexGetter probeIndexGetter,
			Map<String, NormalDeviance> deviance, LogTransformationEnum transform)
	{
		this.transform = transform;
		this.deviance = deviance;
		this.progressUpdater = progressUpdater;
		this.cdLatch = cDownLatch;
		this.degree = degree;
		this.probeResponses = probeResponses;
		this.instanceIndex = instanceIndex;
		this.numThreads = numThreads;
		this.polyResults = polyResults;
		this.probeIndexGetter = probeIndexGetter;
		this.tmpFolder = tmpFolder;

	}

	public void setDoses(float[] doses)
	{
		this.doses = doses;
	}

	public void setObjects(int degree, ModelInputParameters inputParameters)
	{
		this.degree = degree;
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

			PolyResult polyResult = (PolyResult) polyResults.get(probeIndex);

			if (cancel)
			{
				break;
			}

			try
			{
				NormalDeviance dev = deviance.get(probeResponses.get(probeIndex).getProbe().getId());
				String id = probeResponses.get(probeIndex).getProbe().getId().replaceAll("\\s", "_");
				id = String.valueOf(randInt) + "_" + BMDExpressProperties.getInstance()
						.getNextTempFile(this.tmpFolder, String.valueOf(Math.abs(id.hashCode())), ".(d)");
				float[] responses = probeResponses.get(probeIndex).getResponseArray();
				double[] responsesD = new double[responses.length];
				int ri = 0;
				for (float r : responses)
					responsesD[ri++] = r;

				int polyModelConstant = ToxicRConstants.LINEAR;
				if (inputParameters.getPolyDegree() == 2)
					polyModelConstant = ToxicRConstants.POLY2;
				else if (inputParameters.getPolyDegree() == 3)
					polyModelConstant = ToxicRConstants.POLY3;
				else if (inputParameters.getPolyDegree() == 4)
					polyModelConstant = ToxicRConstants.POLY4;

				List<double[]> resultsList = null;

				double[] results = null;
				double[] covariates = null;
				if (inputParameters.getPolyDegree() == 1)
				{
					resultsList = BMDSToxicRUtils.calculateToxicR(ToxicRConstants.POWER, responsesD, dosesd,
							inputParameters.getBmrType(), inputParameters.getBmrLevel(),
							inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
							transform);
					results = resultsList.get(0);
					covariates = resultsList.get(1);
				}
				else
				{
					boolean mono = inputParameters.isPolyMonotonic();
					if (inputParameters.getPolyDegree() > 2)
						mono = true;
					// run it in both directions.

					resultsList = BMDSToxicRUtils.calculateToxicR(ToxicRConstants.POWER, responsesD, dosesd,
							inputParameters.getBmrType(), inputParameters.getBmrLevel(),
							inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
							transform);

					double[] results1 = resultsList.get(0);
					double[] covariates1 = resultsList.get(1);

					resultsList = BMDSToxicRUtils.calculateToxicR(polyModelConstant, responsesD, dosesd,
							inputParameters.getBmrType(), inputParameters.getBmrLevel(),
							inputParameters.getConstantVariance() != 1, false, dev, inputParameters.isFast(),
							mono, transform);
					double[] results2 = resultsList.get(0);
					double[] covariates2 = resultsList.get(1);

					if ((results1[0] > results2[0] && results2[0] != DEFAULTDOUBLE)
							|| results1[0] == DEFAULTDOUBLE)
					{
						results = results2;
						covariates = covariates2;
					}
					else
					{
						results = results1;
						covariates = covariates1;
					}
				}

				if (results != null)
				{
					fillOutput(results, covariates, polyResult);
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

	private void fillOutput(double[] results, double[] covariates, PolyResult polyResult)
	{
		polyResult.setBMD(results[0]);
		polyResult.setBMDL(results[1]);
		polyResult.setBMDU(results[2]);
		polyResult.setFitPValue(results[3]);
		polyResult.setFitLogLikelihood(results[4]);
		polyResult.setAIC(results[5]);
		polyResult.setCovariates(covariates);
		int direction = 1;

		if (results[7] < 0)
		{
			direction = -1;
		}
		polyResult.setCurveParameters(Arrays.copyOfRange(results, 6, results.length));
		polyResult.setAdverseDirection((short) direction);
		polyResult.setSuccess("true");
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}
}
