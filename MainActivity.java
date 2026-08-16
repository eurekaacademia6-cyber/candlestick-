package com.candlevision.expert;

import android.Manifest;import android.app.*;import android.os.*;import android.content.*;import android.content.pm.PackageManager;import android.graphics.*;import android.view.*;import android.widget.*;import androidx.activity.ComponentActivity;import androidx.core.content.ContextCompat;import androidx.camera.core.*;import androidx.camera.lifecycle.ProcessCameraProvider;import androidx.camera.view.PreviewView;import com.google.android.gms.tasks.*;import com.google.mlkit.vision.text.*;import com.google.mlkit.vision.common.InputImage;import java.util.concurrent.*;import java.util.*;

public class MainActivity extends ComponentActivity {
    PreviewView preview; TextView status, signal, details, learning; ChartVisionEngine vision; ExpertPredictor expert; AdaptiveModel model; ExecutorService exec; volatile long lastRun=0; ExpertPredictor.Prediction lastPrediction; Button learnUp,learnDown,learnFlat;
    int bg=Color.rgb(7,19,27), panel=Color.rgb(14,34,48), cyan=Color.rgb(57,217,255), gold=Color.rgb(255,196,0), white=Color.WHITE, muted=Color.rgb(142,165,181), green=Color.rgb(50,226,141), red=Color.rgb(255,92,112);
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg); vision=new ChartVisionEngine(); model=new AdaptiveModel(this,9); expert=new ExpertPredictor(model);exec=Executors.newFixedThreadPool(2);buildUi();if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.CAMERA},10);else startCamera();}
    TextView tv(String s,int sp){TextView t=new TextView(this);t.setText(s);t.setTextColor(white);t.setTextSize(sp);t.setPadding(12,6,12,6);return t;}
    void buildUi(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(bg);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL); TextView title=tv("CANDLEVISION EXPERT",20);title.setTypeface(null,1); head.addView(title,new LinearLayout.LayoutParams(0,64,1)); status=tv("CAMERA OFFLINE",14);status.setTextColor(gold);head.addView(status,new LinearLayout.LayoutParams(-2,64));root.addView(head);
        FrameLayout frame=new FrameLayout(this); preview=new PreviewView(this); frame.addView(preview,new FrameLayout.LayoutParams(-1,-1)); ChartOverlay overlay=new ChartOverlay(this);frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));root.addView(frame,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(16,12,16,12);card.setBackgroundColor(panel);signal=tv("ALIGN THE CHART",28);signal.setTextColor(gold);signal.setGravity(Gravity.CENTER);card.addView(signal);details=tv("Point the camera at a clean candlestick chart inside the frame.\nNo bridge • no internet • no broker connection.",14);details.setTextColor(muted);details.setGravity(Gravity.CENTER);card.addView(details);learning=tv(model.summary(),12);learning.setTextColor(cyan);learning.setGravity(Gravity.CENTER);card.addView(learning);root.addView(card,new LinearLayout.LayoutParams(-1,190));
        LinearLayout buttons=new LinearLayout(this);buttons.setPadding(8,8,8,8);learnUp=btn("LABEL UP",green);learnDown=btn("LABEL DOWN",red);learnFlat=btn("LABEL FLAT",muted);buttons.addView(learnUp,new LinearLayout.LayoutParams(0,60,1));buttons.addView(learnDown,new LinearLayout.LayoutParams(0,60,1));buttons.addView(learnFlat,new LinearLayout.LayoutParams(0,60,1));root.addView(buttons);setContentView(root);
        learnUp.setOnClickListener(v->label(1));learnDown.setOnClickListener(v->label(0));learnFlat.setOnClickListener(v->labelFlat());
    }
    Button btn(String s,int c){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setTextColor(Color.BLACK);b.setBackgroundColor(c);return b;}
    void label(int y){if(lastPrediction!=null){model.learn(lastPrediction.features,y);learning.setText(model.summary());Toast.makeText(this,"Learned outcome",Toast.LENGTH_SHORT).show();}}
    void labelFlat(){if(lastPrediction!=null){Toast.makeText(this,"Flat labels are stored as a neutral review event (not used as direction yet).",Toast.LENGTH_SHORT).show();}}
    void startCamera(){status.setText("CAMERA LIVE");status.setTextColor(green);var future=ProcessCameraProvider.getInstance(this);future.addListener(()->{try{var provider=future.get();Preview p=new Preview.Builder().build();p.setSurfaceProvider(preview.getSurfaceProvider());ImageAnalysis a=new ImageAnalysis.Builder().setTargetResolution(new android.util.Size(960,540)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build();a.setAnalyzer(exec,ip->{long now=System.currentTimeMillis();if(now-lastRun<900){ip.close();return;}lastRun=now;Bitmap bm=toBitmap(ip);ip.close();exec.execute(()->analyzeFrame(bm));});provider.unbindAll();provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,p,a);}catch(Exception e){runOnUiThread(()->status.setText("CAMERA ERROR"));}},ContextCompat.getMainExecutor(this));}
    Bitmap toBitmap(ImageProxy ip){
        int w=ip.getWidth(),h=ip.getHeight();
        ImageProxy.PlaneProxy plane=ip.getPlanes()[0];
        java.nio.ByteBuffer src=plane.getBuffer();
        int pixelStride=plane.getPixelStride(), rowStride=plane.getRowStride();
        Bitmap bm=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        int rowBytes=w*4; byte[] row=new byte[rowBytes];
        for(int y=0;y<h;y++){
            int pos=y*rowStride; src.position(Math.min(pos,src.limit()));
            int count=Math.min(rowBytes,src.remaining()); src.get(row,0,count);
            bm.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(row));
        }
        return bm;
    }
    void analyzeFrame(Bitmap bm){var r=vision.analyze(bm);var p=expert.predict(r.candles);lastPrediction=p;runOnUiThread(()->{if(p.direction.startsWith("BULL")){signal.setText("BULLISH BIAS");signal.setTextColor(green);}else if(p.direction.startsWith("BEAR")){signal.setText("BEARISH BIAS");signal.setTextColor(red);}else{signal.setText("NO CLEAR EDGE");signal.setTextColor(gold);}details.setText(String.format(Locale.US,"Chart quality %.0f%% • %d candles\nRegime: %s\nConfidence: %.1f%%\n%s",100*r.quality,r.candles.size(),p.regime,p.confidence,p.reason));learning.setText(model.summary()+" • camera-only adaptive reader");});}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==10&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startCamera();else status.setText("CAMERA PERMISSION REQUIRED");}
    class ChartOverlay extends View{Paint paint=new Paint(1);public ChartOverlay(Context c){super(c);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(5);paint.setColor(cyan);}protected void onDraw(Canvas c){super.onDraw(c);float l=getWidth()*.06f,t=getHeight()*.16f,r=getWidth()*.94f,b=getHeight()*.78f;c.drawRoundRect(l,t,r,b,24,24,paint);paint.setStyle(Paint.Style.FILL);paint.setTextSize(30);paint.setColor(white);c.drawText("ALIGN CHART HERE",l+24,t+38,paint);paint.setStyle(Paint.Style.STROKE);paint.setColor(cyan);}}
}
