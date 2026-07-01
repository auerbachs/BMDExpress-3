package com.sciome.bmdexpress2.service.tpod;

import java.util.Arrays;

/** Return type of ModeDetector.modes2(...), mirroring R's Modes2() list(modes, mode.dens, size). */
public final class ModeResult
{
	public final double[] modes;
	public final double[] modeDens;
	public final double[] size;

	public ModeResult(double[] modes, double[] modeDens, double[] size)
	{
		this.modes = modes;
		this.modeDens = modeDens;
		this.size = size;
	}

	@Override
	public String toString()
	{
		return "ModeResult{modes=" + Arrays.toString(modes) + ", modeDens=" + Arrays.toString(modeDens)
				+ ", size=" + Arrays.toString(size) + "}";
	}
}
