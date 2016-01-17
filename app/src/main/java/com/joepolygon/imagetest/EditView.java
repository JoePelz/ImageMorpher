package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by Joe on 2016-01-07.
 * Intended to display an image and allow the user to draw upon it.
 */
public class EditView extends ImageView implements ProjectUpdateListener {
    //background image
    private Bitmap bgBitmap;
    private Bitmap bgBackup;
    // matrix used to translate 0-1 to image coordinates
    private Matrix drawMatrix;
    private float[] matValues;
    private Project model;

    private int  selectedVertex;
    private Line tempLine;
    private float startx, starty;


    public EditView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //Log.v("Thumbnail", "new layout: " + left + "," + top + "," + right + ", " + bottom);
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

    private void updateDimensions(int size) {
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
        bgBackup = model.getImage(Project.IMG_EDIT);
        if (bgBackup == null) {
            bgBitmap = null;
            super.setImageResource(R.drawable.test);
            return;
        }
        bgBitmap = bgBackup;
        super.setImageBitmap(bgBitmap);

        drawMatrix = getImageMatrix();
        drawMatrix.getValues(matValues);

        drawMatrix = new Matrix();
        //scale to fit window
        drawMatrix.setScale(getHeight(), getHeight());
        //match offset
        drawMatrix.postTranslate(matValues[2], matValues[5]);

        //Log.v("EditView", "Scaled to " + getHeight());
        //Log.v("EditView", "getImageMatrix() -> " + getImageMatrix());
        //Log.v("EditView", "Image resolution -> " + bgBitmap.getWidth() + " * " + bgBitmap.getHeight());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (model == null) {
            return;
        }

        if (model.getSelectedLine() != null)
            LineArtist.draw(c, drawMatrix, model.getSelectedLine(), LineArtist.paintSelected);

        for (Line l : model.getLines()) {
            LineArtist.draw(c, drawMatrix, l, LineArtist.paintNice);
        }
        if (tempLine != null) {
            LineArtist.draw(c, drawMatrix, tempLine, LineArtist.paintErasable);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float threshold = 0.06f;
        float closestDist = threshold + 1;
        Line closestLine = null;
        float tempx, tempy;

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
                //if clicking outside the image, abort.
                if (startx < 0 || startx > 1 || starty < 0 || starty > 1) {
                    break;
                }
                if (!model.getLines().isEmpty()) {
                    float tempDist;
                    for (Line l : model.getLines()) {
                        tempDist = Math.abs(l.distanceFromLinePts(startx, starty));
                        if (tempDist < closestDist) {
                            closestDist = tempDist;
                            closestLine = l;
                        }
                    }
                }
                if (closestDist < threshold) {
                    model.selectLine(closestLine);
                    selectedVertex = closestLine.getClosestVertex(startx, starty);
                    tempLine = null;
                } else {
                    model.selectLine(null);
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
                tempx = (event.getX() - matValues[2]) / getHeight();
                tempy = (event.getY() - matValues[5]) / getHeight();
                if (model.getSelectedLine() == null) {
                    if (tempx < 0 || tempx > 1 || tempy < 0 || tempy > 1) {
                        tempLine = null;
                    } else if (tempLine != null) {
                        tempLine = new Line(startx, starty, tempx, tempy);
                    }
                } else {
                    if (tempx < 0 || tempx > 1 || tempy < 0 || tempy > 1) {
                        model.removeSelected();
                    } else {
                        if (selectedVertex == Line.P0) {
                            model.getSelectedLine().setP0(tempx, tempy);
                        } else if (selectedVertex == Line.P1) {
                            model.getSelectedLine().setP1(tempx, tempy);
                        }
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
                    model.addLine(tempLine);
                    tempLine = null;
                }
                model.fireUpdate();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onProjectUpdate() {
        invalidate();
    }
}
