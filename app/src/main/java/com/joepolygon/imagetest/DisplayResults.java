package com.joepolygon.imagetest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class DisplayResults extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private ProgressBar pb;
    private TextView progressMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = (ProgressBar) findViewById(R.id.progressRendering);
        progressMessage = (TextView) findViewById(R.id.progressMessage);
        SeekBar sb = (SeekBar) findViewById(R.id.seekFrames);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) findViewById(R.id.seekA);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) findViewById(R.id.seekB);
        sb.setOnSeekBarChangeListener(this);
        sb = (SeekBar) findViewById(R.id.seekP);
        sb.setOnSeekBarChangeListener(this);
    }

    public void onRender(View v) {
        Log.v("DisplayResults", "Render clicked");
    }

    public void onView(View v) {
        Log.v("DisplayResults", "View clicked");
    }

    public void updateProgressMessage() {
        int i = pb.getProgress() + 2;
        int n = pb.getMax() + 2;
        int ratio = (i * 100) / n;
        String temp = String.format("Progress: %d / %d (%d%%)", i, n, ratio);
        progressMessage.setText(temp);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        EditText field;
        if (!fromUser)
            return;
        String tag = (String) seekBar.getTag();
        switch (tag) {
            case "frames":
                //Log.v("DisplayResults", "frames seekbar changed");
                field = (EditText) findViewById(R.id.valFrames);
                field.setText(String.valueOf(seekBar.getProgress() + 2));
                pb.setMax(seekBar.getProgress());
                updateProgressMessage();
                break;
            case "a":
                //Log.v("DisplayResults", "'a' seekbar changed");
                field = (EditText) findViewById(R.id.valA);
                field.setText(String.valueOf((seekBar.getProgress() + 1) / 100.0 * 0.1));
                break;
            case "b":
                //Log.v("DisplayResults", "'b' seekbar changed");
                field = (EditText) findViewById(R.id.valB);
                field.setText(String.valueOf(seekBar.getProgress() / 100.0 + 1.0));
                break;
            case "P":
                //Log.v("DisplayResults", "'P' seekbar changed");
                field = (EditText) findViewById(R.id.valP);
                field.setText(String.valueOf(seekBar.getProgress() / 100.0));
                break;
            default:
                Log.v("DisplayResults", "unknown seekbar changed.");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
