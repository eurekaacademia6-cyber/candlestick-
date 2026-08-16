package com.candlevision.expert.engine;

public final class CandleFeature {
    public final double body;
    public final double upperWick;
    public final double lowerWick;
    public final boolean bullish;

    public CandleFeature(double body, double upperWick, double lowerWick, boolean bullish) {
        this.body = body;
        this.upperWick = upperWick;
        this.lowerWick = lowerWick;
        this.bullish = bullish;
    }
}
