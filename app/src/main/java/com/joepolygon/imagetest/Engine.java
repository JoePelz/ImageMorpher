package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * Created by Joe on 2016-01-13.
 */
public class Engine {
    private Context app;
    private Bitmap imgA;
    private Bitmap imgB;
    private int[] pixelsA, pixelsB, pixelsR;
    private String projectName;
    private int frames;
    private int width;
    private int height;
    /** Small value added to prevent division by 0. */
    private float a;
    /** Distance falloff exponent. */
    private float b;
    /** Influence of line length on magnitude of effect. */
    private float P;

    /** The lines on the src(A) image. */
    private ArrayList<Line> srcs;
    /** The lines on the dst(B) image. */
    private ArrayList<Line> dsts;


    /** Forward offsets to find the origin of a point in the destination. */
    private int[] forward;
    /** Backwards offsets to find the origin of a point in the source. */
    private int[] backward;

    /**
     * Create a new rendering engine.
     * @param project The project name to work within
     * @param frames The number of frames to render
     * @param a
     * @param b
     * @param P
     */
    public Engine(Context app, String project, int frames, float a, float b, float P, int width, int height) {
        this.app = app;
        this.projectName = project;
        this.frames = frames;
        this.a = a;
        this.b = b;
        this.P = P;
        this.width = width;
        this.height = height;
        Project p = new Project(app);
        if (!p.isLeftEmpty()) {
            imgA = ThumbnailUtils.extractThumbnail(p.getImage(Project.IMG_LEFT), 512, 512);
        }
        if (!p.isRightEmpty()) {
            imgB = ThumbnailUtils.extractThumbnail(p.getImage(Project.IMG_RIGHT), 512, 512);
        }
        pixelsA = new int[width * height];
        pixelsB = new int[width * height];
        pixelsR = new int[width * height];

        srcs = p.getLines(Project.IMG_LEFT);
        dsts = p.getLines(Project.IMG_RIGHT);

        //scale the lines to the resolution at hand
        scaleLines(); //to match the chosen resolution
    }

    private void scaleLines() {
        float[] f;
        for (Line l : srcs) {
            f = l.getPts();
            f[0] *= width;
            f[1] *= height;
            f[2] *= width;
            f[3] *= height;
        }
        for (Line l : dsts) {
            f = l.getPts();
            f[0] *= width;
            f[1] *= height;
            f[2] *= width;
            f[3] *= height;
        }
    }

    public float weight(float lineLength, float distance) {
        return (float) Math.pow(Math.pow(lineLength, P) / (a + distance), b);
    }

    public PointF findEquivalentPoint(Line A, Line B, int x, int y) {
        PointF equivalent = new PointF();

        //calculate D and F from A
        VectorF Va = new VectorF(A);
        float[] f = A.getPts();
        float D = Va.distToPoint(x, y, f[0], f[1]);
        float F = Va.distanceAlong(x, y, f[0], f[1]);

        //look at B
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
        Line A;
        Line B;
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

        return new Point((int)dPSum.x, (int)dPSum.y);
    }

    public void generateMapForDstPoints() {
        int size = width * height * 2;
        Point offset;
        forward = new int[size];
        for(int i = 0; i < size; i+= 2) {
            //ix = (i / 2) % width
            //iy = (i / 2) / width
            //pixel i, x offset:  forward[i]
            //pixel i, y offset:  forward[i+1]
            offset = vectorForPoint(dsts, srcs, (i/2) % width, (i/2) / width);
            forward[i] = offset.x;
            forward[i+1] = offset.y;
        }
    }

    public void generateMapForSrcPoints() {
        int size = width * height * 2;
        Point offset;
        backward = new int[size];
        for(int i = 0; i < size; i+= 2) {
            //ix = (i / 2) % width
            //iy = (i / 2) / width
            //pixel i, x offset:  forward[i]
            //pixel i, y offset:  forward[i+1]
            offset = vectorForPoint(srcs, dsts, (i/2) % width, (i/2) / width);
            backward[i] = offset.x;
            backward[i+1] = offset.y;
        }
    }

    public int blendColors(int from, int to, float ratio) {
        //TODO: experiment with blending over HSL instead of RGB
        int result;
        int fr = (from & 0x0000FF);
        int fg = (from & 0x00FF00) >> 8;
        int fb = (from & 0xFF0000) >> 16;
        int tr = to & 0x0000ff;
        int tg = (to & 0x00ff00) >> 8;
        int tb = (to & 0xff0000) >> 16;
        result  =  (int)(fr + (tr - fr) * ratio);
        result += ((int)(fg + (tg - fg) * ratio)) << 8;
        result += ((int)(fb + (tb - fb) * ratio)) << 16;

        return result;
    }

    public Bitmap generateImage(float t) {
        if (t == 0.0f) {
            return imgA;
        } else if (t == 1.0f) {
            return imgB;
        } else if (t < 0.0f || t > 1.0f) {
            throw new InvalidParameterException("t must be between 0 and 1 inclusive.");
        }
        Bitmap result = null;
        //t is between 0 and 1.
        //FWD = generate t_percent from A + vectors
        //BWD = generate (1-t_percent) from B + vectors
        //generate t_percent color shift from FWD to BWD


        Log.v("Engine", "Generating frame t=" + t);


        //placeholder
        result = Bitmap.createBitmap(imgA);
        imgA.getPixels(pixelsA, 0, width, 0, 0, width, height);
        imgB.getPixels(pixelsB, 0, width, 0, 0, width, height);

        for(int i = 0; i < pixelsR.length; i++) {
            pixelsR[i] = blendColors(pixelsA[i], pixelsB[i], t);
        }

        result.setPixels(pixelsR, 0, width, 0, 0, width, height);
        return result;
    }

    public boolean saveFrame(Bitmap image, int frameNo) {
        String filename = String.format("%04d.jpg", frameNo); //frame count limited to 0..9999
        boolean success = false;
        File f = new File(app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + RenderSettings.RENDER_FOLDER + File.separator + filename);
        try (OutputStream stream = new FileOutputStream(f)) {
            image.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            Log.v("Engine", "saveFrame: Wrote frame " + filename);
            Log.v("Engine", "saveFrame: Writing to " + f.getAbsolutePath());
            success = true;
        } catch (IOException e) {
            Log.v("Engine", "saveFrame: Couldn't save " + filename);
            e.printStackTrace();
        }
        return success;
    }

    public void render() {
        float t;
        Bitmap frame;
        Log.v("Engine", "Frames to write: " + frames);
        for(int i = 0; i < frames; i++) {
            //frames == 5; i == [0..4]
            t = (float)i / (frames - 1.0f); // 0/4,  1/4,  2/4,  3/4,  4/4
            frame = generateImage(t);
            saveFrame(frame, i);
        }
    }
}
