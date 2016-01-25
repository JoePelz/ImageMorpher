package com.joepolygon.warpertoy;

/**
 * 2D Vector represented by two floats. Includes basic manipulation methods.
 * Created by Joe on 2016-01-13.
 */
public class VectorF {
    public float x;
    public float y;

    public VectorF() {
    }

    public VectorF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public VectorF(Line l) {
        float[] pts = l.getPts();
        this.x = pts[2] - pts[0];
        this.y = pts[3] - pts[1];
    }

    public VectorF(Vector v) {
        x = v.x;
        y = v.y;
    }
    public VectorF(VectorF v) {
        x = v.x;
        y = v.y;
    }

    public float lengthSquared() {
        return x*x+y*y;
    }

    public float length() {
        return (float)Math.hypot(x, y);
    }

    public float dotProduct(Vector b) {
        return this.x * b.x + this.y * b.y;
    }
    public float dotProduct(VectorF b) {
        return this.x * b.x + this.y * b.y;
    }

    public VectorF normal() {
        return new VectorF(-y, x);
    }

    public void scale(float s) {
        x *= s;
        y *= s;
    }
    public void scale(float sx, float sy) {
        x *= sx;
        y *= sy;
    }

    /**
     *
     * @param ontoThis The vector to project onto.
     * @return A new vector, the result of the projection.
     */
    public VectorF projection(VectorF ontoThis) {
        VectorF result = new VectorF(ontoThis);
        result.scale(dotProduct(ontoThis));
        result.scale(1.0f / ontoThis.lengthSquared());
        return result;
    }

    /**
     *
     * @param ontoThis The vector to project onto.
     * @return A new vector, the result of the projection.
     */
    public VectorF projection(Vector ontoThis) {
        VectorF result = new VectorF(ontoThis);
        result.scale(dotProduct(ontoThis));
        result.scale(1.0f / ontoThis.lengthSquared());
        return result;
    }

    public float projectionLength(VectorF ontoThis) {
        return dotProduct(ontoThis) / ontoThis.length();
    }

    public float projectionLength(Vector ontoThis) {
        return dotProduct(ontoThis) / ontoThis.length();
    }

    /**
     * Returns distance from the point to the line described by this vector.
     * Positive or negative return values indicate right and left respectively,
     * when facing in the direction of the vector.
     * @param x
     * @param y
     * @return
     */
    public float distToPoint(float x, float y) {
        VectorF XP = new VectorF(this.x - x, this.y - y);
        VectorF n = normal();
        return XP.projectionLength(n);
    }

    public float distToPoint(float x, float y, float originX, float originY) {
        x -= originX;
        y -= originY;
        VectorF XP = new VectorF(this.x - x, this.y - y);
        VectorF n = normal();
        return XP.projectionLength(n);
    }

    public float distanceAlong(float x, float y, float originX, float originY) {
        //(ProjPQ PX) / |PQ|
        //this is PQ
        VectorF PX = new VectorF(x - originX, y - originY);
        return dotProduct(PX) / lengthSquared();
    }

    @Override
    public boolean equals(Object b) {
        VectorF v;
        if (b instanceof VectorF) {
            v = (VectorF) b;
            return this.x == v.x && this.y == v.y;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("<%.3f, %.3f>", x, y);
    }
}
