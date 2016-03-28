package com.joepolygon.warpertoy;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class Playback extends AppCompatActivity implements MediaScannerConnection.OnScanCompletedListener {
    private String projectName;
    private int frameCount;
    private int frameLoaded;
    private int downX, downFrame;
    private ImageView imgView;
    private ShareActionProvider mShareActionProvider;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        //mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Return true to display menu
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Send image to..."));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                File img = getCurrentFrame();
                String[] paths = new String[1];
                paths[0] = img.getAbsolutePath();
                String[] mimeTypes = new String[1];
                mimeTypes[0] = "image/jpeg";
                MediaScannerConnection.scanFile(this, paths, mimeTypes, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Returns the currently loaded image file.
     * @return The file currently loaded and displayed.
     */
    private File getCurrentFrame() {
        String fileName = String.format("%04d.jpg", frameLoaded);
        return new File(
                this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                , projectName + File.separator
                + RenderSettings.RENDER_FOLDER + File.separator
                + fileName);
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
