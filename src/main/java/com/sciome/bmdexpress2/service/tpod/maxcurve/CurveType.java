package com.sciome.bmdexpress2.service.tpod.maxcurve;

/**
 * Defines the shape of the curve being analyzed by the Kneedle algorithm.
 *
 * <p>
 * The Kneedle algorithm supports four combinations:
 *
 * <ul>
 * <li>Increasing + Concave</li>
 * <li>Increasing + Convex</li>
 * <li>Decreasing + Concave</li>
 * <li>Decreasing + Convex</li>
 * </ul>
 *
 * The curve type determines how the normalized data is transformed into the
 * difference curve used for knee detection.
 */
public enum CurveType
{

	/**
	 * Monotonically increasing and concave.
	 *
	 * Example:
	 * 
	 * <pre>
	 *      ______
	 *    /
	 *  /
	 * /
	 * </pre>
	 */
	INCREASING_CONCAVE(true, true),

	/**
	 * Monotonically increasing and convex.
	 *
	 * Example:
	 * 
	 * <pre>
	 *         /
	 *       /
	 *     /
	 * ___/
	 * </pre>
	 */
	INCREASING_CONVEX(true, false),

	/**
	 * Monotonically decreasing and concave.
	 *
	 * Example:
	 * 
	 * <pre>
	 * \
	 *  \
	 *   \____
	 * </pre>
	 */
	DECREASING_CONCAVE(false, true),

	/**
	 * Monotonically decreasing and convex.
	 *
	 * Example:
	 * 
	 * <pre>
	 * ______
	 *      \
	 *       \
	 *        \
	 * </pre>
	 */
	DECREASING_CONVEX(false, false);

	private final boolean increasing;
	private final boolean concave;

	CurveType(boolean increasing, boolean concave)
	{
		this.increasing = increasing;
		this.concave = concave;
	}

	/**
	 * Returns true if the curve is increasing.
	 */
	public boolean isIncreasing()
	{
		return increasing;
	}

	/**
	 * Returns true if the curve is decreasing.
	 */
	public boolean isDecreasing()
	{
		return !increasing;
	}

	/**
	 * Returns true if the curve is concave.
	 */
	public boolean isConcave()
	{
		return concave;
	}

	/**
	 * Returns true if the curve is convex.
	 */
	public boolean isConvex()
	{
		return !concave;
	}
}
