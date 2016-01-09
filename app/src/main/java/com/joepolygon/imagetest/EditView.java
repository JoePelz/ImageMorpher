package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
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
    // matrix used to display the bgBitmap
    private Matrix imageMatrix;
    float[] matValues;

    private Point touchSpot;

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
        imageMatrix = new Matrix();
        //getImageMatrix().invert(imageMatrix);
        imageMatrix = getImageMatrix();
        imageMatrix.getValues(matValues);
        Log.v("EditView", "getImageMatrix() -> " + getImageMatrix());
        Log.v("EditView", "Image resolution -> " + bgBitmap.getWidth() + " * " + bgBitmap.getHeight());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        //getImageMatrix will give the matrix used to display the image
        c.drawText("Touch: (" + touchSpot.x + ", " + touchSpot.y + ")", 5, 20, drawPaint2);
        c.drawLine(0, 0, c.getWidth(), c.getHeight(), drawPaint1);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                touchSpot.x = (int)((event.getX() - matValues[2]) / getHeight()*100);
                touchSpot.y = (int)((event.getY() - matValues[5]) / getHeight()*100);
                break;
            default:
                break;
        }
        //Canvas c

        invalidate(0,0,300,100);
        return true;
    }
}
