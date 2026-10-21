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
	 * @param b
	 * @return percentile value
	 */
	public static CalcResult percentile(double[] sortedValues, double percent, boolean isAllGeneSets,
			Integer numGenesInGeneSet)
	{

		if (sortedValues == null || sortedValues.length < 2)
		{
			return new CalcResult(-1, Double.NaN);

		}

		int n = sortedValues.length;

		if (n == 1)
			return new CalcResult(0, sortedValues[0]);

		if (isAllGeneSets && numGenesInGeneSet != null)
			n = numGenesInGeneSet;
		else if (isAllGeneSets)
		{
			System.out.println("All genesets is being used, but number of gene sets is null");
			return new CalcResult(-1, Double.NaN);

		}

		double rank = (percent / 100.0) * n;

		int lowerIndex = (int) Math.floor(rank);
		int upperIndex = (int) Math.ceil(rank);

		if (lowerIndex != upperIndex)
			lowerIndex = upperIndex;

		double result = Double.NaN;

		if (lowerIndex < sortedValues.length)
			result = sortedValues[lowerIndex];

		return new CalcResult(lowerIndex, result);

		// double weight = rank - lowerIndex;

		// return new CalcResult(lowerIndex, upperIndex,
		// sortedValues[lowerIndex] * (1 - weight) + sortedValues[upperIndex] * weight);
	}

}
