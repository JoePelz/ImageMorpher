package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Joe on 2016-01-07.
 * Intended to display an image and allow the user to draw upon it.
 */
public class EditView extends ImageView {
    //background image
    private Bitmap bgBitmap;
    // matrix used to translate 0-1 to image coordinates
    private Matrix drawMatrix;
    private float[] matValues;

    private ArrayList<Line> controlLines;
    private Line selectedLine;
    private int  selectedVertex;
    private Line tempLine;
    private float startx, starty;

    public EditView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        matValues = new float[9];
        controlLines = new ArrayList<Line>();
        // vertical test line.
        // controlLines.add(new Line(0.5f, 0.2f, 0.5f, 0.8f));
    }

    /*
    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(Math.min(scaleWidth, scaleHeight), Math.min(scaleWidth, scaleHeight));

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    */

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            return;
        }
        bgBitmap = bm;
        super.setImageBitmap(bgBitmap);

        drawMatrix = getImageMatrix();
        drawMatrix.getValues(matValues);

        drawMatrix = new Matrix();
        //scale to fit window
        drawMatrix.setScale(1100, 1100);
        //match offset
        drawMatrix.postTranslate(matValues[2], matValues[5]);

        Log.v("EditView", "getImageMatrix() -> " + getImageMatrix());
        Log.v("EditView", "Image resolution -> " + bgBitmap.getWidth() + " * " + bgBitmap.getHeight());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (selectedLine != null) {
            selectedLine.drawSelected(c, drawMatrix);
        }
        for (Line l : controlLines) {
            l.drawNice(c, drawMatrix);
        }
        if (tempLine != null) {
            tempLine.drawErasable(c, drawMatrix);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float threshhold = 0.06f;
        float closestDist = threshhold + 1;
        Line closestLine = null;

        switch(event.getActionMasked()) {
            /* Action down should first
             * Check if an existing vertex or line is within range
             *     if so, select that line, and save which vertex is being edited.
             * Otherwise, clear vertex being edited / selection
             *     and begin a new tempLine, saving startx/y
             */
            case MotionEvent.ACTION_DOWN:
                startx = (event.getX() - matValues[2]) / getHeight();
                starty = (event.getY() - matValues[5]) / getHeight();
                if (!controlLines.isEmpty()) {
                    float tempDist;
                    for (Line l : controlLines) {
                        tempDist = Math.abs(l.distanceFromLine(startx, starty));
                        if (tempDist < closestDist) {
                            closestDist = tempDist;
                            closestLine = l;
                        }
                    }
                }
                if (closestDist < threshhold) {
                    selectedLine = closestLine;
                    selectedVertex = closestLine.getClosestVertex(startx, starty);
                    tempLine = null;
                } else {
                    selectedLine = null;
                    tempLine = new Line(
                            startx,
                            starty,
                            startx,
                            starty);
                }
                invalidate();
                break;

            /* Action move should
             * if selection is set, move saved vertex
             * else if tempLine exists,
             *     recreate templine with a new endpoint.
             */
            case MotionEvent.ACTION_MOVE:
                if (selectedLine == null) {
                    tempLine = new Line(
                            startx,
                            starty,
                            (event.getX() - matValues[2]) / getHeight(),
                            (event.getY() - matValues[5]) / getHeight());
                } else {
                    if (selectedVertex == Line.P0) {
                        selectedLine.setP0(
                                (event.getX() - matValues[2]) / getHeight(),
                                (event.getY() - matValues[5]) / getHeight());
                    } else if (selectedVertex == Line.P1) {
                        selectedLine.setP1(
                                (event.getX() - matValues[2]) / getHeight(),
                                (event.getY() - matValues[5]) / getHeight());
                    }
                }
                invalidate();
                break;
            /* Action up should
             * if tempLine is not null, it should be saved to the line collection.
             *     and the new permanent line selected.
             */
            case MotionEvent.ACTION_UP:
                if (tempLine != null) {
                    controlLines.add(tempLine);
                    selectedLine = tempLine;
                    tempLine = null;
                    invalidate();
                }
                break;
            default:
                break;
        }
        return true;
    }
}
