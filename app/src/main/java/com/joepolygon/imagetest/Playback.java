package com.joepolygon.imagetest;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Playback extends AppCompatActivity {
    String projectName;
    int frameLoaded;
    ImageView imgView;
    Bitmap frame;

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
        initializePlayback();
    }

    public void loadFrame(int frameNo) {
        String fileName = String.format("%04d.jpg", frameNo);
        File f = new File(
                this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        , projectName + File.separator
                        + RenderSettings.RENDER_FOLDER + File.separator
                        + fileName);
        Log.v("Playback", "loadFrame: loading from " + f.getAbsolutePath());
        try (InputStream inputStream = new FileInputStream(f)) {
            frame = BitmapFactory.decodeStream(inputStream);
            if (frame != null) {
                imgView.setImageBitmap(frame);
                frameLoaded = frameNo;
            } else {
            }
        } catch (FileNotFoundException e) {
            Log.v("Playback", "loadFrame suffered a FileNotFound exception");
            e.printStackTrace();
            Toast.makeText(this, "Frame " + frameNo + " failed.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.v("Playback", "loadFrame suffered an IO exception");
            e.printStackTrace();
            Toast.makeText(this, "Frame " + frameNo + " failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayback() {
        //load frame 0, if available.
        Log.v("Playback", "Loading frame 0");
        loadFrame(0);
        //
    }

    public void onStepBack(View v) {
        loadFrame (frameLoaded - 1);
    }

    public void onStepForward(View v) {
        loadFrame (frameLoaded + 1);
    }

    public void onBackward(View v) {
        onStepBack(v);
    }

    public void onForward(View v) {
        onStepForward(v);
    }

    public void onStop(View v) {
        //do nothing; play doesn't work yet.
    }
}
