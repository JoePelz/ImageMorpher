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

/**
 * Created by Joe on 2016-01-07.
 * Intended to display an image and allow the user to draw upon it.
 */
public class EditView extends ImageView {
    //drawing and canvas paint
    private Paint drawPaint1, drawPaint2;
    //initial color  (A R G B)?
    private int paintColor1 = 0xFF660000;
    private int paintColor2 = 0xFF00FF00;
    //canvas
    private Canvas drawCanvas;
    //background image
    private Bitmap bgBitmap;
    // matrix used to translate 0-1 to image coordinates
    private Matrix drawMatrix;
    private float[] matValues;

    private Point touchSpot;
    private Line permaLine;
    private Line tempLine;
    private float startx, starty;

    public EditView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        touchSpot = new Point();
        matValues = new float[9];
    //get drawing area setup for interaction
        drawPaint1 = new Paint();
        drawPaint1.setColor(paintColor1);
        drawPaint1.setStrokeWidth(5.0f);
        drawPaint2 = new Paint();
        drawPaint2.setColor(paintColor2);
        drawPaint2.setStrokeWidth(1.0f);

        tempLine = new Line(new PointF(0.2f, 0.2f), new PointF(0.8f, 0.8f));
        tempLine.setB(new PointF(0.2f, 0.8f));
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
        if (permaLine != null) {
            permaLine.drawNice(c, drawMatrix);
        }
        if (tempLine != null) {
            tempLine.drawErasable(c, drawMatrix);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            /* Action down should first
             * Check if an existing vertex or line is within range
             *     if so, select that line, and save which vertex is being edited.
             * Otherwise, clear vertex being edited
             *     and begin a new tempLine, saving startx/y
             */
            case MotionEvent.ACTION_DOWN:
                startx = (event.getX() - matValues[2]) / getHeight();
                starty = (event.getY() - matValues[5]) / getHeight();
                tempLine = new Line(
                        startx,
                        starty,
                        startx,
                        starty);
                invalidate();
                break;

            /* Action move should
             * if selection is set, move saved vertex
             * else if tempLine exists,
             *     recreate templine with a new endpoint.
             */
            case MotionEvent.ACTION_MOVE:
                tempLine = new Line(
                        startx,
                        starty,
                        (event.getX() - matValues[2]) / getHeight(),
                        (event.getY() - matValues[5]) / getHeight());
                invalidate();
                break;
            /* Action up should
             * if tempLine is not null, it should be saved to the line collection.
             *     and the new permanent line selected.
             */
            case MotionEvent.ACTION_UP:
                permaLine = tempLine;
                tempLine = null;
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }
}
