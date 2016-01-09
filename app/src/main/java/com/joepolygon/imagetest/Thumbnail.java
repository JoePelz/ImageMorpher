package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 *
 * Created by Joe on 2016-01-09.
 */
public class Thumbnail extends ImageView implements ProjectUpdateListener {
    //background image
    private Bitmap bgBackup;
    private Bitmap bgBitmap;
    // matrix used to translate 0-1 to image coordinates
    private Matrix drawMatrix;
    private float[] matValues;
    private Project model;
    private int role;

    public Thumbnail(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateDimensions(Math.min(right - left, bottom - top));
            }
        });
    }

    private void setupDrawing(){
        matValues = new float[9];
    }

    public void setModel(Project model) {
        this.model = model;
    }

    public void setRole(int image) {
        role = image;
    }

    public void updateDimensions(int size) {
        if (bgBackup == null) {
            return;
        }
        if (bgBitmap != bgBackup) {
            bgBitmap.recycle();
        }
        bgBitmap = ThumbnailUtils.extractThumbnail(bgBackup, size, size);
        super.setImageBitmap(bgBitmap);

        drawMatrix = getImageMatrix();
        drawMatrix.getValues(matValues);

        drawMatrix = new Matrix();
        //scale to fit window
        drawMatrix.setScale(size, size);
        //match offset
        drawMatrix.postTranslate(matValues[2], matValues[5]);
    }

    public void updateImage() {
        bgBackup = model.getImage(role);
        if (bgBackup == null) {
            bgBitmap = null;
            return;
        }
        bgBitmap = bgBackup;
        super.setImageBitmap(bgBitmap);
        model.setLoaded(role, true);

        drawMatrix = getImageMatrix();
        drawMatrix.getValues(matValues);

        drawMatrix = new Matrix();
        //scale to fit window
        drawMatrix.setScale(getHeight(), getHeight());
        //match offset
        drawMatrix.postTranslate(matValues[2], matValues[5]);
    }


    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (model.getSelectedLine(role) != null)
            model.getSelectedLine(role).draw(c, drawMatrix, Line.paintSelected);
        for (Line l : model.getLines(role)) {
            l.draw(c, drawMatrix, Line.paintThin);
        }
    }

    @Override
    public void onProjectUpdate() {
        invalidate();
    }
}
