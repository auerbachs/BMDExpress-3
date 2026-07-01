package com.sciome.bmdexpress2.service.tpod;

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
}