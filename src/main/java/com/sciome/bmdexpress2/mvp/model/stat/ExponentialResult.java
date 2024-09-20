package com.sciome.bmdexpress2.mvp.model.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExponentialResult extends StatResult
{

	private static final long serialVersionUID = -527776055122273597L;

	/**
	 * 
	 */
	private int option;

	public ExponentialResult()
	{
		super();
	}

	public int getOption()
	{
		return option;
	}

	public void setOption(int o)
	{
		this.option = o;
	}

	@Override
	public List<String> getColumnNames()
	{
		String expName = "Exp " + option;
		List<String> residualHeader = getResidualHeader(expName + " Residual ");
		List<String> returnList = new ArrayList<String>(Arrays.asList(expName + " BMD", expName + " BMDL",
				expName + " BMDU", "Exp " + option + " fitPValue", expName + " fitLogLikelihood",
				expName + " AIC", expName + " adverseDirection", expName + " BMD/BMDL",
				expName + " Execution Complete"));

		List<String> parameters = this.getParametersNames();
		for (String parameter : parameters)
			returnList.add(expName + " Parameter " + parameter);

		returnList.add(expName + " RSquared");
		returnList.addAll(residualHeader);
		returnList.add(expName + " Is Step Function Less Than Lowest Dose");
		returnList.add(expName + " Z-Score");
		returnList.add(expName + " ABS Z-Score");
		returnList.add(expName + " Modelled Response BMR Multiples");
		returnList.add(expName + " ABS Modelled Response BMR Multiples");
		returnList.add(expName + " Fold Change Top To Bottom (Model)");
		returnList.add(expName + " ABS Fold Change Top To Bottom (Model)");

		return returnList;

	}

	@Override
	public List<Object> getRow()
	{

		List<Object> returnList = new ArrayList<Object>(Arrays.asList((this.getBMD()), (this.getBMDL()),
				(this.getBMDU()), (this.getFitPValue()), (this.getFitLogLikelihood()), (this.getAIC()),
				(this.getAdverseDirection()), (this.getBMDdiffBMDL()), this.getSuccess()));
		int pcount = 2;
		int start = 1;
		// option 2 and 3 have a "sign" as a parameter too.
		// the first parameter array is sign. this is not needed for option 4 and 5 because
		// the sign is part of the formula.
		if (option == 2 || option == 3)
			start = 0;
		if (option == 3 || option == 4)
			pcount = 3;
		else if (option == 5)
			pcount = 4;
		for (int i = start; i <= pcount; i++)
		{
			if (curveParameters != null)
				returnList.add(new Double(this.curveParameters[i]));
			else
				returnList.add(null);
		}

		returnList.add(getrSquared());
		returnList.addAll(getResidualList());
		returnList.add(isStepWithBMDLessLowest());
		returnList.add(this.getZscore());
		returnList.add(this.getAbsZScore());
		returnList.add(this.getBmrCountsToTop());
		returnList.add(this.getAbsBmrCountsToTop());
		returnList.add(this.getFoldChangeToTop());
		returnList.add(this.getAbsFoldChangeToTop());
		return returnList;
	}

	@Override
	public String toString()
	{
		return "Exp " + option;
	}

	@Override
	public List<String> getParametersNames()
	{
		if (option == 2)
		{
			return new ArrayList<String>(Arrays.asList("sign", "a", "b"));
		}
		else if (option == 3)
		{
			return new ArrayList<String>(Arrays.asList("sign", "a", "b", "d"));
		}
		else if (option == 4)
		{
			return new ArrayList<String>(Arrays.asList("a", "b", "c"));
		}
		else if (option == 5)
		{
			return new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
		}
		else
			return new ArrayList<>();
	}

	@Override
	public double getResponseAt(double d)
	{
		if (option == 2)
		{
			return exp2Function(0, d);
		}
		else if (option == 3)
		{
			return exp3Function(0, d);
		}
		else if (option == 4)
		{
			return exp4Function(0, d);
		}
		else if (option == 5)
		{
			return exp5Function(0, d);
		}
		return 0.0;
	}

	/**
	 * Exp functioin
	 */
	private double exp2Function(int base, double dose)
	{
		double a = curveParameters[base + 1];
		double b = curveParameters[base + 2];

		return a * Math.exp(curveParameters[base] * b * dose);
	}

	/**
	 * Exp functioin
	 */
	private double exp3Function(int base, double dose)
	{
		double a = curveParameters[base + 1];
		double b = curveParameters[base + 2];
		double d = curveParameters[base + 3];

		double expvalue = Math.pow(b * dose, d);
		return a * Math.exp(curveParameters[base] * expvalue);
	}

	/**
	 * Exp functioin
	 */
	private double exp4Function(int base, double dose)
	{
		double a = curveParameters[base + 1];
		double b = curveParameters[base + 2];
		double c = curveParameters[base + 3];
		return a * (c - (c - 1) * Math.exp(-b * dose));
	}

	// for the case of exp3 and exp5, the first "parameter" stored
	// is actually a sign..aka -1 or 1. so we do not return
	// that parameter with this method.

	@Override
	public double[] getAllParameters()
	{
		// maybe in the case of model averaging or gcurvep
		if (curveParameters == null || otherParameters == null)
			return new double[0];

		if (option == 3)
		{
			double[] returnval = new double[3 + otherParameters.length];
			returnval[0] = curveParameters[1];
			returnval[1] = curveParameters[2];
			returnval[2] = curveParameters[3];
			int ii = 3;
			for (int i = 0; i < otherParameters.length; i++)
				returnval[ii++] = otherParameters[i];

			return returnval;
		}
		else if (option == 5)
		{
			double[] returnval = new double[4 + otherParameters.length];
			returnval[0] = curveParameters[1];
			returnval[1] = curveParameters[2];
			returnval[2] = curveParameters[3];
			returnval[3] = curveParameters[4];
			int ii = 4;
			for (int i = 0; i < otherParameters.length; i++)
				returnval[ii++] = otherParameters[i];

			return returnval;
		}

		else
			return super.getAllParameters();

	}

	/**
	 * Exp functioin
	 */
	private double exp5Function(int base, double dose)
	{
		double a = curveParameters[base + 1];
		double b = curveParameters[base + 2];
		double c = curveParameters[base + 3];
		double d = curveParameters[base + 4];
		double expvalue = Math.pow(b * dose, d);
		return a * (c - (c - 1) * Math.exp(-expvalue));
	}

	// this implementation is assuming the parameters are coming straight from toxicR
	// it was written to handle calculating gradients, so it is slightly
	// different than the main getResponseAt because we store the parameters
	// slightly differently. we store the parameters with a anti-logged c value
	// and we store the sign as a parameter in bmdexpress. toxicr doesnt'
	// send it that way
	@Override
	public double getResponseAt(double d, double[] customParameters)
	{
		if (option == 2)
		{
			return exp2Function(0, d, customParameters);
		}
		else if (option == 3)
		{
			return exp3Function(0, d, customParameters);
		}
		else if (option == 4)
		{
			return exp4Function(0, d, customParameters);
		}
		else if (option == 5)
		{
			return exp5Function(0, d, customParameters);
		}
		return 0.0;
	}

	/**
	 * Exp functioin
	 */
	private double exp2Function(int base, double dose, double[] customParameters)
	{
		double a = customParameters[base + 1];
		double b = customParameters[base + 2];

		return a * Math.exp(customParameters[base] * b * dose);
	}

	/**
	 * Exp functioin
	 */
	private double exp3Function(int base, double dose, double[] customParameters)
	{
		double a = customParameters[base];
		double b = customParameters[base + 1];
		double d = customParameters[base + 2];

		double expvalue = Math.pow(b * dose, d);
		// the sign parameter is really not a parameter that is returned by
		// toxicr. it is added when model is brought into bmdexpress
		// since this is meant for calculating gradient, keep in line
		// with toxicr
		return a * Math.exp(this.curveParameters[base] * expvalue);
	}

	/**
	 * Exp functioin
	 */
	private double exp4Function(int base, double dose, double[] customParameters)
	{
		double a = customParameters[base + 1];
		double b = customParameters[base + 2];
		double c = customParameters[base + 3];

		return a * (c - (c - 1) * Math.exp(-b * dose));
	}

	/**
	 * Exp functioin
	 */
	private double exp5Function(int base, double dose, double[] customParameters)
	{
		double a = customParameters[base];
		double b = customParameters[base + 1];
		double c = customParameters[base + 2];
		double d = customParameters[base + 3];
		c = Math.pow(Math.E, c);
		double expvalue = Math.pow(b * dose, d);
		return a * (c - (c - 1) * Math.exp(-expvalue));
	}

	@Override
	public String getFormulaText()
	{
		String formula = "";

		if (option == 2)
		{
			formula = "a * exp(b * dose)";
		}
		else if (option == 3)
		{
			formula = "a * exp((b * dose)^d)";
		}
		else if (option == 4)
		{
			formula = "a * (c - (c - 1) * exp(-b * dose))";
		}
		else if (option == 5)
		{
			formula = "a * (c - (c - 1) * exp(-(b * dose)^d))";
		}

		return formula;
	}

	@Override
	public String getEquation()
	{
		int base = 0;
		StringBuilder sb = new StringBuilder("RESPONSE = " + curveParameters[base]);

		if (option == 2)
		{
			sb = new StringBuilder("RESPONSE = ");
			double a = curveParameters[base + 1];
			double b = curveParameters[base + 2];
			sb.append(a + " * EXP(" + curveParameters[base] + " * " + b + " * DOSE)");
		}
		else if (option == 3)
		{
			sb = new StringBuilder("RESPONSE = ");
			double a = curveParameters[base + 1];
			double b = curveParameters[base + 2];
			double d = curveParameters[base + 3];
			sb.append(a + " * EXP((" + curveParameters[base] + " * " + b + " * DOSE)^" + d + ")");
		}
		else if (option == 4)
		{
			sb = new StringBuilder("RESPONSE = ");
			double a = curveParameters[base + 1];
			double b = curveParameters[base + 2];
			double c = curveParameters[base + 3];
			sb.append(a + " * (" + c + " - (" + c + " - 1) * EXP(-" + b + " * DOSE))");
		}
		else if (option == 5)
		{
			sb = new StringBuilder("RESPONSE = ");
			double a = curveParameters[base + 1];
			double b = curveParameters[base + 2];
			double c = curveParameters[base + 3];
			double d = curveParameters[base + 4];
			sb.append(a + " * (" + c + " - (" + c + " - 1) * EXP(-(" + b + " * DOSE)^" + d + "))");
		}
		return sb.toString();
	}

}
