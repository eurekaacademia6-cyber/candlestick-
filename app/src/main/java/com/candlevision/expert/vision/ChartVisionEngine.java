package com.candlevision.expert.vision;

import androidx.camera.core.ImageProxy;
import java.nio.ByteBuffer;

public final class ChartVisionEngine {
    public FrameMetrics analyze(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        if (width <= 0 || height <= 0 || buffer.remaining() == 0) {
            return new FrameMetrics(0, 0, 0, 0, 0);
        }

        int stepX = Math.max(4, width / 96);
        int stepY = Math.max(4, height / 72);
        long sum = 0;
        long sumSq = 0;
        long low = 0;
        long high = 0;
        long samples = 0;
        long leftEnergy = 0;
        long rightEnergy = 0;

        for (int y = height / 5; y < height * 4 / 5; y += stepY) {
            int rowBase = y * rowStride;
            for (int x = width / 10; x < width * 9 / 10; x += stepX) {
                int idx = rowBase + x * pixelStride;
                if (idx < 0 || idx >= buffer.limit()) continue;
                int v = buffer.get(idx) & 0xFF;
                sum += v;
                sumSq += (long) v * v;
                if (v < 70) low++;
                if (v > 185) high++;
                long edge = 0;
                if (x < width / 2) leftEnergy += v; else rightEnergy += v;
                samples++;
            }
        }

        if (samples < 50) return new FrameMetrics(0, 0, 0, 0, 0);
        double mean = (double) sum / samples;
        double variance = Math.max(0, (double) sumSq / samples - mean * mean);
        double std = Math.sqrt(variance);
        double contrast = clamp(std / 64.0 * 100.0, 0, 100);
        double darkRatio = (double) low / samples;
        double brightRatio = (double) high / samples;
        double quality = clamp(35 + contrast * 0.55 + Math.min(15, (darkRatio + brightRatio) * 25), 0, 100);
        double directionalBias = clamp((brightRatio - darkRatio) * 120, -50, 50);
        double lower = clamp((darkRatio - brightRatio) * 80 + 50, 0, 100);
        double upper = clamp((brightRatio - darkRatio) * 80 + 50, 0, 100);
        return new FrameMetrics(quality, contrast, directionalBias, lower, upper);
    }

    private static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }
}
