package com.sciome.bmdexpress2.service.tpod.math;

/**
 * Gaussian kernel density estimation matching R's density(x, bw = "nrd0").
 *
 * R's implementation uses linear binning + FFT for speed; this uses direct
 * summation instead, which is simpler and numerically equivalent (just
 * slower for very large data sets). Defaults (n = 512 grid points, cut = 3
 * bandwidths of padding on each side of the data range) match R's defaults.
 */
public final class KernelDensity
{

	private KernelDensity()
	{
	}

	/** bw.nrd0 - R's default ("Silverman's rule of thumb", 0.9x scaled) bandwidth selector. */
	public static double bwNrd0(double[] x)
	{
		int n = x.length;
		if (n < 2)
		{
			throw new IllegalArgumentException("need at least 2 data points to select a bandwidth");
		}
		double hi = MathUtils.sd(x);
		double lo;
		if (!Double.isNaN(hi))
		{
			lo = Math.min(hi, MathUtils.iqr(x) / 1.34);
		}
		else
		{
			lo = MathUtils.iqr(x) / 1.34;
		}
		if (lo == 0)
		{
			if (!Double.isNaN(hi))
			{
				lo = hi;
			}
			else if (Math.abs(x[0]) > 0)
			{
				lo = Math.abs(x[0]);
			}
			else
			{
				lo = 1;
			}
		}
		return 0.9 * lo * Math.pow(n, -0.2);
	}

	/** density(x, bw="nrd0") with R's default grid size (512) and padding (cut=3). */
	public static DensityEstimate density(double[] x, double bw)
	{
		return density(x, bw, 512, 3.0);
	}

	public static DensityEstimate density(double[] x, double bw, int n, double cut)
	{
		double minX = MathUtils.min(x);
		double maxX = MathUtils.max(x);
		double from = minX - cut * bw;
		double to = maxX + cut * bw;

		double[] xs = new double[n];
		double[] ys = new double[n];
		double step = (to - from) / (n - 1);
		double norm = 1.0 / (x.length * bw * Math.sqrt(2 * Math.PI));

		for (int i = 0; i < n; i++)
		{
			double xi = from + i * step;
			double sum = 0;
			for (double xj : x)
			{
				double z = (xi - xj) / bw;
				sum += Math.exp(-0.5 * z * z);
			}
			xs[i] = xi;
			ys[i] = sum * norm;
		}
		return new DensityEstimate(xs, ys);
	}
}