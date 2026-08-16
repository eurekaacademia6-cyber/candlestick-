package com.candlevision.expert;

import java.util.*;

public final class ExpertPredictor {
    public static final class Prediction {
        public String direction, regime, reason; public double confidence, bullish, bearish, quality, pUp;
        public FeatureVector features;
    }
    private final AdaptiveModel model;
    public ExpertPredictor(AdaptiveModel m){model=m;}
    public Prediction predict(List<Candle> c){
        Prediction p=new Prediction();
        if(c==null||c.size()<8){p.direction="ALIGN CHART";p.regime="INSUFFICIENT";p.confidence=0;return p;}
        int n=c.size(); double bull=0,bear=0;
        Candle a=c.get(n-1), b=c.get(n-2), c3=c.get(n-3);
        double avgRange=0; for(int i=Math.max(0,n-14);i<n;i++)avgRange+=c.get(i).range(); avgRange/=Math.min(14,n);
        double bodyRatio=a.body()/a.range();
        double upper=a.upper()/a.range(), lower=a.lower()/a.range();
        if(a.bull()) bull+=1.0; else bear+=1.0;
        if(a.bull()&&b.bull()&&a.close>b.close) bull+=.8; if(!a.bull()&& !b.bull()&&a.close<b.close)bear+=.8;
        if(a.body()>avgRange*0.6 && a.bull()) bull+=.9; if(a.body()>avgRange*0.6&&!a.bull())bear+=.9;
        if(lower>.5 && bodyRatio<.35) bull+=1.2; if(upper>.5&&bodyRatio<.35)bear+=1.2;
        if(a.bull()&&a.open<=b.close&&a.close>=b.open)bull+=1.5;
        if(!a.bull()&&a.open>=b.close&&a.close<=b.open)bear+=1.5;
        boolean hh=true,ll=true; double last=a.close;
        for(int i=Math.max(1,n-6);i<n;i++){ if(c.get(i).close<=c.get(i-1).close)hh=false; if(c.get(i).close>=c.get(i-1).close)ll=false; }
        if(hh)bull+=1.3; if(ll)bear+=1.3;
        double momentum=(a.close-c3.close)/Math.max(1e-4,avgRange);
        if(momentum>.8)bull+=1.0; if(momentum<-.8)bear+=1.0;
        double compression=avgRange/Math.max(1e-4,a.range());
        if(compression>2.2){ // possible expansion setup; require direction agreement
            if(a.bull())bull+=.7;else bear+=.7;
        }
        double total=Math.max(1,bull+bear); double prior=0.5+0.18*Math.tanh((bull-bear)/2.5);
        FeatureVector f=new FeatureVector(new double[]{1,a.bull()?1:-1,bodyRatio,upper,lower,momentum,hh?1:0,ll?-1:0,compression>2.2?1:0});
        double ml=model.predict(f); double combined=0.72*prior+0.28*ml; if(Math.abs(combined-.5)<.055){p.direction="NO CLEAR EDGE";} else p.direction=combined>.5?"BULLISH BIAS":"BEARISH BIAS";
        p.pUp=combined; p.bullish=bull; p.bearish=bear; p.confidence=50+Math.abs(combined-.5)*100; p.quality=Math.min(100,100*Math.abs(bull-bear)/6.0); p.features=f;
        if(combined>.5) p.regime=hh?"UPTREND / MOMENTUM":"REVERSAL / TEST"; else p.regime=ll?"DOWNTREND / MOMENTUM":"REVERSAL / TEST";
        List<String> reasons=new ArrayList<>(); if(a.bull())reasons.add("latest candle bullish"); else reasons.add("latest candle bearish"); if(lower>.5)reasons.add("long lower wick"); if(upper>.5)reasons.add("long upper wick"); if(hh)reasons.add("rising closes"); if(ll)reasons.add("falling closes"); if(a.bull()&&a.open<=b.close&&a.close>=b.open)reasons.add("bullish engulfing-like body"); if(!a.bull()&&a.open>=b.close&&a.close<=b.open)reasons.add("bearish engulfing-like body");
        p.reason=String.join(" • ",reasons);
        return p;
    }
}
