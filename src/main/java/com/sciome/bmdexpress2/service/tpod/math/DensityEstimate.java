package com.sciome.bmdexpress2.service.tpod.math;

/**
 * Holds the (x, y) grid of a kernel density estimate — the Java analogue of
 * what R's density() returns (only the parts this port actually uses).
 */
public final class DensityEstimate {
    public final double[] x;
    public final double[] y;

    public DensityEstimate(double[] x, double[] y) {
        this.x = x;
        this.y = y;
    }
}
