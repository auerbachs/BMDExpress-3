package com.sciome.bmdexpress2.service.tpod.lcrd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sciome.bmdexpress2.service.tpod.CalcResult;

public class LCRD
{

	/**
	 * Equivalent to the R function:
	 * LCRD(x, ratio=1.66, run.length=NULL)
	 *
	 * @param values
	 *            Input values (does not modify original array)
	 * @param ratio
	 *            Ratio threshold
	 * @param runLength
	 *            null to use original algorithm, otherwise minimum run length
	 * @return LCRD value, or Double.NaN if none found
	 */
	public static CalcResult calculate(double[] values, double ratio, Integer runLength)
	{

		if (values == null || values.length < 2)
		{
			return new CalcResult(-1, Double.NaN);

		}

		// Sort ascending
		double[] x = values.clone();
		Arrays.sort(x);

		int n = x.length;

		// Calculate consecutive ratios
		double[] ratios = new double[n - 1];
		for (int i = 0; i < n - 1; i++)
		{
			ratios[i] = x[i + 1] / x[i];
		}

		List<Integer> indexes = new ArrayList<>();
		List<Integer> nonIndexes = new ArrayList<>();

		// R indices are 1-based
		for (int i = 0; i < ratios.length; i++)
		{
			if (ratios[i] < ratio)
			{
				indexes.add(i + 1);
			}
			else
			{
				nonIndexes.add(i + 1);
			}
		}

		if (runLength == null)
		{

			Integer lcrdIndex;

			if (!nonIndexes.isEmpty())
			{
				lcrdIndex = CollectionsUtil.max(nonIndexes) + 1;
			}
			else if (!indexes.isEmpty())
			{
				lcrdIndex = indexes.get(0);
			}
			else
			{
				return new CalcResult(-1, Double.NaN);
			}

			// Do not allow last ranked BMC
			if (lcrdIndex == n)
			{
				return new CalcResult(-1, Double.NaN);
			}

			return new CalcResult(lcrdIndex - 1, x[lcrdIndex - 1]);
		}

		// ---------------------------------------------------------
		// run.length version
		// ---------------------------------------------------------

		List<Integer> runSizes = new ArrayList<>();

		if (nonIndexes.size() > 1)
		{
			for (int i = 0; i < nonIndexes.size() - 1; i++)
			{
				runSizes.add(nonIndexes.get(i + 1) - nonIndexes.get(i));
			}
		}

		Integer lcrdIndex = null;

		if (!nonIndexes.isEmpty() && nonIndexes.get(0) != (n - 1))
		{

			runSizes.add(n - nonIndexes.get(nonIndexes.size() - 1));

			for (int i = 0; i < runSizes.size(); i++)
			{
				if (runSizes.get(i) >= runLength)
				{
					lcrdIndex = nonIndexes.get(i);
					break;
				}
			}

			if (lcrdIndex != null && lcrdIndex < (n - 1))
			{
				lcrdIndex++;
			}

			if (lcrdIndex == null && !indexes.isEmpty() && indexes.get(0) == 1)
			{

				int firstRun = nonIndexes.get(0) - 1;

				if (firstRun >= runLength)
				{
					lcrdIndex = indexes.get(0);
				}
			}

		}
		else
		{

			if (!indexes.isEmpty())
			{
				runSizes.add(indexes.get(indexes.size() - 1));
			}

			for (int i = 0; i < runSizes.size(); i++)
			{
				if (runSizes.get(i) >= runLength)
				{
					lcrdIndex = indexes.get(i);
					break;
				}
			}
		}

		if (lcrdIndex == null)
		{
			return new CalcResult(-1, Double.NaN);
		}

		return new CalcResult(lcrdIndex - 1, x[lcrdIndex - 1]);
	}

	/**
	 * Convenience overload matching R defaults.
	 */
	public static CalcResult calculate(double[] values)
	{
		return calculate(values, 1.66, null);
	}

	/**
	 * Simple helper since Java lacks Collections.max for primitive lists.
	 */
	private static class CollectionsUtil
	{
		static int max(List<Integer> list)
		{
			int max = Integer.MIN_VALUE;
			for (int v : list)
			{
				if (v > max)
				{
					max = v;
				}
			}
			return max;
		}
	}

	public static void main(String[] args)
	{

		double[] x = { 1.2, 1.3, 1.31, 1.35, 2.6, 2.8, 3.0, 3.2 };

		System.out.println(calculate(x));
		System.out.println(calculate(x, 1.66, 3));
	}
}