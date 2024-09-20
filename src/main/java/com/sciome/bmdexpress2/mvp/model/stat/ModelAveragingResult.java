package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelAveragingResult extends StatResult
{

	private static final long serialVersionUID = -527776055122273597L;

	/**
	 * GeneId
	 */

	private List<StatResult> modelResults;
	private List<Double> posteriorProbabilities;

	public ModelAveragingResult()
	{
		super();
	}

	@Override
	public List<String> getColumnNames()
	{

		List<String> returnList = new ArrayList<String>(Arrays.asList("MA BMD", "MA BMDL", "MA BMDU",
				"MA adverseDirection", "MA BMD/BMDL", "MA Execution Complete"));
		for (StatResult sr : modelResults)
			returnList.add("MA " + sr.getModel() + " Posterior Probability");

		returnList.add("MA RSquared");
		List<String> residualHeader = getResidualHeader("MA Residual ");
		returnList.addAll(residualHeader);
		returnList.add("MA Is Step Function");
		returnList.add("MA Is Step Function Less Than Lowest Dose");
		returnList.add("MA Z-Score");
		returnList.add("MA ABS Z-Score");
		returnList.add("MA Modelled Response BMR Multiples");
		returnList.add("MA ABS Modelled Response BMR Multiples");
		returnList.add("MA Fold Change Top To Bottom (Model)");
		returnList.add("MA ABS Fold Change Top To Bottom (Model)");
		returnList.add("MA BMD/Low Dose");
		returnList.add("MA BMD/High Dose");

		return returnList;

	}

	@Override
	public List<Object> getRow()
	{
		List<Object> returnList = new ArrayList<Object>(
				Arrays.asList((this.getBMD()), (this.getBMDL()), (this.getBMDU()), this.getAdverseDirection(),

						(this.getBMDdiffBMDL()), this.getSuccess()));

		returnList.addAll(posteriorProbabilities);
		returnList.add(getrSquared());
		returnList.addAll(getResidualList());
		returnList.add(getIsStepFunction());
		returnList.add(isStepWithBMDLessLowest());
		returnList.add(this.getZscore());
		returnList.add(this.getAbsZScore());
		returnList.add(this.getBmrCountsToTop());
		returnList.add(this.getAbsBmrCountsToTop());
		returnList.add(this.getFoldChangeToTop());
		returnList.add(this.getAbsFoldChangeToTop());
		returnList.add(this.getBmdLowDoseRatio());
		returnList.add(this.getBmdHighDoseRatio());
		return returnList;

	}

	public List<StatResult> getModelResults()
	{
		return modelResults;
	}

	public void setModelResults(List<StatResult> modelResults)
	{
		this.modelResults = modelResults;
	}

	public List<Double> getPosteriorProbabilities()
	{
		return posteriorProbabilities;
	}

	public void setPosteriorProbabilities(List<Double> posteriorProbabilities)
	{
		this.posteriorProbabilities = posteriorProbabilities;
	}

	@Override
	public String toString()
	{
		return "Model Average";
	}

	@Override
	public List<String> getParametersNames()
	{
		return new ArrayList<String>();
	}

	@Override
	public double getResponseAt(double dose)
	{
		int i = 0;
		Double sum = 0.0;
		for (StatResult model : this.modelResults)
		{
			if (posteriorProbabilities.get(i) > 0.0)
				sum += model.getResponseAt(dose) * posteriorProbabilities.get(i);
			i++;
		}

		return sum.doubleValue();

	}

	// custom parameters here will equate to custom posterior proabilities
	@Override
	public double getResponseAt(double dose, double[] customParameters)
	{
		int i = 0;
		Double sum = 0.0;

		List<Double> customPP = new ArrayList<>();
		for (int j = 0; j < customParameters.length; j++)
			customPP.add(customParameters[j]);
		for (StatResult model : this.modelResults)
		{
			if (customPP.get(i) > 0.0)
				sum += model.getResponseAt(dose) * customPP.get(i);
			i++;
		}

		return sum.doubleValue();

	}

	@Override
	public String getFormulaText()
	{
		return "Model Average";
	}

	@Override
	public String getEquation()
	{
		String returnStr = "";
		int i = 0;
		for (StatResult sr : modelResults)
			returnStr += "(PP " + sr.getModel() + ": " + this.posteriorProbabilities.get(i++) + "),  ";

		return returnStr.replaceAll("\\,  $", "");
	}

	public StatResult getModelWithHighestPP()
	{
		int i = 0;
		Double highestPP = 0.0;
		StatResult highestModel = null;
		for (StatResult model : this.modelResults)
		{
			if (posteriorProbabilities.get(i) > highestPP)
			{
				highestPP = posteriorProbabilities.get(i);
				highestModel = model;
			}
			i++;
		}

		return highestModel;
	}

}
