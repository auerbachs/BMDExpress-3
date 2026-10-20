package com.sciome.bmdexpress2.service.tpod.maxcurve;

import java.util.List;

import com.sciome.bmdexpress2.service.tpod.math.MathUtils;

/**
 * Java implementation of the Kneedle algorithm
 * (Satopaa et al., 2011).
 *
 * Detects "knee points" in a curve.
 */
public class Kneedle
{

	private final double sensitivity;
	private final CurveType curveType;

	public Kneedle()
	{
		this(0.5, CurveType.INCREASING_CONVEX);
	}

	public Kneedle(double sensitivity, CurveType curveType)
	{
		if (sensitivity <= 0)
		{
			throw new IllegalArgumentException("Sensitivity must be > 0");
		}
		this.sensitivity = sensitivity;
		this.curveType = curveType;
	}

	/**
	 * Main entry point.
	 */
	public KneePoint findKnee(double[] x, double[] y)
	{

		MathUtils.validate(x, y);

		double[] xn = MathUtils.normalize(x);
		double[] yn = MathUtils.normalize(y);

		double[] transformed = transform(xn, yn);

		double[] diffCurve = new double[transformed.length];

		for (int i = 0; i < transformed.length; i++)
		{
			diffCurve[i] = transformed[i] - xn[i];
		}

		List<Integer> maxima = MathUtils.findLocalMaxima(diffCurve);

		if (maxima.isEmpty())
		{
			int idx = MathUtils.argMax(diffCurve);
			return buildPoint(idx, x, y, xn, yn, diffCurve);
		}

		double avgStep = MathUtils.averageStep(diffCurve);
		double thresholdOffset = sensitivity * avgStep;

		for (int peak : maxima)
		{

			double peakValue = diffCurve[peak];
			double threshold = peakValue - thresholdOffset;

			for (int i = peak; i < diffCurve.length; i++)
			{

				if (diffCurve[i] < threshold)
				{
					return buildPoint(i, x, y, xn, yn, diffCurve);
				}
			}
		}

		int fallback = MathUtils.argMax(diffCurve);
		return buildPoint(fallback, x, y, xn, yn, diffCurve);
	}

	/**
	 * Handles curve transformations based on type.
	 */
	private double[] transform(double[] x, double[] y)
	{

		double[] t = new double[x.length];

		boolean increasing = curveType.isIncreasing();
		boolean concave = curveType.isConcave();

		for (int i = 0; i < x.length; i++)
		{

			double val = y[i];

			if (!increasing)
			{
				val = 1.0 - val;
			}

			if (concave)
			{
				t[i] = val;
			}
			else
			{
				t[i] = 1.0 - val;
			}
		}

		return t;
	}

	private KneePoint buildPoint(int index, double[] x, double[] y, double[] xn, double[] yn, double[] diff)
	{

		return new KneePoint(index, x[index], y[index], xn[index], yn[index], diff[index]);
	}

	/**
	 * Convenience static method.
	 */
	public static KneePoint find(double[] x, double[] y)
	{
		return new Kneedle().findKnee(x, y);
	}

	/**
	 * Convenience static method with settings.
	 */
	public static KneePoint find(double[] x, double[] y, double sensitivity, CurveType type)
	{
		return new Kneedle(sensitivity, type).findKnee(x, y);
	}
}
