package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HillResult extends StatResult
{

	private static final long serialVersionUID = -527776055122273597L;
	/**
	 * GeneId
	 */

	private short kFlag;

	public HillResult()
	{
		super();
	}

	public short getkFlag()
	{
		return kFlag;
	}

	public void setkFlag(short kFlag)
	{
		this.kFlag = kFlag;
	}

	@Override
	public List<String> getColumnNames()
	{

		List<String> residualHeader = getResidualHeader("Hill Residual ");

		List<String> header = new ArrayList<String>(Arrays.asList("Hill BMD", "Hill BMDL", "Hill BMDU",
				"Hill fitPValue", "Hill fitLogLikelihood", "Hill AIC", "Hill adverseDirection",
				"Hill BMD/BMDL", "Flagged Hill", "Hill Parameter Intercept", "Hill Parameter v",
				"Hill Parameter n", "Hill Parameter k", "Hill Execution Complete", "Hill RSquared"));
		header.addAll(residualHeader);
		header.add("Hill Is Step Function Less Than Lowest Dose");
		header.add("Hill Z-Score");
		return header;

	}

	@Override
	public List<Object> getRow()
	{
		Double param1 = null;
		Double param2 = null;
		Double param3 = null;
		Double param4 = null;
		if (curveParameters != null)
		{
			param1 = curveParameters[0];
			param2 = curveParameters[1];
			param3 = curveParameters[2];
			param4 = curveParameters[3];
		}

		List<Object> returnList = new ArrayList<Object>(Arrays.asList((this.getBMD()), (this.getBMDL()),
				(this.getBMDU()), (this.getFitPValue()), (this.getFitLogLikelihood()), (this.getAIC()),
				(this.getAdverseDirection()), (this.getBMDdiffBMDL()), (this.getkFlag()), param1, param2,
				param3, param4, this.getSuccess()));

		returnList.add(getrSquared());
		returnList.addAll(getResidualList());
		returnList.add(isStepWithBMDLessLowest());
		returnList.add(this.getZscore());
		return returnList;

	}

	@Override
	public String toString()
	{
		return "Hill";
	}

	@Override
	public List<String> getParametersNames()
	{
		return new ArrayList<String>(Arrays.asList("intercept", "v-parameter", "n-parameter", "k-parameter"));
	}

	@Override
	public double getResponseAt(double dose)
	{
		int base = 0;
		double theDose = dose; // Math.log(dose + Math.sqrt(dose * dose + 1.0));
		double nom = curveParameters[base + 1] * Math.pow(theDose, curveParameters[base + 2]);
		double denom = Math.pow(curveParameters[base + 3], curveParameters[base + 2])
				+ Math.pow(theDose, curveParameters[base + 2]);

		return curveParameters[base] + nom / denom;
	}

	@Override
	public double getResponseAt(double dose, double[] customParameters)
	{
		int base = 0;
		double[] fixedparms = new double[customParameters.length];
		for (int i = 0; i < customParameters.length; i++)
			fixedparms[i] = customParameters[i];
		double tmp = fixedparms[3];
		fixedparms[3] = fixedparms[2];
		fixedparms[2] = tmp;

		double theDose = dose; // Math.log(dose + Math.sqrt(dose * dose + 1.0));
		double nom = fixedparms[base + 1] * Math.pow(theDose, fixedparms[base + 2]);
		double denom = Math.pow(fixedparms[base + 3], fixedparms[base + 2])
				+ Math.pow(theDose, fixedparms[base + 2]);

		return fixedparms[base] + nom / denom;
	}

	@Override
	public String getFormulaText()
	{
		return "intercept + v * dose^n/(k^n + dose^n)";
	}

	@Override
	public double[] getAllParameters()
	{
		if (curveParameters == null || otherParameters == null)
			return new double[0];

		int ii = 0;
		double[] returnval = new double[curveParameters.length + otherParameters.length];
		for (int i = 0; i < curveParameters.length; i++)
			returnval[ii++] = curveParameters[i];
		// this is to get it back in the same
		// order as it came in. hillfitthread switches the two elements
		double tmp = returnval[curveParameters.length - 1];
		returnval[curveParameters.length - 1] = returnval[curveParameters.length - 2];
		returnval[curveParameters.length - 2] = tmp;
		for (int i = 0; i < otherParameters.length; i++)
			returnval[ii++] = otherParameters[i];

		return returnval;
	}

	@Override
	public String getEquation()
	{
		int base = 0;
		StringBuilder sb = new StringBuilder("RESPONSE = " + curveParameters[base]);
		if (curveParameters[base + 1] >= 0)
		{
			sb.append(" + " + curveParameters[base + 1] + " * DOSE^");
		}
		else
		{
			sb.append(" " + curveParameters[base + 1] + " * DOSE^");
		}

		String paramN = Double.toString(curveParameters[base + 2]);

		if (curveParameters[base + 2] < 0)
		{
			paramN = "(" + curveParameters[base + 2] + ")";
		}

		sb.append(paramN + "/(");

		if (curveParameters[base + 3] >= 0)
		{
			sb.append(curveParameters[base + 3] + "^" + paramN + " + DOSE^" + paramN + ")");
		}
		else
		{
			sb.append("(" + curveParameters[base + 2] + ")^" + paramN + " + DOSE^" + paramN + ")");
		}
		return sb.toString();
	}

}
