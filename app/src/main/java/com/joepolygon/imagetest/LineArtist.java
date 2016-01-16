package com.joepolygon.imagetest;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Created to remove painting code from Line class.
 * Created by Joe on 2016-01-15.
 */
public class LineArtist {
    public static final Paint paintErasable = new Paint();
    public static final Paint paintNice = new Paint();
    public static final Paint paintSelected = new Paint();
    public static final Paint paintThin = new Paint();
    static {
        paintErasable.setStrokeWidth(5);
        paintErasable.setAntiAlias(false);
        paintErasable.setColor(0xFFFFFFFF);
        paintErasable.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        paintNice.setStrokeWidth(3);
        paintNice.setAntiAlias(true);
        paintNice.setColor(0xFF99FFAA);
        paintNice.setStyle(Paint.Style.STROKE);

        paintSelected.setStrokeWidth(6);
        paintSelected.setAntiAlias(true);
        paintSelected.setColor(0xFF0000FF);
        paintSelected.setStyle(Paint.Style.STROKE);

        paintThin.setStrokeWidth(0);
        paintThin.setAntiAlias(true);
        paintThin.setColor(0xFFFFFFFF);
        paintThin.setStyle(Paint.Style.FILL_AND_STROKE);
    }



    public static void draw(Canvas c, Matrix m, Line l, Paint p) {
        if (c == null || m == null) {
            return;
        }
        float[] pts = l.getPts();
        float[] ptsXform = new float[pts.length];
        m.mapPoints(ptsXform, pts);
        //Log.v("Line", "pts (" + pts[0] + "," + pts[1] + ") mapped to (" + ptsXform[0] + "," + ptsXform[1] + ")");
        c.drawLine(ptsXform[0], ptsXform[1], ptsXform[2], ptsXform[3], p);

        for(int i = 0; i < ptsXform.length; i+= 2) {
            c.drawCircle(ptsXform[i], ptsXform[i+1], 10, p);
        }
    }

}
