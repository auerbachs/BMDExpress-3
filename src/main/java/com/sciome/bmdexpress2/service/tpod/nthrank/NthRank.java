package com.sciome.bmdexpress2.service.tpod.nthrank;

import com.sciome.bmdexpress2.service.tpod.CalcResult;

/**
 * Nth Rank Selector (Order Statistic)
 *
 * Returns the Nth smallest value from a sorted or unsorted array.
 * Uses 1-based indexing for rank:
 * n = 1 -> smallest
 * n = 2 -> second smallest
 */
public class NthRank
{

	/**
	 * Faster version if array is already sorted ascending.
	 */
	public static CalcResult rankSorted(double[] sortedValues, int n)
	{

		if (sortedValues == null || sortedValues.length == 0)
			throw new IllegalArgumentException("values cannot be empty");

		if (n <= 0 || n > sortedValues.length)
			throw new IllegalArgumentException("n must be between 1 and " + sortedValues.length);

		return new CalcResult(n - 1, sortedValues[n - 1]);
	}

}