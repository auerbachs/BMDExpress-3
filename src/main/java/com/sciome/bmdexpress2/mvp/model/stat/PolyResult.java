package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolyResult extends StatResult
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8080785183059201658L;

	private int degree;

	public PolyResult()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public int getDegree()
	{
		return degree;
	}

	public void setDegree(int degree)
	{
		this.degree = degree;
	}

	@Override
	public List<String> getColumnNames()
	{
		String polyname = "Linear";
		if (degree > 1)
		{
			polyname = "Poly " + String.valueOf(degree);
		}

		List<String> residualHeader = getResidualHeader(polyname + " Residual ");

		List<String> returnList = new ArrayList<String>(Arrays.asList(polyname + " BMD", polyname + " BMDL",
				polyname + " BMDU", polyname + " fitPValue", polyname + " fitLogLikelihood",
				polyname + " AIC", polyname + " adverseDirection", polyname + " BMD/BMDL",
				polyname + " Execution Complete"));

		for (int i = 0; i < this.curveParameters.length; i++)
			returnList.add(polyname + " Parameter beta_" + i);

		returnList.add(polyname + " RSquared");
		returnList.addAll(residualHeader);
		returnList.add(polyname + "  Is Step Function Less Than Lowest Dose");
		return returnList;

	}

	@Override
	public List<Object> getRow()
	{
		List<Object> returnList = new ArrayList<Object>(Arrays.asList((this.getBMD()), (this.getBMDL()),
				(this.getBMDU()), (this.getFitPValue()), (this.getFitLogLikelihood()), (this.getAIC()),
				(this.getAdverseDirection()), (this.getBMDdiffBMDL()), this.getSuccess()));

		for (int i = 0; i < this.curveParameters.length; i++)
		{
			if (curveParameters != null)
				returnList.add(new Double(this.curveParameters[i]));
			else
				returnList.add(null);
		}
		returnList.add(getrSquared());
		returnList.addAll(getResidualList());
		returnList.add(isStepWithBMDLessLowest());
		return returnList;
	}

	@Override
	public String toString()
	{
		String polyname = "Linear";
		if (degree > 1)
		{
			polyname = "Poly " + String.valueOf(degree);
		}
		return polyname;
	}

	@Override
	public List<String> getParametersNames()
	{
		List<String> parameters = new ArrayList<>();

		for (int i = 0; i <= degree; i++)
		{
			parameters.add("beta_" + i);
		}
		return parameters;
	}

	@Override
	public double getResponseAt(double d)
	{
		return polyFunction(d, degree);
	}

	/**
	 * Polynomial dynamic degree function
	 */
	private double polyFunction(double dose, int degree)
	{
		int base = 0;
		int start = base + 1;

		return curveParameters[base] + polyValue(dose, start, 1, degree);
	}

	/**
	 * Recursive function
	 */
	private double polyValue(double dose, int index, int cur, int max)
	{
		if (cur == max)
		{
			return curveParameters[index] * Math.pow(dose, max);
		}
		else
		{
			return curveParameters[index] * Math.pow(dose, cur) + polyValue(dose, index + 1, cur + 1, max);
		}
	}

	@Override
	public String getFormulaText()
	{
		StringBuilder sb = new StringBuilder("y[dose] = beta_0 + beta_1 * dose"); // degree == 1

		for (int d = 2; d <= degree; d++)
		{
			sb.append(" + beta_" + d + " * dose^" + d);
		}

		return sb.toString();
	}

	@Override
	public String getEquation()
	{
		int base = 0;
		StringBuilder sb = new StringBuilder("RESPONSE = " + curveParameters[base]);

		if (degree > 0)
		{
			for (int i = 1; i <= degree; i++)
			{
				if (curveParameters[base + i] >= 0)
				{ // positive
					sb.append(" + " + curveParameters[base + i] + " * DOSE");
				}
				else
				{ // negative
					sb.append(" " + curveParameters[base + i] + " * DOSE");
				}

				if (i > 1)
				{
					sb.append("^" + i);
				}
			}
		}

		return sb.toString();
	}

}
