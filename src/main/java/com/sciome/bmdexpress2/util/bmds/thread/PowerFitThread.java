/**
 * PowerFitThread.java
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
import com.sciome.bmdexpress2.mvp.model.stat.PowerResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSToxicRUtils;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.toxicR.ToxicRConstants;
import com.toxicR.model.NormalDeviance;

public class PowerFitThread extends Thread implements IFitThread
{
	private CountDownLatch cdLatch;

	private ModelInputParameters inputParameters;

	private float[] doses;

	private final int[] adversDirections = { 0, 1, -1 };
	private List<ProbeResponse> probeResponses;
	private List<StatResult> powerResults;
	private int numThread;
	private int instanceIndex;
	private IModelProgressUpdater progressUpdater;
	private IProbeIndexGetter probeIndexGetter;
	private boolean cancel = false;
	private String tmpFolder;
	private Map<String, NormalDeviance> deviance;
	LogTransformationEnum transform;

	public PowerFitThread(CountDownLatch cdLatch, List<ProbeResponse> probeResponses,
			List<StatResult> powerResults, int numThread, int instanceIndex, int killTime, String tmpFolder,
			IModelProgressUpdater progressUpdater, IProbeIndexGetter probeIndexGetter,
			Map<String, NormalDeviance> deviance, LogTransformationEnum transform)
	{
		this.transform = transform;
		this.deviance = deviance;
		this.progressUpdater = progressUpdater;
		this.cdLatch = cdLatch;
		this.probeResponses = probeResponses;
		this.powerResults = powerResults;
		this.numThread = numThread;
		this.instanceIndex = instanceIndex;
		this.probeIndexGetter = probeIndexGetter;
		this.tmpFolder = tmpFolder;

	}

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

			PowerResult powerResult = (PowerResult) powerResults.get(probeIndex);

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

				List<double[]> resultsList = BMDSToxicRUtils.calculateToxicR(ToxicRConstants.POWER,
						responsesD, dosesd, inputParameters.getBmrType(), inputParameters.getBmrLevel(),
						inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
						transform);
				double[] results = resultsList.get(0);
				double[] results1 = resultsList.get(1);
				double[] covariates = resultsList.get(2);

				if (results != null)
				{
					fillOutput(results, results1, covariates, powerResult);
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

	private void fillOutput(double[] results, double[] results1, double[] covariates, PowerResult powerResult)
	{
		powerResult.setBMD(results[0]);
		powerResult.setBMDL(results[1]);
		powerResult.setBMDU(results[2]);
		powerResult.setFitPValue(results[3]);
		powerResult.setFitLogLikelihood(results[4]);
		powerResult.setAIC(results[5]);
		powerResult.setOtherParameters(results1);
		powerResult.setCovariances(covariates);
		int direction = 1;

		if (results[7] < 0)
		{
			direction = -1;
		}
		powerResult.setCurveParameters(Arrays.copyOfRange(results, 6, results.length));
		powerResult.setAdverseDirection((short) direction);
		powerResult.setSuccess("true");
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}
}
