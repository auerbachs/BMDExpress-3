package com.sciome.bmdexpress2.service.tpod.maxcurve;

import java.util.Objects;

/**
 * Represents a knee point detected by the Kneedle algorithm.
 */
public final class KneePoint
{

	/** Index in the original input arrays. */
	private final int index;

	/** Original x coordinate. */
	private final double x;

	/** Original y coordinate. */
	private final double y;

	/** Normalized x coordinate. */
	private final double normalizedX;

	/** Normalized y coordinate. */
	private final double normalizedY;

	/** Difference curve value used by Kneedle. */
	private final double difference;

	public KneePoint(int index, double x, double y, double normalizedX, double normalizedY, double difference)
	{

		this.index = index;
		this.x = x;
		this.y = y;
		this.normalizedX = normalizedX;
		this.normalizedY = normalizedY;
		this.difference = difference;
	}

	public int getIndex()
	{
		return index;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getNormalizedX()
	{
		return normalizedX;
	}

	public double getNormalizedY()
	{
		return normalizedY;
	}

	/**
	 * Returns the transformed difference value used by the algorithm.
	 */
	public double getDifference()
	{
		return difference;
	}

	@Override
	public String toString()
	{
		return "KneePoint{" + "index=" + index + ", x=" + x + ", y=" + y + ", normalizedX=" + normalizedX
				+ ", normalizedY=" + normalizedY + ", difference=" + difference + '}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof KneePoint))
			return false;

		KneePoint other = (KneePoint) o;

		return index == other.index && Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0
				&& Double.compare(normalizedX, other.normalizedX) == 0
				&& Double.compare(normalizedY, other.normalizedY) == 0
				&& Double.compare(difference, other.difference) == 0;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(index, x, y, normalizedX, normalizedY, difference);
	}
}