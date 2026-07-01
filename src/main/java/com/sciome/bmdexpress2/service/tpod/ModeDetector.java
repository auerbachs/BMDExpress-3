package com.sciome.bmdexpress2.service.tpod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Direct port of the R functions Modes2() and mode.antimode():
 * finds local modes (and, for mode.antimode, the anti-modes/valleys between
 * them) of a Gaussian kernel density estimate of a numeric sample.
 *
 * Both methods walk the density curve looking for direction changes
 * (increasing -> decreasing = a mode, decreasing -> increasing = an
 * anti-mode), exactly like the original R code.
 */
public final class ModeDetector
{

	private ModeDetector()
	{
	}

	// ------------------------------------------------------------------
	// shared helpers
	// ------------------------------------------------------------------

	private static double[] toFiniteDoubleArray(double[] x)
	{
		List<Double> out = new ArrayList<>();
		for (double v : x)
		{
			if (Double.isFinite(v))
				out.add(v);
		}
		double[] arr = new double[out.size()];
		for (int i = 0; i < arr.length; i++)
			arr[i] = out.get(i);
		return arr;
	}

	private static boolean isConstant(double[] x)
	{
		for (double v : x)
			if (v != x[0])
				return false;
		return true;
	}

	/** 1 where the density is increasing, 0 where it's flat/decreasing (R's `incr`). */
	private static int[] incrArray(double[] y)
	{
		int n = y.length;
		int[] incr = new int[n - 1];
		for (int i = 0; i < n - 1; i++)
		{
			incr[i] = (y[i + 1] - y[i]) > 0 ? 1 : 0;
		}
		return incr;
	}

	/** Indices where the density curve changes direction (R's `begin`), 0-based. */
	private static List<Integer> beginIndices(int[] incr)
	{
		List<Integer> begin = new ArrayList<>();
		begin.add(0);
		for (int i = 1; i < incr.length; i++)
		{
			if (incr[i] != incr[i - 1])
				begin.add(i);
		}
		begin.add(incr.length - 1);
		return begin;
	}

	private static double sum(double[] a, int from, int to)
	{ // inclusive
		double s = 0;
		for (int i = from; i <= to; i++)
			s += a[i];
		return s;
	}

	/**
	 * R-style tie-break: take the median of a set of tied indices; if that
	 * median is fractional (an even number of ties whose midpoint falls
	 * between two indices), drop the last tied index and take the median
	 * again. Returns an index local to the tied list's own numbering.
	 */
	private static int tieBreakIndex(List<Integer> tied)
	{
		double med = MathUtils.median(tied);
		if (med % 1 != 0)
		{
			List<Integer> trimmed = tied.subList(0, tied.size() - 1);
			return (int) MathUtils.median(trimmed);
		}
		return (int) med;
	}

	// ------------------------------------------------------------------
	// Modes2
	// ------------------------------------------------------------------

	/** Convenience overload: bandwidth chosen automatically via bw.nrd0, like R's default bw="nrd0". */
	public static ModeResult modes2(double[] x, double minSize)
	{
		double[] clean = toFiniteDoubleArray(x);
		return modes2(clean, minSize, KernelDensity.bwNrd0(clean));
	}

	public static ModeResult modes2(double[] xRaw, double minSize, double bw)
	{
		double[] x = toFiniteDoubleArray(xRaw);
		if (isConstant(x))
		{
			return new ModeResult(new double[] { Double.NaN }, new double[] { Double.NaN },
					new double[] { 1 });
		}

		DensityEstimate dens = KernelDensity.density(x, bw);
		double[] y = dens.y;
		double[] xg = dens.x;

		int[] incr = incrArray(y);
		List<Integer> begin = beginIndices(incr);
		int count = begin.size() - 1;

		double sumdens = sum(y, 0, y.length - 1);

		int nModes = count / 2;
		double[] size = new double[nModes];
		double[] modes = new double[nModes];
		double[] modeDens = new double[nModes];

		int init = 0;
		if (incr[0] == 0)
		{
			size[0] = sum(y, begin.get(0), begin.get(1)) / sumdens;
			init = 1;
		}

		int j = init;
		for (int i = init; i < nModes; i++)
		{
			int b0 = begin.get(j);
			int b2 = begin.get(j + 2);
			size[i] = sum(y, b0, b2) / sumdens;

			// R's Modes2 takes kde$x[kde$y == max(kde$y)] with no tie-break;
			// assigning a multi-element vector into modes[i] causes R to
			// silently keep only the first matching element, so we do the same.
			int maxIdx = b0;
			for (int k = b0; k <= b2; k++)
			{
				if (y[k] > y[maxIdx])
					maxIdx = k;
			}
			modes[i] = xg[maxIdx];
			modeDens[i] = y[maxIdx];
			j += 2;
		}

		// sort by mode density, descending
		Integer[] order = new Integer[nModes];
		for (int i = 0; i < nModes; i++)
			order[i] = i;
		final double[] modeDensFinal = modeDens;
		Arrays.sort(order, (a, b) -> Double.compare(modeDensFinal[b], modeDensFinal[a]));

		double[] sModes = new double[nModes];
		double[] sDens = new double[nModes];
		double[] sSize = new double[nModes];
		for (int i = 0; i < nModes; i++)
		{
			sModes[i] = modes[order[i]];
			sDens[i] = modeDens[order[i]];
			sSize[i] = size[order[i]];
		}

		// NOTE: this reproduces a quirk in the original R code. It only
		// removes small modes when at least one is below the *hardcoded*
		// 0.1 threshold, but the actual removal condition uses `minSize`.
		// That means if every mode's size is >= 0.1 but below `minSize`,
		// R's Modes2 (and this port) will keep them all anyway.
		boolean anyBelowPointOne = false;
		for (double s : sSize)
			if (s < 0.1)
			{
				anyBelowPointOne = true;
				break;
			}

		List<Double> fModes = new ArrayList<>();
		List<Double> fDens = new ArrayList<>();
		List<Double> fSize = new ArrayList<>();
		if (anyBelowPointOne)
		{
			for (int i = 0; i < nModes; i++)
			{
				if (sSize[i] >= minSize)
				{
					fModes.add(sModes[i]);
					fDens.add(sDens[i]);
					fSize.add(sSize[i]);
				}
			}
		}
		else
		{
			for (int i = 0; i < nModes; i++)
			{
				fModes.add(sModes[i]);
				fDens.add(sDens[i]);
				fSize.add(sSize[i]);
			}
		}

		double totalSize = 0;
		for (double s : fSize)
			totalSize += s;
		double[] finalSize = new double[fSize.size()];
		for (int i = 0; i < finalSize.length; i++)
		{
			finalSize[i] = (totalSize > 1) ? fSize.get(i) / totalSize : fSize.get(i);
		}

		double[] finalModes = new double[fModes.size()];
		double[] finalDens = new double[fDens.size()];
		for (int i = 0; i < finalModes.length; i++)
		{
			finalModes[i] = fModes.get(i);
			finalDens[i] = fDens.get(i);
		}

		return new ModeResult(finalModes, finalDens, finalSize);
	}

