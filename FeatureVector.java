package com.candlevision.expert;

public final class FeatureVector {
    public final double[] x;
    public FeatureVector(double[] x){ this.x=x; }
    public int size(){ return x.length; }
}
