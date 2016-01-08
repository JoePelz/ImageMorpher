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
    //canvas bitmap
    private Bitmap canvasBitmap;

    private Point touchSpot;

    public EditView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        touchSpot = new Point();
    //get drawing area setup for interaction
        drawPaint1 = new Paint();
        drawPaint1.setColor(paintColor1);
        drawPaint1.setStrokeWidth(5.0f);
        drawPaint2 = new Paint();
        drawPaint2.setColor(paintColor2);
        drawPaint2.setStrokeWidth(1.0f);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        bgBitmap = bm;
        super.setImageBitmap(bm);
        imageMatrix = new Matrix();
        //getImageMatrix().invert(imageMatrix);
        imageMatrix = getImageMatrix();
        Log.v("EditView", "getImageMatrix() -> " + getImageMatrix());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        //getImageMatrix will give the matrix used to display the image
        c.drawText("Touch: (" + touchSpot.x + ", " + touchSpot.y + ")", 5, 20, drawPaint2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                touchSpot.x = (int)event.getX();
                touchSpot.y = (int)event.getY();
                break;
            default:
                break;
        }

        //Canvas c

        //invalidate();
        return true;
    }
}
