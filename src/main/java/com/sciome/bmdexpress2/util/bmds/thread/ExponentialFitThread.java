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
import com.sciome.bmdexpress2.mvp.model.stat.ExponentialResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.shared.BMDExpressConstants;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSToxicRUtils;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.toxicR.ToxicRConstants;
import com.toxicR.model.NormalDeviance;

public class ExponentialFitThread extends Thread implements IFitThread
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
	private int expOption = 0;
	private String tmpFolder;
	private Map<String, NormalDeviance> deviance;
	LogTransformationEnum transform;

	public ExponentialFitThread(CountDownLatch cdLatch, List<ProbeResponse> probeResponses,
			List<StatResult> powerResults, int numThread, int instanceIndex, int option, int killTime,
			String tmpFolder, IModelProgressUpdater progressUpdater, IProbeIndexGetter probeIndexGetter,
			Map<String, NormalDeviance> deviance, LogTransformationEnum transform)
	{
		this.deviance = deviance;
		this.transform = transform;
		this.progressUpdater = progressUpdater;
		this.cdLatch = cdLatch;
		this.probeResponses = probeResponses;
		this.powerResults = powerResults;
		this.numThread = numThread;
		this.instanceIndex = instanceIndex;
		this.probeIndexGetter = probeIndexGetter;
		this.expOption = option;
		this.tmpFolder = tmpFolder;
		if (tmpFolder == null || tmpFolder.equals(""))
			this.tmpFolder = BMDExpressConstants.getInstance().TEMP_FOLDER;

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

			ExponentialResult expResult = (ExponentialResult) powerResults.get(probeIndex);

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

				int expModel = ToxicRConstants.EXP3;

				if (expOption == 5)
					expModel = ToxicRConstants.EXP5;

				List<double[]> resultsList = BMDSToxicRUtils.calculateToxicR(expModel, responsesD, dosesd,
						inputParameters.getBmrType(), inputParameters.getBmrLevel(),
						inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
						transform);
				double[] results = resultsList.get(0);
				double[] results1 = resultsList.get(1);

				// if (expModel == ToxicRConstants.EXP3) // move param d to param c
				// results[9] = results[10];
				if (expModel == ToxicRConstants.EXP5) // anti log c
					results[9] = Math.pow(Math.E, results[9]);
				if (results != null)
				{
					fillOutput(results, results1, expResult);
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

	private void fillOutput(double[] results, double[] covariates, ExponentialResult expResult)
	{
		expResult.setBMD(results[0]);
		expResult.setBMDL(results[1]);
		expResult.setBMDU(results[2]);
		expResult.setFitPValue(results[3]);
		expResult.setFitLogLikelihood(results[4]);
		expResult.setAIC(results[5]);
		expResult.setOption(expOption);
		// expResult.setVariances(covariates);
		int direction = 1;

		if (results[6] < 0)
		{
			direction = -1;
		}
		expResult.setCurveParameters(Arrays.copyOfRange(results, 6, results.length));
		expResult.setAdverseDirection((short) direction);
		expResult.setSuccess("true");
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}
}
