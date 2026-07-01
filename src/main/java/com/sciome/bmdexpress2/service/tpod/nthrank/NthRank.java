package com.sciome.bmdexpress2.service.tpod.nthrank;

import java.util.Arrays;

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
	 * Returns the Nth rank value (1-based).
	 *
	 * @param values
	 *            array of doubles
	 * @param n
	 *            rank (1-based)
	 * @return Nth smallest value
	 */
	public static double rank(double[] values, int n)
	{

		if (values == null || values.length == 0)
			throw new IllegalArgumentException("values cannot be empty");

		if (n <= 0 || n > values.length)
			throw new IllegalArgumentException("n must be between 1 and " + values.length);

		double[] copy = Arrays.copyOf(values, values.length);
		Arrays.sort(copy);

		return copy[n - 1];
	}

	/**
	 * Faster version if array is already sorted ascending.
	 */
	public static double rankSorted(double[] sortedValues, int n)
	{

		if (sortedValues == null || sortedValues.length == 0)
			throw new IllegalArgumentException("values cannot be empty");

		if (n <= 0 || n > sortedValues.length)
			throw new IllegalArgumentException("n must be between 1 and " + sortedValues.length);

		return sortedValues[n - 1];
	}

	public static void main(String[] args)
	{

		double[] values = { 5, 1, 9, 2, 10 };

		System.out.println(rank(values, 2)); // 2nd smallest → 2
		System.out.println(rankSorted(new double[] { 1, 2, 5, 9, 10 }, 2)); // 2
	}
}