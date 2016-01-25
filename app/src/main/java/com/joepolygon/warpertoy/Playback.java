package com.joepolygon.warpertoy;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Playback extends AppCompatActivity {
    private String projectName;
    private int frameCount;
    private int frameLoaded;
    private int downX, downFrame;
    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imgView = (ImageView) findViewById(R.id.imgDisplay);

        android.support.v7.app.ActionBar temp = getSupportActionBar();
        if (temp != null) {
            temp.setDisplayHomeAsUpEnabled(true);
        }


        projectName = Project.readProjectName(this);
        frameCount = RenderSettings.getNumFramesRendered(this, projectName);
        loadFrame(0);
    }

    /**
     * Handle touch events to allow the very slick swipe-animation that plays the warp.
     * @param event The touch event that triggered this callback
     * @return True if the event was handled (always returns true)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int posX;
        int rangePixels, rangeFrames;
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int)event.getX();
                downFrame = frameLoaded;
                break;
            case MotionEvent.ACTION_MOVE:
                //TODO:  if currently playing back, stop. Or ignore this.
                posX = (int)event.getX();
                if (posX > downX) {
                    rangePixels = width - downX == 0 ? 1 : width - downX; //to prevent divide-by-zero
                    rangeFrames = frameCount - downFrame;
                    loadFrame((posX - downX) * rangeFrames / rangePixels + downFrame);
                } else {
                    rangePixels = downX == 0 ? 1 : downX; //to prevent divide-by-zero
                    rangeFrames = downFrame;
                    loadFrame(posX * rangeFrames / rangePixels);
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Open the image specified by the given frame number. Looks in the RenderSettings.RENDER_FOLDER for frames.
     * @param frameNo The frame to load
     */
    private void loadFrame(int frameNo) {
        Bitmap frame;
        String fileName = String.format("%04d.jpg", frameNo);
        File f = new File(
                this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        , projectName + File.separator
                        + RenderSettings.RENDER_FOLDER + File.separator
                        + fileName);
        if (frameNo >= frameCount || frameNo < 0) {
            return;
        }
        //Log.v("Playback", "loadFrame: loading from " + f.getAbsolutePath());
        try (InputStream inputStream = new FileInputStream(f)) {
            frame = BitmapFactory.decodeStream(inputStream);
            if (frame != null) {
                imgView.setImageBitmap(frame);
                frameLoaded = frameNo;
            }
        } catch (FileNotFoundException e) {
            Log.v("Playback", "loadFrame suffered a FileNotFound exception");
            e.printStackTrace();
            //Toast.makeText(this, "Frame " + (frameNo+1) + " failed.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.v("Playback", "loadFrame suffered an IO exception");
            e.printStackTrace();
            //Toast.makeText(this, "Frame " + (frameNo+1) + " failed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * GUI callback to step one frame backward
     * @param v The source of the call
     */
    public void onStepBack(View v) {
        loadFrame(frameLoaded - 1);
    }

    /**
     * GUI callback to step one frame forward
     * @param v The source of the call
     */
    public void onStepForward(View v) {
        loadFrame (frameLoaded + 1);
    }
}