	// ------------------------------------------------------------------
	// mode.antimode
	// ------------------------------------------------------------------

	/** Convenience overload: bandwidth chosen automatically via bw.nrd0, like R's default bw="nrd0". */
	public static ModeAntimodeResult modeAntimode(double[] x, double minSize)
	{
		double[] clean = toFiniteDoubleArray(x);
		return modeAntimode(clean, minSize, KernelDensity.bwNrd0(clean));
	}

	public static ModeAntimodeResult modeAntimode(double[] xRaw, double minSize, double bw)
	{
		double[] x = toFiniteDoubleArray(xRaw);

		double[] uniq = Arrays.stream(x).distinct().toArray();
		if (uniq.length <= 1)
		{
			return new ModeAntimodeResult(new double[] { Double.NaN }, new double[] { Double.NaN },
					new double[] { 1 }, null);
		}

		DensityEstimate dens = KernelDensity.density(x, bw);
		double[] y = dens.y;
		double[] xg = dens.x;

		int[] incr = incrArray(y);
		List<Integer> begin = beginIndices(incr);
		int count = begin.size() - 1;

		double sumdens = sum(y, 0, y.length - 1);

		int nModes = count / 2;
		double[] size = new double[nModes];
		double[] modes = new double[nModes];
		double[] modeDens = new double[nModes];

		int init = 0;
		if (incr[0] == 0)
		{
			size[0] = sum(y, begin.get(0), begin.get(1)) / sumdens;
			init = 1;
		}

		int j = init;
		for (int i = init; i < nModes; i++)
		{
			int b0 = begin.get(j);
			int b2 = begin.get(j + 2);
			size[i] = sum(y, b0, b2) / sumdens;

			double maxVal = Double.NEGATIVE_INFINITY;
			for (int k = b0; k <= b2; k++)
			{
				if (y[k] > maxVal)
					maxVal = y[k];
			}
			List<Integer> highs = new ArrayList<>();
			for (int k = b0; k <= b2; k++)
			{
				if (y[k] == maxVal)
					highs.add(k - b0); // local (segment-relative) index
			}
			int highPoint = tieBreakIndex(highs) + b0;

			modes[i] = xg[highPoint];
			modeDens[i] = y[highPoint];
			j += 2;
		}

		// remove modes smaller than min.size (mode.antimode uses min.size
		// consistently, unlike Modes2's hardcoded-0.1 quirk above)
		List<Double> fModes = new ArrayList<>();
		List<Double> fDens = new ArrayList<>();
		List<Double> fSize = new ArrayList<>();
		for (int i = 0; i < nModes; i++)
		{
			if (size[i] >= minSize)
			{
				fModes.add(modes[i]);
				fDens.add(modeDens[i]);
				fSize.add(size[i]);
			}
		}

		double[] finalModes = new double[fModes.size()];
		double[] finalDens = new double[fDens.size()];
		double[] finalSize = new double[fSize.size()];
		for (int i = 0; i < finalModes.length; i++)
		{
			finalModes[i] = fModes.get(i);
			finalDens[i] = fDens.get(i);
			finalSize[i] = fSize.get(i);
		}

		double[] antiModes = null;
		if (finalModes.length > 1)
		{
			antiModes = new double[finalSize.length - 1];
			for (int i = 0; i < antiModes.length; i++)
			{
				int m1 = -1, m2 = -1;
				for (int idx : begin)
				{
					if (y[idx] == finalDens[i])
					{
						m1 = idx;
						break;
					}
				}
				for (int idx : begin)
				{
					if (y[idx] == finalDens[i + 1])
					{
						m2 = idx;
						break;
					}
				}
				if (m1 < 0 || m2 < 0 || m1 > m2)
				{
					antiModes[i] = Double.NaN;
					continue;
				}

				double minVal = Double.POSITIVE_INFINITY;
				for (int k = m1; k <= m2; k++)
					if (y[k] < minVal)
						minVal = y[k];
				List<Integer> lows = new ArrayList<>();
				for (int k = m1; k <= m2; k++)
					if (y[k] == minVal)
						lows.add(k - m1);
				int lowPoint = tieBreakIndex(lows) + m1;

				antiModes[i] = xg[lowPoint];
			}
		}

		return new ModeAntimodeResult(finalModes, finalDens, finalSize, antiModes);
	}
}
