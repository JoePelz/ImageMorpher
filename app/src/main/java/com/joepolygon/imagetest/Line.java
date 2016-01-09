package com.joepolygon.imagetest;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

/**
 * Created by Joe on 2016-01-08.
 */
public class Line {
    private static final Paint paintErasable = new Paint();
    private static final Paint paintNice = new Paint();
    private static final Paint paintSelected = new Paint();
    {
        paintErasable.setStrokeWidth(5);
        paintErasable.setAntiAlias(false);
        paintErasable.setColor(0xFFFFFFFF);
        paintErasable.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        paintNice.setStrokeWidth(3);
        paintNice.setAntiAlias(true);
        paintNice.setColor(0xFF00FFFF);

        paintSelected.setStrokeWidth(6);
        paintSelected.setAntiAlias(true);
        paintSelected.setColor(0xFF0000FF);
    }

    private float[] pts;
    private float[] ptsXform;

    public Line(PointF start, PointF end) {
        init(start.x, start.y, end.x, end.y);
    }

    public Line(float startx, float starty, float endx, float endy) {
        init(startx, starty, endx, endy);
    }

    private void init(float x0, float y0, float x1, float y1) {
        //three points. Start, middle, end.
        //format: x0, y0,  x1, y1,  x2, y2
        pts = new float[6];
        ptsXform = new float[6];

        pts[0] = x0;
        pts[1] = y0;

        // 2nd point starts in the middle of the first two.
        pts[2] = (x0 + x1) / 2;
        pts[3] = (y0 + y1) / 2;

        pts[4] = x1;
        pts[5] = y1;

    }

    public void setA(PointF newA) {
        pts[0] = newA.x;
        pts[1] = newA.y;
    }
    public void setB(PointF newB) {
        pts[2] = newB.x;
        pts[3] = newB.y;
    }
    public void setC(PointF newC) {
        pts[4] = newC.x;
        pts[5] = newC.y;
    }
    public PointF getA() {
        return new PointF(pts[0], pts[1]);
    }
    public PointF getB() {
        return new PointF(pts[2], pts[3]);
    }
    public PointF getC() {
        return new PointF(pts[4], pts[5]);
    }


    //TODO: make draw modes to be options when invoking draw(...);
    public void drawErasable(Canvas c, Matrix m) {
        draw(c, m, paintErasable);
    }

    public void drawNice(Canvas c, Matrix m) {
        draw(c, m, paintNice);
    }

    public void drawSelected(Canvas c, Matrix m) {
        draw(c, m, paintSelected);
    }

    private void draw(Canvas c, Matrix m, Paint p) {
        if (c == null || m == null) {
            return;
        }
        m.mapPoints(ptsXform, pts);
        //Log.v("Line", "pts (" + pts[0] + "," + pts[1] + ") mapped to (" + ptsXform[0] + "," + ptsXform[1] + ")");
        c.drawLine(ptsXform[0], ptsXform[1], ptsXform[2], ptsXform[3], p);
        c.drawLine(ptsXform[2], ptsXform[3], ptsXform[4], ptsXform[5], p);

        for(int i = 0; i < 6; i+= 2) {
            c.drawCircle(ptsXform[i], ptsXform[i+1], 20, p);
        }
    }
}
