package com.sciome.bmdexpress2.service.tpod.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Small statistics helpers needed to reproduce R behavior (sd, IQR with
 * R's default "type 7" quantile algorithm, and median-of-indices used for
 * tie-breaking in the mode detector).
 */
public final class MathUtils
{

	private MathUtils()
	{
	}

	public static double mean(double[] x)
	{
		double sum = 0;
		for (double v : x)
			sum += v;
		return sum / x.length;
	}

	/** Sample standard deviation (n-1 denominator), matching R's sd(). */
	public static double sd(double[] x)
	{
		int n = x.length;
		if (n < 2)
			return Double.NaN;
		double m = mean(x);
		double sumSq = 0;
		for (double v : x)
			sumSq += (v - m) * (v - m);
		return Math.sqrt(sumSq / (n - 1));
	}

	/** R's default quantile algorithm (type 7). `sortedX` must already be sorted ascending. */
	public static double quantile7(double[] sortedX, double p)
	{
		int n = sortedX.length;
		if (n == 1)
			return sortedX[0];
		double h = (n - 1) * p;
		int lo = (int) Math.floor(h);
		int hi = (int) Math.ceil(h);
		if (lo == hi)
			return sortedX[lo];
		return sortedX[lo] + (h - lo) * (sortedX[hi] - sortedX[lo]);
	}

	/** Inter-quartile range using R's default (type 7) quantiles, matching R's IQR(). */
	public static double iqr(double[] x)
	{
		double[] sorted = x.clone();
		Arrays.sort(sorted);
		return quantile7(sorted, 0.75) - quantile7(sorted, 0.25);
	}

	public static double min(double[] x)
	{
		double m = x[0];
		for (double v : x)
			if (v < m)
				m = v;
		return m;
	}

	public static double max(double[] x)
	{
		double m = x[0];
		for (double v : x)
			if (v > m)
				m = v;
		return m;
	}

	/** Median of a list of (index) integers, returned as a double exactly like R's median(). */
	public static double median(List<Integer> values)
	{
		int n = values.size();
		List<Integer> sorted = new ArrayList<>(values);
		Collections.sort(sorted);
		if (n % 2 == 1)
		{
			return sorted.get(n / 2);
		}
		else
		{
			return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
		}
	}

	/**
	 * Min-max normalize an array to [0,1].
	 */
	public static double[] normalize(double[] values)
	{

		if (values == null || values.length == 0)
		{
			throw new IllegalArgumentException("Array is empty.");
		}

		double min = values[0];
		double max = values[0];

		for (double v : values)
		{
			if (v < min)
				min = v;
			if (v > max)
				max = v;
		}

		double[] normalized = new double[values.length];

		if (max == min)
		{
			return normalized;
		}

		double range = max - min;

		for (int i = 0; i < values.length; i++)
		{
			normalized[i] = (values[i] - min) / range;
		}

		return normalized;
	}

	/**
	 * Average absolute step size.
	 */
	public static double averageStep(double[] values)
	{

		if (values.length < 2)
		{
			return 0;
		}

		double sum = 0;

		for (int i = 1; i < values.length; i++)
		{
			sum += Math.abs(values[i] - values[i - 1]);
		}

		return sum / (values.length - 1);
	}

	/**
	 * Difference between neighboring values.
	 */
	public static double[] diff(double[] values)
	{

		double[] d = new double[values.length - 1];

		for (int i = 0; i < d.length; i++)
		{
			d[i] = values[i + 1] - values[i];
		}

		return d;
	}

	/**
	 * Returns indexes of local maxima.
	 */
	public static List<Integer> findLocalMaxima(double[] values)
	{

		List<Integer> maxima = new ArrayList<>();

		if (values.length < 3)
		{
			return maxima;
		}

		for (int i = 1; i < values.length - 1; i++)
		{

			if (values[i] >= values[i - 1] && values[i] > values[i + 1])
			{

				maxima.add(i);
			}
		}

		return maxima;
	}

	/**
	 * Returns indexes of local minima.
	 */
	public static List<Integer> findLocalMinima(double[] values)
	{

		List<Integer> minima = new ArrayList<>();

		if (values.length < 3)
		{
			return minima;
		}

		for (int i = 1; i < values.length - 1; i++)
		{

			if (values[i] <= values[i - 1] && values[i] < values[i + 1])
			{

				minima.add(i);
			}
		}

		return minima;
	}

	/**
	 * Ensures x is strictly increasing.
	 */
	public static void validateIncreasing(double[] x)
	{

		for (int i = 1; i < x.length; i++)
		{

			if (x[i] <= x[i - 1])
			{
				throw new IllegalArgumentException("x values must be strictly increasing.");
			}
		}
	}

	/**
	 * Ensures arrays are usable.
	 */
	public static void validate(double[] x, double[] y)
	{

		if (x == null || y == null)
		{
			throw new IllegalArgumentException("Arrays cannot be null.");
		}

		if (x.length != y.length)
		{
			throw new IllegalArgumentException("x and y must have same length.");
		}

		if (x.length < 3)
		{
			throw new IllegalArgumentException("At least three points are required.");
		}

		validateIncreasing(x);
	}

	/**
	 * Returns the index of the maximum value.
	 */
	public static int argMax(double[] values)
	{

		int maxIndex = 0;

		for (int i = 1; i < values.length; i++)
		{

			if (values[i] > values[maxIndex])
			{
				maxIndex = i;
			}
		}

		return maxIndex;
	}

}