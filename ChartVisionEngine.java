package com.candlevision.expert;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Camera-only candle reconstruction. It deliberately avoids any network/broker data. */
public final class ChartVisionEngine {
    public static final class Result {
        public final List<Candle> candles; public final float quality; public final String note;
        public Result(List<Candle> c,float q,String n){candles=c;quality=q;note=n;}
    }
    public Result analyze(Bitmap src){
        if(src==null) return new Result(Collections.emptyList(),0,"No frame");
        int w=src.getWidth(), h=src.getHeight();
        int left=(int)(w*.07), right=(int)(w*.93), top=(int)(h*.15), bottom=(int)(h*.86);
        int span=Math.max(1,right-left), step=Math.max(3,span/90);
        List<Candle> out=new ArrayList<>();
        for(int x=left+step/2;x<right;x+=step){
            int min=h, max=-1, brightMin=h, brightMax=-1; double sat=0; int n=0;
            float mean=0;
            for(int dx=-Math.max(1,step/3);dx<=Math.max(1,step/3);dx++){
                int px=Math.max(0,Math.min(w-1,x+dx));
                for(int y=top;y<bottom;y+=2){
                    int c=src.getPixel(px,y); float r=(c>>16)&255,g=(c>>8)&255,b=c&255;
                    float lum=(r+g+b)/3f; float hi=Math.max(r,Math.max(g,b)), lo=Math.min(r,Math.min(g,b));
                    if(hi-lo>55){ min=Math.min(min,y); max=Math.max(max,y); sat+=hi-lo; }
                    mean+=lum; n++;
                }
            }
            if(max>min && (max-min)>8 && sat/n>12){
                float hh=(max+min)/2f; float body=Math.max(3,(max-min)*.35f); float open=hh+body/2, close=hh-body/2;
                // color heuristic: green-ish pixels => bullish, red-ish => bearish
                int green=0, red=0;
                for(int y=min;y<=max;y+=2){int c=src.getPixel(Math.max(0,Math.min(w-1,x)),y);int r=(c>>16)&255,g=(c>>8)&255,b=c&255;if(g>r*1.15&&g>b*1.05)green++;if(r>g*1.15&&r>b*1.05)red++;}
                if(green>red) close=open-body; else if(red>green) close=open+body;
                out.add(new Candle(x,open,max,min,close));
            }
        }
        if(out.size()>60) out=new ArrayList<>(out.subList(out.size()-60,out.size()));
        float q=Math.min(1f,out.size()/28f);
        return new Result(out,q,out.size()+" candidate candles detected");
    }
}
