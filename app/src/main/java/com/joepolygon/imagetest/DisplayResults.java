package com.joepolygon.imagetest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class DisplayResults extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, TextView.OnEditorActionListener {
    private ProgressBar pb;
    private TextView progressMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar temp = getSupportActionBar();
        if (temp != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

        EditText et = (EditText) findViewById(R.id.valFrames);
        et.setOnEditorActionListener(this);
        et = (EditText) findViewById(R.id.valA);
        et.setOnEditorActionListener(this);
        et = (EditText) findViewById(R.id.valB);
        et.setOnEditorActionListener(this);
        et = (EditText) findViewById(R.id.valP);
        et.setOnEditorActionListener(this);
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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String text = v.getText().toString();
        SeekBar sb;
        String tag = v.getTag().toString();
        double val;
        int frames;

        switch (tag) {
            case "frames":
                frames = Integer.parseInt(text);
                if (frames < 2) {
                    frames = 2;
                } else if (frames > 200) {
                    frames = 200;
                }
                v.setText(String.valueOf(frames));

                sb = (SeekBar) findViewById(R.id.seekFrames);
                if ((frames-2) <= sb.getMax()) {
                    sb.setProgress(frames - 2);
                    updateProgressMessage();
                }
                break;
            case "a":
                val = Double.parseDouble(text);
                if (val <= 0) {
                    val = 0.001;
                    v.setText(String.valueOf(val));
                }

                sb = (SeekBar) findViewById(R.id.seekA);
                Log.v("DisplayResults", "val is " + val);
                val = val * 1000 - 1;
                Log.v("DisplayResults", "modified val is " + val);
                Log.v("DisplayResults", "sb.getMax() is " + sb.getMax());
                if (val <= sb.getMax()) {
                    sb.setProgress((int)val);
                }
                break;
            case "b":
                val = Double.parseDouble(text);
                if (val < 1) {
                    val = 1;
                } else if (val > 2) {
                    val = 2;
                }
                v.setText(String.valueOf(val));

                sb = (SeekBar) findViewById(R.id.seekB);
                val = (val - 1) * 100;
                if (val <= sb.getMax()) {
                    sb.setProgress((int)val);
                }
                break;
            case "P":
                val = Double.parseDouble(text);
                if (val < 0) {
                    val = 0;
                } else if (val > 1) {
                    val = 1;
                }
                v.setText(String.valueOf(val));

                sb = (SeekBar) findViewById(R.id.seekP);
                val = val * 100;
                if (val <= sb.getMax()) {
                    sb.setProgress((int)val);
                }
                break;
            default:
                return false;
        }
        return true;
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
                field.setText(String.valueOf((seekBar.getProgress() + 1) / 1000.0));
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
