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
 * Rendering engine for the image warping. Will load the project images/lines and generate tweens.
 * Created by Joe on 2016-01-13.
 */
class Engine {
    private final Context app;
    private Bitmap imgA;
    private Bitmap imgB;
    private final int[] pixelsA, pixelsB, pixelsR;
    private final String projectName;
    private final int width;
    private final int height;
    private final int frames;
    /** Small value added to prevent division by 0. */
    private final float a;
    /** Distance falloff exponent. */
    private final float b;
    /** Influence of line length on magnitude of effect. */
    private final float P;

    /** The lines on the src(A) image. */
    private final ArrayList<Line> srcs;
    /** The lines on the dst(B) image. */
    private final ArrayList<Line> dsts;


    /** Forward offsets to find the origin of a point in the destination. */
    private int[] forwardX;
    private int[] forwardY;
    /** Backwards offsets to find the origin of a point in the source. */
    private int[] backwardX;
    private int[] backwardY;

    /**
     * Create a new rendering engine.
     * @param project The project name to work within
     * @param frames The number of frames to render
     * @param a A small value to prevent division by 0.  increasing this smooths the warp.
     * @param b Controls falloff of the range of effect based on distance from lines. Higher values tighten up effect to only be close to lines.
     * @param P Indicates the effect of line length of range of effect.
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
        if (p.isLeftLoaded()) {
            imgA = ThumbnailUtils.extractThumbnail(p.getImage(Project.IMG_LEFT), width, height);
        }
        if (p.isRightLoaded()) {
            imgB = ThumbnailUtils.extractThumbnail(p.getImage(Project.IMG_RIGHT), width, height);
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

    private int interpolate(int left, int right, int num, int denom) {
        return left + (right - left) * num / denom;
    }

    public void generateMapForDstPoints() {
        forwardX = new int[width*height];
        forwardY = new int[width*height];
        int factor = 3;
        int remainder;
        int i;
        Point offset;
        int x, y;
        /*
        _ _ _ _ _
        _ _ _ _ _
        _ _ _ _ _
        _ _ _ _ _
        _ _ _ _ _
         */

        //A -- calculate key points
        for (y = 0; y < height; y += factor) {
            for (x = 0; x < width; x += factor) {
                i = y*width + x;
                //calculate value of key points
                offset = vectorForPoint(dsts, srcs, x, y);
                forwardX[i] = offset.x;
                forwardY[i] = offset.y;
            }
        }

        /*
        A _ _ A _
        _ _ _ _ _
        _ _ _ _ _
        A _ _ A _
        _ _ _ _ _
         */

        //B -- calculate rows connecting
        for (y = 0; y < height; y += factor) {
            for (x = 0; x < width; x++) {
                remainder = x % factor;
                if (remainder == 0) {
                    continue;
                }
                i = y*width + x;

                if (x + factor - remainder >= width) {
                    //use the left value
                    forwardX[i] = forwardX[i - remainder];
                    forwardY[i] = forwardY[i - remainder];
                } else {
                    //interpolate between left and right
                    forwardX[i] = interpolate(forwardX[i - remainder], forwardX[i + (factor-remainder)], remainder, factor);
                    forwardY[i] = interpolate(forwardY[i - remainder], forwardY[i + (factor-remainder)], remainder, factor);
                }
            }
        }

        /*
        A B B A B
        _ _ _ _ _
        _ _ _ _ _
        A B B A B
        _ _ _ _ _
         */

        //C -- calculate columns connecting
        for (y = 0; y < height; y++) {
            remainder = y % factor;
            if (remainder == 0) {
                continue;
            }
            for (x = 0; x < width; x++) {
                i = y*width + x;

                if (y + factor - remainder >= height) {
                    //use the top value
                    forwardX[i] = forwardX[i - remainder * width];
                    forwardY[i] = forwardY[i - remainder * width];
                } else {
                    //interpolate between top and bottom values
                    //top is forward[i - remainder*width]
                    //bottom is forward[i + (factor - remainder)*width]
                    forwardX[i] = interpolate(
                            forwardX[i - remainder*width],
                            forwardX[i + (factor - remainder)*width],
                            remainder,
                            factor);
                    forwardY[i] = interpolate(
                            forwardY[i - remainder*width],
                            forwardY[i + (factor - remainder)*width],
                            remainder,
                            factor);
                }
            }
        }

