package com.candlevision.expert.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.candlevision.expert.R;
import com.candlevision.expert.engine.AdaptiveModel;
import com.candlevision.expert.engine.CandleFeature;
import com.candlevision.expert.engine.ExpertPredictor;
import com.candlevision.expert.engine.Prediction;
import com.candlevision.expert.vision.CandleEstimator;
import com.candlevision.expert.vision.ChartVisionEngine;
import com.candlevision.expert.vision.FrameMetrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends ComponentActivity {
    private static final int CAMERA_PERMISSION = 701;
    private PreviewView previewView;
    private TextView verdict;
    private TextView confidence;
    private TextView context;
    private TextView factors;
    private TextView hint;
    private ExpertPredictor predictor;
    private AdaptiveModel adaptive;
    private final ChartVisionEngine vision = new ChartVisionEngine();
    private final CandleEstimator candleEstimator = new CandleEstimator();
    private ExecutorService analysisExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        verdict = findViewById(R.id.txtVerdict);
        confidence = findViewById(R.id.txtConfidence);
        context = findViewById(R.id.txtContext);
        factors = findViewById(R.id.txtFactors);
        hint = findViewById(R.id.txtHint);
        Button up = findViewById(R.id.btnUp);
        Button down = findViewById(R.id.btnDown);

        adaptive = new AdaptiveModel(getSharedPreferences("candle_adaptive", MODE_PRIVATE));
        predictor = new ExpertPredictor(adaptive);
        analysisExecutor = Executors.newSingleThreadExecutor();

        up.setOnClickListener(v -> adaptive.learn(true));
        down.setOnClickListener(v -> adaptive.learn(false));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(this).get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setImageQueueDepth(1)
                        .build();

                analysis.setAnalyzer(analysisExecutor, image -> {
                    try {
                        FrameMetrics metrics = vision.analyze(image);
                        CandleFeature candle = candleEstimator.estimate(metrics);
                        Prediction p = predictor.update(candle, metrics.chartQuality);
                        runOnUiThread(() -> render(p));
                    } finally {
                        image.close();
                    }
                });

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    verdict.setText("CAMERA ERROR");
                    context.setText(e.getMessage() == null ? "Unable to start camera" : e.getMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void render(Prediction p) {
        String label = p.direction == Prediction.Direction.UP ? "BULLISH BIAS"
                : p.direction == Prediction.Direction.DOWN ? "BEARISH BIAS" : "NO CLEAR EDGE";
        verdict.setText(label);
        confidence.setText(String.format(java.util.Locale.US, "Confidence %.1f%%", p.confidence));
        context.setText(p.context);
        factors.setText(String.format(java.util.Locale.US,
                "Structure %.0f   Momentum %.0f   Reversal %.0f\nQuality %.0f       Sequence %.0f       Samples %d",
                p.structure, p.momentum, p.reversal, p.quality, p.sequence, p.sampleCount));
        hint.setText(p.quality < 55 ? "Improve focus/lighting and fill the frame with the chart" :
                "Visual estimate only — use clear candles and avoid perspective distortion");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            verdict.setText("CAMERA PERMISSION NEEDED");
            context.setText("Allow camera access in Android Settings");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (analysisExecutor != null) analysisExecutor.shutdownNow();
    }
}
