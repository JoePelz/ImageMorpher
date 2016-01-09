package com.joepolygon.imagetest;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

/**
 * Created by Joe on 2016-01-08.
 */
class Line {
    public static final int P0 = 0;
    public static final int P1 = 1;
    private static final Paint paintErasable = new Paint();
    private static final Paint paintNice = new Paint();
    private static final Paint paintSelected = new Paint();
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
    }

    private float[] pts;
    private float[] ptsXform;

    public Line(PointF start, PointF end) {
        init(start.x, start.y, end.x, end.y);
    }

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

    public void setP0(PointF newA) {
        pts[0] = newA.x;
        pts[1] = newA.y;
    }
    public void setP0(float x, float y) {
        pts[0] = x;
        pts[1] = y;
    }
    public void setP1(PointF newB) {
        pts[2] = newB.x;
        pts[3] = newB.y;
    }
    public void setP1(float x, float y) {
        pts[2] = x;
        pts[3] = y;
    }
    public PointF getP0() {
        return new PointF(pts[0], pts[1]);
    }
    public PointF getP1() {
        return new PointF(pts[2], pts[3]);
    }
    public float[] getPts() {
        return pts;
    }

    public void normal(Line out) {
        out.setP0(0, 0);
        // (-dy, dx)
        out.setP1(pts[1] - pts[3], pts[2] - pts[0]);
    }

    public float dotProduct(Line op) {
        float[] temp = op.getPts();
        return pts[2] * temp[2] + pts[3] * temp[3];
    }

    public float length() {
        return (float)Math.hypot(pts[2]-pts[0], pts[3]-pts[1]);
    }

    public float distanceFromLine(float x, float y) {
        Line hypotenuse = new Line(pts[0] - x, pts[1] - y);
        Line n = new Line(0, 0);
        normal(n);
        //Log.v("Line", "distance from " + this + " to (" + x + ", " + y + ") is " + result);
        return hypotenuse.dotProduct(n) / n.length();
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

        for(int i = 0; i < ptsXform.length; i+= 2) {
            c.drawCircle(ptsXform[i], ptsXform[i+1], 10, p);
        }
    }

    @Override
    public String toString() {
        return String.format("Line: (%.3f,%.3f) to (%.3f,%.3f)", pts[0], pts[1], pts[2], pts[3]);
    }
}
