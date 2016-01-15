package com.joepolygon.imagetest;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.io.Serializable;

/**
 * Represents a line, by two points. Can be used as a vector when the first point is 0,0
 * Created by Joe on 2016-01-08.
 */
class Line implements Serializable{
    public static final int P0 = 0;
    public static final int P1 = 1;
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

    private float[] pts;
    private float[] ptsXform;

    public Line(float x, float y) {
        init(0, 0, x, y);
    }

    public Line(float startx, float starty, float endx, float endy) {
        init(startx, starty, endx, endy);
    }

    private void init(float x0, float y0, float x1, float y1) {
        //three points. Start, middle, end.
        //format: x0, y0,  x1, y1,  x2, y2
        pts = new float[4];
        ptsXform = new float[4];

        pts[0] = x0;
        pts[1] = y0;

        pts[2] = x1;
        pts[3] = y1;

    }
    public void setP0(float x, float y) {
        pts[0] = x;
        pts[1] = y;
    }
    public void setP1(float x, float y) {
        pts[2] = x;
        pts[3] = y;
    }

    public float[] getPts() {
        return pts;
    }

    public void normal(VectorF out) {
        out.x = pts[1] - pts[3];
        out.y = pts[2] - pts[0];
    }

    public float length() {
        return (float)Math.hypot(pts[2]-pts[0], pts[3]-pts[1]);
    }

    public float distanceFromLine(float x, float y) {
        float distanceToA = getDistSquared(pts[0], pts[1], x, y);
        float distanceToB = getDistSquared(pts[2], pts[3], x, y);
        return (float) Math.sqrt(Math.min(distanceToA, distanceToB));
    }

    public int getClosestVertex(float x, float y) {
        float dP0 = getDistSquared(x, y, pts[0], pts[1]);
        float dP1 = getDistSquared(x, y, pts[2], pts[3]);
        if (dP0 > dP1) return P1;
        return P0;
    }

    private float getDistSquared(float x0, float y0, float x1, float y1) {
        return (x1-x0)*(x1-x0) + (y1-y0)*(y1-y0);
    }

    public void draw(Canvas c, Matrix m, Paint p) {
        if (c == null || m == null) {
            return;
        }
        m.mapPoints(ptsXform, pts);
        //Log.v("Line", "pts (" + pts[0] + "," + pts[1] + ") mapped to (" + ptsXform[0] + "," + ptsXform[1] + ")");
        c.drawLine(ptsXform[0], ptsXform[1], ptsXform[2], ptsXform[3], p);

        for(int i = 0; i < ptsXform.length; i+= 2) {
            c.drawCircle(ptsXform[i], ptsXform[i+1], 10, p);
        }
    }

    @Override
    public String toString() {
        return String.format("Line: (%.3f,%.3f) to (%.3f,%.3f)", pts[0], pts[1], pts[2], pts[3]);
    }

    public Line copy() {
        return new Line(pts[0], pts[1], pts[2], pts[3]);
    }
}
