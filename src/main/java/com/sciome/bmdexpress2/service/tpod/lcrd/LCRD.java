package com.sciome.bmdexpress2.service.tpod.lcrd;

/**
 * Lowest Consistent Response Dose (LCRD)
 *
 * Finds the lowest index i such that for runLength consecutive points:
 *
 * values[i+k] >= values[i] * spacingRatio^k
 */
public class LCRD
{

	public static class Result
	{
		public final int index;
		public final double dose;

		public Result(int index, double dose)
		{
			this.index = index;
			this.dose = dose;
		}

		@Override
		public String toString()
		{
			return "Result{index=" + index + ", dose=" + dose + "}";
		}
	}

	/**
	 * Main function
	 *
	 * @param values
	 *            sorted ascending dose-response or dose array
	 * @param runLength
	 *            number of consecutive points required
	 * @param spacingRatio
	 *            geometric spacing ratio between doses
	 */
	public static Result compute(double[] values, int runLength, double spacingRatio)
	{

		if (values == null || values.length == 0)
			throw new IllegalArgumentException("values cannot be empty");

		if (runLength <= 0)
			throw new IllegalArgumentException("runLength must be > 0");

		if (runLength > values.length)
			return new Result(-1, Double.NaN);

		for (int i = 0; i <= values.length - runLength; i++)
		{

			double base = values[i];

			boolean consistent = true;

			for (int k = 1; k < runLength; k++)
			{

				double expectedMin = base * Math.pow(spacingRatio, k);

				if (values[i + k] < expectedMin)
				{
					consistent = false;
					break;
				}
			}

			if (consistent)
			{
				double dose = indexToDose(values, i, spacingRatio);
				return new Result(i, dose);
			}
		}

		return new Result(-1, Double.NaN);
	}

	/**
	 * Reconstructs dose assuming geometric spacing.
	 * If values are already doses, this just returns values[i].
	 */
	private static double indexToDose(double[] values, int i, double spacingRatio)
	{

		// If spacingRatio ~ 1, assume values already are doses
		if (Math.abs(spacingRatio - 1.0) < 1e-9)
		{
			return values[i];
		}

		return values[i];
	}

	public static void main(String[] args)
	{

		double[] values = { 1, 2, 4, 8, 16, 32 };

		Result r = compute(values, 3, 2.0);

		System.out.println(r);
	}
}