        /*
        A B B A B
        C C C C C
        C C C C C
        A B B A B
        C C C C C
         */
    }

    public void generateMapForSrcPoints() {
        backwardX = new int[width*height];
        backwardY = new int[width*height];
        int factor = 3;
        int remainder;
        int i;
        Point offset;
        int x, y;

        //A -- calculate key points
        for (y = 0; y < height; y += factor) {
            for (x = 0; x < width; x += factor) {
                i = y*width + x;
                //calculate value of key points
                offset = vectorForPoint(srcs, dsts, x, y);
                backwardX[i] = offset.x;
                backwardY[i] = offset.y;
            }
        }

        //B -- calculate rows connecting
        for (y = 0; y < height; y += factor) {
            for (x = 0; x < width; x++) {
                remainder = x % factor;
                if (remainder == 0) {
                    continue;
                }
                i = y*width + x;

                if (x + factor - remainder >= width) {
                    //use the left value
                    backwardX[i] = backwardX[i - remainder];
                    backwardY[i] = backwardY[i - remainder];
                } else {
                    //interpolate between left and right
                    backwardX[i] = interpolate(
                            backwardX[i - remainder],
                            backwardX[i + (factor-remainder)],
                            remainder, factor);
                    backwardY[i] = interpolate(
                            backwardY[i - remainder],
                            backwardY[i + (factor-remainder)],
                            remainder, factor);
                }
            }
        }

        //C -- calculate columns connecting
        for (y = 0; y < height; y++) {
            remainder = y % factor;
            if (remainder == 0) {
                continue;
            }
            for (x = 0; x < width; x++) {
                i = y*width + x;

                if (y + factor - remainder >= height) {
                    //use the top value
                    backwardX[i] = backwardX[i - remainder * width];
                    backwardY[i] = backwardY[i - remainder * width];
                } else {
                    //interpolate between top and bottom values
                    //top is forward[i - remainder*width]
                    //bottom is forward[i + (factor - remainder)*width]
                    backwardX[i] = interpolate(
                            backwardX[i - remainder*width],
                            backwardX[i + (factor - remainder)*width],
                            remainder,
                            factor);
                    backwardY[i] = interpolate(
                            backwardY[i - remainder*width],
                            backwardY[i + (factor - remainder)*width],
                            remainder,
                            factor);
                }
            }
        }
    }

    public int blendColors(int from, int to, int num, int denom) {
        //TODO: experiment with blending over HSL instead of RGB
        int result;
        int fb = (from & 0x0000FF);
        int fg = (from & 0x00FF00) >> 8;
        int fr = (from & 0xFF0000) >> 16;
        int tb = (to & 0x0000ff);
        int tg = (to & 0x00ff00) >> 8;
        int tr = (to & 0xff0000) >> 16;
        result  = (fb + (tb - fb) * num / denom);
        result += (fg + (tg - fg) * num / denom) << 8;
        result += (fr + (tr - fr) * num / denom) << 16;
        return result | 0xFF000000; //white alpha
    }

    private Bitmap generateImage(int num, int denom) {
        if (num == 0) {
            return imgA;
        } else if (num == denom) {
            return imgB;
        } else if (num < 0 || num > denom) {
            throw new InvalidParameterException("numerator must be between 0 and the denominator inclusive.");
        }
        //num is between 0 and the denominator.
        //FWD = generate t_percent from A + vectors
        //BWD = generate (1-t_percent) from B + vectors
        //generate t_percent color shift from FWD to BWD

        Log.v("Engine", "Generating frame t=" + num + "/" + denom);

        //placeholder
        //result = Bitmap.createBitmap(width, height, imgA.getConfig());
        imgA.getPixels(pixelsA, 0, width, 0, 0, width, height);
        imgB.getPixels(pixelsB, 0, width, 0, 0, width, height);

        int offsetAX;
        int offsetAY;
        int offsetBX;
        int offsetBY;
        int Ai;
        int Bi;
        int colorA;
        int colorB;

        for(int i = 0; i < pixelsR.length; i++) {
            offsetAX = forwardX[i] * num / denom;
            offsetAY = forwardY[i] * num / denom;
            offsetBX = backwardX[i] * (denom - num) / denom;
            offsetBY = backwardY[i] * (denom - num) / denom;
            Ai = i + width * offsetAY + offsetAX;
            Bi = i + width * offsetBY + offsetBX;
            if (Ai < 0 || Ai >= pixelsA.length) {
                colorA = 0x000000;
            } else {
                colorA = pixelsA[Ai];
            }
            if (Bi < 0 || Bi >= pixelsB.length) {
                colorB = 0x000000;
            } else {
                colorB = pixelsB[Bi];
            }
            pixelsR[i] = blendColors(colorA, colorB, num, denom);
        }

        return Bitmap.createBitmap(pixelsR, width, height, Bitmap.Config.ARGB_8888);
    }

    private boolean saveFrame(Bitmap image, int frameNo) {
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

    private void clearFrames() {
        File frameFolder = new File(app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + RenderSettings.RENDER_FOLDER);
        File[] frameFolderContents = frameFolder.listFiles();
        for (File f : frameFolderContents) {
            f.delete();
        }
    }

    public void render() {
        Bitmap frame;
        Log.v("Engine", "Frames to write: " + frames);


        //generate vector maps
        generateMapForSrcPoints();
        generateMapForDstPoints();
        Log.v("Engine", "Generating vector maps complete.");

        clearFrames();
        Log.v("Engine", "Old frames deleted. Generating new frames.");

        for(int i = 0; i < frames; i++) {
            //frames == 5; i == [0..4]
            frame = generateImage(i, frames-1);
            saveFrame(frame, i);
        }
    }

    private class vectorGeneratorSRC implements Runnable {

        @Override
        public void run() {
            generateMapForSrcPoints();
        }
    }
}
