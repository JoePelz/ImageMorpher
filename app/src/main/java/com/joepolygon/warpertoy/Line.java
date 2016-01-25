package com.joepolygon.warpertoy;

import java.io.Serializable;

/**
 * Represents a line, by two points. Can be used as a vector when the first point is 0,0
 * Created by Joe on 2016-01-08.
 */
class Line implements Serializable{
    public static final int P0 = 0;
    public static final int P1 = 1;
    private float[] pts;

    public Line(float startx, float starty, float endx, float endy) {
        init(startx, starty, endx, endy);
    }

    private void init(float x0, float y0, float x1, float y1) {
        //three points. Start, middle, end.
        //format: x0, y0,  x1, y1,  x2, y2
        pts = new float[4];

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

    public float length() {
        return (float)Math.hypot(pts[2] - pts[0], pts[3] - pts[1]);
    }

    public float distanceFromLinePts(float x, float y) {
        float distanceToA = getDistSquared(pts[0], pts[1], x, y);
        float distanceToB = getDistSquared(pts[2], pts[3], x, y);
        return (float) Math.sqrt(Math.min(distanceToA, distanceToB));
    }

    public float distanceFromLine(float x, float y) {
        //get percent along line
        //if percent < 0
        //  return dist to A
        //if percent > 1
        //  return dist to B
        //else
        //  return dist to line
        float dx = pts[2]-pts[0];
        float dy = pts[3]-pts[1];
        float percent = ((dx)*(x-pts[0]) + (dy)*(y-pts[1]))
                / (dx*dx + dy*dy);
        if (percent <= 0) {
            return (float)Math.sqrt(getDistSquared(pts[0], pts[1], x, y));
        } else if (percent >= 1) {
            return (float)Math.sqrt(getDistSquared(pts[2], pts[3], x, y));
        } else {
            x -= pts[0];
            y -= pts[1];
            VectorF XP = new VectorF(dx - x, dy - y);
            VectorF n = new VectorF(-dy, dx);
            return Math.abs(XP.projectionLength(n));
        }
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

    @Override
    public String toString() {
        return String.format("Line: (%.3f,%.3f) to (%.3f,%.3f)", pts[0], pts[1], pts[2], pts[3]);
    }

    public Line copy() {
        return new Line(pts[0], pts[1], pts[2], pts[3]);
    }
}
