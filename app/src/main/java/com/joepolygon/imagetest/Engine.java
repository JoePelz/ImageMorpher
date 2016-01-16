package com.joepolygon.imagetest;

import android.graphics.Bitmap;

import java.util.ArrayList;


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

    public float weight(float lineLength, float distance) {
        return (float) Math.pow(Math.pow(lineLength, P) / (a + distance), b);
    }

    public PointF findEquivalentPoint(Line A, Line B, int x, int y) {
        PointF equivalent = new PointF();
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
        equivalent.x = Vb.x * F + f[0];
        equivalent.y = Vb.y * F + f[1];
        Nb.scale(D / Nb.length());
        equivalent.x -= Nb.x;
        equivalent.y -= Nb.y;

        return equivalent;
    }



    public Point vectorForPoint(ArrayList<Line> srcs, ArrayList<Line> dsts, int x, int y) {
        Line A = null;
        Line B = null;
        int nLines = srcs.size();
        PointF[] dP = new PointF[nLines];
        PointF dPSum = new PointF(0, 0);
        float[] W = new float[nLines];
        float WSum = 0;

        //calculate point and weight for each line
        for(int i = 0; i < nLines; i++) {
            A = srcs.get(i);
            B = dsts.get(i);
            //new position relative to (0, 0)
            dP[i] = findEquivalentPoint(A, B, x, y);
            dP[i].x -= x; //now relative to (x, 0)
            dP[i].y -= y; //now relative to (x, y)
            W[i] = weight(A.length(), A.distanceFromLine(x, y));
            //multiply by weight
            dPSum.x += dP[i].x * W[i];
            dPSum.y += dP[i].y * W[i];
            WSum += W[i];
        }
        //divide offset sum by weight sum
        dPSum.x /= WSum;
        dPSum.y /= WSum;

        //save final offset
        Point result = new Point((int)dPSum.x, (int)dPSum.y);

        return result;
    }


}
