package com.candlevision.expert.engine;

public final class Prediction {
    public enum Direction { UP, DOWN, NEUTRAL }

    public final Direction direction;
    public final double confidence;
    public final double structure;
    public final double momentum;
    public final double reversal;
    public final double quality;
    public final double sequence;
    public final int sampleCount;
    public final String context;

    public Prediction(Direction direction, double confidence, double structure, double momentum,
                      double reversal, double quality, double sequence, int sampleCount, String context) {
        this.direction = direction;
        this.confidence = confidence;
        this.structure = structure;
        this.momentum = momentum;
        this.reversal = reversal;
        this.quality = quality;
        this.sequence = sequence;
        this.sampleCount = sampleCount;
        this.context = context;
    }
}
