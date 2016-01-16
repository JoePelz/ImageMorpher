package com.joepolygon.imagetest;

import android.graphics.Bitmap;


/**
 * Created by Joe on 2016-01-13.
 */
public class Engine {
    private Bitmap imgA;
    private Bitmap imgB;
    private String project;
    private int frames;
    private int width;
    private int height;
    private float a;
    private float b;
    private float P;

    /**
     * Create a new rendering engine.
     * @param project The project name to work within
     * @param frames The number of frames to render
     * @param a
     * @param b
     * @param P
     */
    public Engine(String project, int frames, float a, float b, float P, int width, int height) {
        this.project = project;
        this.frames = frames;
        this.a = a;
        this.b = b;
        this.P = P;
        this.width = width;
        this.height = height;
    }

    public double weight(float lineLength, float distance) {
        return Math.pow(Math.pow(lineLength, P) / (a + distance), b);
    }

    public Point findEquivalentPoint(Line A, Line B, int x, int y) {
        Point equivalent = new Point();
        float tempX, tempY;

        //calculate D and F from A
        VectorF Va = new VectorF(A);
        float[] f = A.getPts();
        float D = Va.distToPoint(x, y, f[0], f[1]);
        float F = Va.distanceAlong(x, y, f[0], f[1]);

        //look at B.
        VectorF Vb = new VectorF(B);
        VectorF Nb = Vb.normal();

        //travel along PQ
        f = B.getPts();
        tempX = Vb.x * F + f[0];
        tempY = Vb.y * F + f[1];
        Nb.scale(D / Nb.length());
        equivalent.x = (int)(tempX - Nb.x);
        equivalent.y = (int)(tempY - Nb.y);

        return equivalent;
    }



    public Point calcVector() {
        Point result = new Point();
        //resolution
        //arrayLines src
        //arrayLines dst
        //point x, y




        return null;
    }


}
