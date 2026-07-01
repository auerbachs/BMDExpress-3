package com.sciome.bmdexpress2.service.tpod.firstmode;

import java.util.Arrays;

/**
 * Return type of ModeDetector.modeAntimode(...), mirroring R's
 * mode.antimode() list(modes, mode.dens, size, anti.modes).
 * antiModes is null when there's only one mode (matching R's NULL).
 */
public final class ModeAntimodeResult
{
	public final double[] modes;
	public final double[] modeDens;
	public final double[] size;
	public final double[] antiModes;

	public ModeAntimodeResult(double[] modes, double[] modeDens, double[] size, double[] antiModes)
	{
		this.modes = modes;
		this.modeDens = modeDens;
		this.size = size;
		this.antiModes = antiModes;
	}

	@Override
	public String toString()
	{
		return "ModeAntimodeResult{modes=" + Arrays.toString(modes) + ", modeDens="
				+ Arrays.toString(modeDens) + ", size=" + Arrays.toString(size) + ", antiModes="
				+ (antiModes == null ? "null" : Arrays.toString(antiModes)) + "}";
	}
}
