package com.joepolygon.warpertoy;

/**
 * Represents a 2D point as two integers.  x and y are publically accessible values.
 * Created by Joe on 2016-01-15.
 */
public class Point {
    int x;
    int y;

    public Point() {

    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point B) {
        this.x = B.x;
        this.y = B.y;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            Point b = (Point) o;
            return b.x == x && b.y == y;
        }
        return super.equals(o);
    }
}
