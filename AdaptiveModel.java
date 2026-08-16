package com.candlevision.expert;

import android.content.Context;
import java.util.Locale;

public final class AdaptiveModel {
    private static final String PREFS="adaptive_model";
    private final Context ctx;
    private final double[] w;
    private double bias;
    private int examples;
    public AdaptiveModel(Context c, int n){ctx=c; w=new double[n]; load();}
    private void load(){
        var p=ctx.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        bias=p.getFloat("bias",0f); examples=p.getInt("examples",0);
        for(int i=0;i<w.length;i++) w[i]=p.getFloat("w"+i,0f);
    }
    private void save(){
        var e=ctx.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putFloat("bias",(float)bias).putInt("examples",examples);
        for(int i=0;i<w.length;i++)e.putFloat("w"+i,(float)w[i]); e.apply();
    }
    public double predict(FeatureVector f){ double z=bias; for(int i=0;i<w.length;i++)z+=w[i]*f.x[i]; return 1.0/(1.0+Math.exp(-Math.max(-30,Math.min(30,z)))); }
    public void learn(FeatureVector f, int label){
        double p=predict(f), err=label-p, lr=0.08/Math.sqrt(1+examples/50.0);
        bias+=lr*err; for(int i=0;i<w.length;i++) w[i]+=lr*err*f.x[i]; examples++; save();
    }
    public int examples(){return examples;}
    public String summary(){return String.format(Locale.US,"Adaptive samples: %d",examples);}
}
