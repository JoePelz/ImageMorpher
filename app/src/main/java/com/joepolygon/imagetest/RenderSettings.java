package com.joepolygon.imagetest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RenderSettings extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, TextView.OnEditorActionListener {
    public static final String RENDER_FOLDER = "frames";
    private ProgressBar pb;
    private TextView progressMessage;
    private String projectName;

    private int frames;
    private float a;
    private float b;
    private float P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar temp = getSupportActionBar();
        if (temp != null) {
            temp.setDisplayHomeAsUpEnabled(true);
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

        restoreProject();
    }

    private boolean restoreProject() {
        //read project name
        projectName = Project.readProjectName(this);
        Log.v("RenderSettings", "Project read as: " + projectName);

        //get render settings from project folder (if exists)
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "render.cfg");

        String jsonData;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            jsonData = br.readLine();
        } catch (IOException e) {
            Log.v("RenderSettings", "restoreProject failed with a IO Exception: " + e.getMessage());
            return false;
        }

        try {
            JSONObject json = new JSONObject(jsonData);
            setFrames(json.getInt("frames"));
            setA((float)json.getDouble("a"));
            setB((float)json.getDouble("b"));
            setP((float)json.getDouble("P"));
        } catch (JSONException e) {
            Log.v("RenderSettings", "restoreProject failed with a JSON Exception: " + e.getMessage());
            return false;
        }
        updateProgressMessage();

        return true;
    }

    private void saveProject() {
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "render.cfg");

        JSONObject json = new JSONObject();

        try {
            json.put("frames", frames);
            json.put("a", a);
            json.put("b", b);
            json.put("P", P);
        } catch (JSONException e) {
            Log.v("RenderSettings", "saveProject failed with a JSON Exception: " + e.getMessage());
        }

        try (FileWriter file = new FileWriter(f)) {
            file.write(json.toString());
        } catch (IOException e) {
            Log.v("RenderSettings", "saveProject failed with a IO Exception: " + e.getMessage());
        }
    }

    public void onRender(View v) {
        Log.v("RenderSettings", "Render clicked");
        //create folder "frames"
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + RENDER_FOLDER );
        if (!f.mkdir() && !f.isDirectory()) {
            Log.e("RenderSettings", "onRender making the render folder(\""+RENDER_FOLDER+"\") failed.");
            return;
        }

        //save settings in folder
        saveProject();

        //start generating frames
        Engine e = new Engine(this, projectName, frames, a, b, P, 512, 512);
        e.render();
    }

    public void onView(View v) {
        Log.v("RenderSettings", "View clicked");
        //switch to render view
        Intent intent = new Intent(this, Playback.class);
        startActivity(intent);
    }

    public int getNumFramesRendered() {
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + RENDER_FOLDER );
        if (f.isDirectory() && f.canRead()) {
            return f.list().length;
        } else {
            return 0;
        }
    }

    public void updateProgressMessage() {
        pb.setMax(frames);
        int rendered = getNumFramesRendered();
        pb.setProgress(Math.min(pb.getMax(), rendered));
        int i = rendered;
        int n = pb.getMax();
        int ratio = (i * 100) / n;
        String temp = String.format("Progress: %d / %d (%d%%)", i, n, ratio);
        progressMessage.setText(temp);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String text = v.getText().toString();
        String tag = v.getTag().toString();

        switch (tag) {
            case "frames":
                setFrames(Integer.parseInt(text));
                break;
            case "a":
                setA(Float.parseFloat(text));
                break;
            case "b":
                setB(Float.parseFloat(text));
                break;
            case "P":
                setP(Float.parseFloat(text));
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;
        String tag = (String) seekBar.getTag();
        switch (tag) {
            case "frames":
                //Log.v("RenderSettings", "frames seekbar changed");
                setFrames(seekBar.getProgress() + 2);
                break;
            case "a":
                //Log.v("RenderSettings", "'a' seekbar changed");
                setA((seekBar.getProgress() + 1) / 1000.0f);
                break;
            case "b":
                //Log.v("RenderSettings", "'b' seekbar changed");
                setB(seekBar.getProgress() / 100.0f + 1.0f);
                break;
            case "P":
                //Log.v("RenderSettings", "'P' seekbar changed");
                setP(seekBar.getProgress() / 100.0f);
                break;
            default:
                Log.v("RenderSettings", "unknown seekbar changed.");
                break;
        }
    }

    public void setFrames(int nFrames) {
        EditText field = (EditText) findViewById(R.id.valFrames);
        SeekBar sb = (SeekBar) findViewById(R.id.seekFrames);

        frames = nFrames;
        if (frames < 2) {
            frames = 2;
        }
        field.setText(String.valueOf(frames));
        if ((frames-2) <= sb.getMax()) {
            sb.setProgress(frames - 2);
        }
        updateProgressMessage();
    }
    public void setA(float dA) {
        EditText field = (EditText) findViewById(R.id.valA);
        SeekBar sb = (SeekBar) findViewById(R.id.seekA);

        a = dA;
        if (a <= 0) {
            a = 0.001f;
        }

        field.setText(String.valueOf(a));
        if (a * 1000 - 1 <= sb.getMax()) {
            sb.setProgress((int)(a * 1000 - 1));
        }
    }
    public void setB(float dB) {
        EditText field = (EditText) findViewById(R.id.valB);
        SeekBar  sb = (SeekBar) findViewById(R.id.seekB);

        b = dB;
        if (b < 1) {
            b = 1.0f;
        } else if (b > 2) {
            b = 2.0f;
        }

        field.setText(String.valueOf(b));

        if ((b - 1) * 100 <= sb.getMax()) {
            sb.setProgress((int)((b - 1) * 100));
        }
    }
    public void setP(float dP) {
        EditText field = (EditText) findViewById(R.id.valP);
        SeekBar sb = (SeekBar) findViewById(R.id.seekP);

        P = dP;
        if (P < 0) {
            P = 0.0f;
        } else if (P > 1) {
            P = 1.0f;
        }

        field.setText(String.valueOf(P));

        if (P * 100 <= sb.getMax()) {
            sb.setProgress((int)(P * 100));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
