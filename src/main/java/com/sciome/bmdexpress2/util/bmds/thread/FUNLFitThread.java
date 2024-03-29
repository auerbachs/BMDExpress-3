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
import com.sciome.bmdexpress2.mvp.model.stat.FunlResult;
import com.sciome.bmdexpress2.mvp.model.stat.StatResult;
import com.sciome.bmdexpress2.shared.BMDExpressConstants;
import com.sciome.bmdexpress2.shared.BMDExpressProperties;
import com.sciome.bmdexpress2.util.bmds.BMDSToxicRUtils;
import com.sciome.bmdexpress2.util.bmds.ModelInputParameters;
import com.toxicR.ToxicRConstants;
import com.toxicR.model.NormalDeviance;

public class FUNLFitThread extends Thread implements IFitThread
{
	private CountDownLatch cdLatch;

	private ModelInputParameters inputParameters;

	private float[] doses;

	private List<ProbeResponse> probeResponses;
	private List<StatResult> funlResults;
	private IModelProgressUpdater progressUpdater;
	private IProbeIndexGetter probeIndexGetter;
	private boolean cancel = false;
	private String tmpFolder;

	private Map<String, NormalDeviance> deviance;
	LogTransformationEnum transform;

	public FUNLFitThread(CountDownLatch cdLatch, List<ProbeResponse> probeResponses,
			List<StatResult> funlResults, int numThread, int instanceIndex, int killTime, String tmpFolder,
			IModelProgressUpdater progressUpdater, IProbeIndexGetter probeIndexGetter,
			Map<String, NormalDeviance> deviance, LogTransformationEnum transform)
	{
		this.transform = transform;
		this.deviance = deviance;
		this.progressUpdater = progressUpdater;
		this.cdLatch = cdLatch;
		this.probeResponses = probeResponses;
		this.funlResults = funlResults;

		this.probeIndexGetter = probeIndexGetter;
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

			FunlResult funlResult = (FunlResult) funlResults.get(probeIndex);

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

				List<double[]> resultsList = BMDSToxicRUtils.calculateToxicR(ToxicRConstants.FUNL, responsesD,
						dosesd, inputParameters.getBmrType(), inputParameters.getBmrLevel(),
						inputParameters.getConstantVariance() != 1, dev, inputParameters.isFast(), false,
						transform);
				double[] results = resultsList.get(0);
				double[] results1 = resultsList.get(1);

				if (results != null)
				{
					fillOutput(results, results1, funlResult);
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

	private void fillOutput(double[] results, double[] covariates, FunlResult funlResult)
	{
		funlResult.setBMD(results[0]);
		funlResult.setBMDL(results[1]);
		funlResult.setBMDU(results[2]);
		funlResult.setFitPValue(results[3]);
		funlResult.setFitLogLikelihood(results[4]);
		funlResult.setAIC(results[5]);
		// funlResult.setVariances(covariates);
		int direction = 1;

		if (results[7] < 0)
		{
			direction = -1;
		}
		funlResult.setCurveParameters(Arrays.copyOfRange(results, 6, results.length));
		funlResult.setAdverseDirection((short) direction);
		funlResult.setSuccess("true");
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}
}
