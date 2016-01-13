package com.joepolygon.imagetest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.VideoView;

import java.io.File;

public class Playback extends AppCompatActivity {
    String project;
    File frameDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        project = Project.readProjectName(this);
    }

    public void onStepBack(View v) {

    }

    public void onStepForward(View v) {

    }

    public void onBackward(View v) {

    }

    public void onForward(View v) {

    }

    public void onStop(View v) {

    }
}
