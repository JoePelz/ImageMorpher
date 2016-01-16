package com.joepolygon.imagetest;

/**
 * Created by Joe on 2016-01-15.
 */
public class PointF {
    float x;
    float y;

    public PointF() {

    }

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF(PointF B) {
        this.x = B.x;
        this.y = B.y;
    }

    @Override
    public String toString() {
        return String.format("[%.3f, %.3f]", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PointF) {
            PointF b = (PointF) o;
            return b.x == x && b.y == y;
        }
        return super.equals(o);
    }
}
