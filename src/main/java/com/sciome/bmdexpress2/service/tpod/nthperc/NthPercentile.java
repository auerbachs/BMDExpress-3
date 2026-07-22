package com.sciome.bmdexpress2.service.tpod.nthperc;

import com.sciome.bmdexpress2.service.tpod.CalcResult;

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
	public static CalcResult percentile(double[] sortedValues, double percent)
	{

		if (sortedValues == null || sortedValues.length == 0)
			throw new IllegalArgumentException("Array cannot be empty");

		if (percent < 0 || percent > 100)
			throw new IllegalArgumentException("Percent must be in [0, 100]");

		int n = sortedValues.length;

		if (n == 1)
			return new CalcResult(0, sortedValues[0]);

		double rank = (percent / 100.0) * (n - 1);

		int lowerIndex = (int) Math.floor(rank);
		int upperIndex = (int) Math.ceil(rank);

		if (lowerIndex != upperIndex)
			lowerIndex = upperIndex;

		return new CalcResult(lowerIndex, sortedValues[lowerIndex]);

		// double weight = rank - lowerIndex;

		// return new CalcResult(lowerIndex, upperIndex,
		// sortedValues[lowerIndex] * (1 - weight) + sortedValues[upperIndex] * weight);
	}

}
