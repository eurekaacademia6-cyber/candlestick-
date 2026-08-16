package com.candlevision.expert.vision;

import com.candlevision.expert.engine.CandleFeature;

public final class CandleEstimator {
    public CandleFeature estimate(FrameMetrics metrics) {
        double body = Math.max(0.1, metrics.contrast / 100.0);
        boolean bullish = metrics.directionalBias >= 0;
        double upper = metrics.upperShadowBias / 100.0;
        double lower = metrics.lowerShadowBias / 100.0;
        return new CandleFeature(body, upper, lower, bullish);
    }
}
