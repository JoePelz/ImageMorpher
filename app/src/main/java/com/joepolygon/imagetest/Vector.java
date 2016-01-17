package com.joepolygon.imagetest;

/**
 * 2D Vector represented by two integers. Includes basic manipulation methods.
 * Created by Joe on 2016-01-13.
 */
public class Vector {
    public int x;
    public int y;

    public Vector() {
    }

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector v) {
        x = v.x;
        y = v.y;
    }

    public int lengthSquared() {
        return x*x+y*y;
    }

    public float length() {
        return (float)Math.hypot(x, y);
    }

    public int dotProduct(Vector b) {
        return this.x * b.x + this.y * b.y;
    }
    public float dotProduct(VectorF b) {
        return this.x * b.x + this.y * b.y;
    }

    /**
     * @return A new vector perpendicular to the original.
     */
    public Vector normal() {
        return new Vector(-y, x);
    }

    public void scale(int s) {
        x *= s;
        y *= s;
    }

    public void scale(double s) {
        x = (int)(s*x);
        y = (int)(s*y);
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

    public float projectionLength(Vector ontoThis) {
        return dotProduct(ontoThis) / ontoThis.length();
    }

    public float projectionLength(VectorF ontoThis) {
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
        Vector n = normal();
        return XP.projectionLength(n);
    }

    @Override
    public boolean equals(Object b) {
        Vector v;
        if (b instanceof Vector) {
            v = (Vector) b;
            return this.x == v.x && this.y == v.y;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("<%.3d, %.3d>", x, y);
    }
}
