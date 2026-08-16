package com.candlevision.expert.vision;

public final class FrameMetrics {
    public final double chartQuality;
    public final double contrast;
    public final double directionalBias;
    public final double lowerShadowBias;
    public final double upperShadowBias;

    public FrameMetrics(double chartQuality, double contrast, double directionalBias,
                        double lowerShadowBias, double upperShadowBias) {
        this.chartQuality = chartQuality;
        this.contrast = contrast;
        this.directionalBias = directionalBias;
        this.lowerShadowBias = lowerShadowBias;
        this.upperShadowBias = upperShadowBias;
    }
}
