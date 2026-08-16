package com.candlevision.expert;

public final class Candle {
    public final float x, open, high, low, close;
    public Candle(float x, float open, float high, float low, float close) { this.x=x; this.open=open; this.high=high; this.low=low; this.close=close; }
    public float body(){ return Math.abs(close-open); }
    public float range(){ return Math.max(1e-4f, high-low); }
    public boolean bull(){ return close>open; }
    public float upper(){ return high-Math.max(open,close); }
    public float lower(){ return Math.min(open,close)-low; }
}
