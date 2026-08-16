package com.candlevision.expert.engine;

import java.util.ArrayDeque;
import java.util.Locale;

public final class ExpertPredictor {
    private final ArrayDeque<CandleFeature> history = new ArrayDeque<>();
    private final AdaptiveModel adaptive;

    public ExpertPredictor(AdaptiveModel adaptive) {
        this.adaptive = adaptive;
    }

    public synchronized Prediction update(CandleFeature candle, double chartQuality) {
        history.addLast(candle);
        while (history.size() > 12) history.removeFirst();
        if (history.size() < 5) {
            return new Prediction(Prediction.Direction.NEUTRAL, 0, 0, 0, 0,
                    chartQuality, 0, adaptive.sampleCount(), "Need more visible candles");
        }

        CandleFeature[] c = history.toArray(new CandleFeature[0]);
        double recent = 0;
        int start = Math.max(0, c.length - 5);
        for (int i = start; i < c.length; i++) recent += c[i].bullish ? 1 : -1;
        double bodyMomentum = 0;
        for (int i = start; i < c.length; i++) bodyMomentum += (c[i].bullish ? 1 : -1) * c[i].body;

        CandleFeature last = c[c.length - 1];
        double rejectionUp = last.lowerWick - last.upperWick;
        double rejectionDown = last.upperWick - last.lowerWick;

        double momentum = clamp(50 + recent * 8 + bodyMomentum * 20, 0, 100);
        double structure = clamp(50 + recent * 7, 0, 100);
        double reversal = clamp(50 + (rejectionUp > 0 ? 15 : rejectionDown > 0 ? -15 : 0), 0, 100);
        double sequence = clamp(50 + recent * 7, 0, 100);
        double quality = clamp(chartQuality, 0, 100);

        double raw = (momentum - 50) * 0.32 + (structure - 50) * 0.26
                + (reversal - 50) * 0.16 + (sequence - 50) * 0.16
                + (quality - 50) * 0.10 + adaptive.bias() * 12.0;
        Prediction.Direction direction = Math.abs(raw) < 9 ? Prediction.Direction.NEUTRAL
                : raw > 0 ? Prediction.Direction.UP : Prediction.Direction.DOWN;
        double confidence = clamp(50 + Math.abs(raw) * 2.2, 50, 97);

        String context;
        if (direction == Prediction.Direction.UP && reversal > 65) context = "Bullish momentum with lower-wick rejection";
        else if (direction == Prediction.Direction.DOWN && reversal > 65) context = "Bearish momentum with upper-wick rejection";
        else if (direction == Prediction.Direction.NEUTRAL) context = "Mixed candle evidence";
        else context = String.format(Locale.US, "%s sequence with improving directional pressure",
                direction == Prediction.Direction.UP ? "Bullish" : "Bearish");

        return new Prediction(direction, confidence, structure, momentum, reversal,
                quality, sequence, adaptive.sampleCount(), context);
    }

    private static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }
}
