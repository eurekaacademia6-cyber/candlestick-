package com.candlevision.expert.engine;

import android.content.SharedPreferences;
import java.util.Locale;

public final class AdaptiveModel {
    private static final String KEY_UP = "up_weight";
    private static final String KEY_DOWN = "down_weight";
    private static final String KEY_N = "samples";
    private final SharedPreferences prefs;

    public AdaptiveModel(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public synchronized void learn(boolean up) {
        double upW = prefs.getFloat(KEY_UP, 1.0f);
        double downW = prefs.getFloat(KEY_DOWN, 1.0f);
        int n = prefs.getInt(KEY_N, 0) + 1;
        if (up) upW += 0.05; else downW += 0.05;
        prefs.edit()
                .putFloat(KEY_UP, (float) upW)
                .putFloat(KEY_DOWN, (float) downW)
                .putInt(KEY_N, n)
                .apply();
    }

    public double bias() {
        double upW = prefs.getFloat(KEY_UP, 1.0f);
        double downW = prefs.getFloat(KEY_DOWN, 1.0f);
        return (upW - downW) / Math.max(1.0, upW + downW);
    }

    public int sampleCount() {
        return prefs.getInt(KEY_N, 0);
    }

    public String summary() {
        return String.format(Locale.US, "bias %.2f", bias());
    }
}
