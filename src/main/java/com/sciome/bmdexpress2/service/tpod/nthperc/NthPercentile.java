package com.sciome.bmdexpress2.service.tpod.nthperc;

public class NthPercentile
{

	/**
	 * Returns the percentile value from a sorted array.
	 *
	 * @param sortedValues
	 *            ascending sorted array
	 * @param percent
	 *            value from 0 to 100
	 * @return percentile value
	 */
	public static double percentile(double[] sortedValues, double percent)
	{

		if (sortedValues == null || sortedValues.length == 0)
			throw new IllegalArgumentException("Array cannot be empty");

		if (percent < 0 || percent > 100)
			throw new IllegalArgumentException("Percent must be in [0, 100]");

		int n = sortedValues.length;

		if (n == 1)
			return sortedValues[0];

		double rank = (percent / 100.0) * (n - 1);

		int lowerIndex = (int) Math.floor(rank);
		int upperIndex = (int) Math.ceil(rank);

		if (lowerIndex == upperIndex)
		{
			return sortedValues[lowerIndex];
		}

		double weight = rank - lowerIndex;

		return sortedValues[lowerIndex] * (1 - weight) + sortedValues[upperIndex] * weight;
	}

	public static void main(String[] args)
	{

		double[] values = { 1, 2, 4, 8, 16 };

		System.out.println(percentile(values, 0)); // min
		System.out.println(percentile(values, 50)); // median-ish
		System.out.println(percentile(values, 100)); // max
	}
}
