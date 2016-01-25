package com.joepolygon.warpertoy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

    private int frames = 50;
    private float a = 0.03f;
    private float b = 1.10f;
    private float P = 0.00f;

    private Handler renderMessageHandler;

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

        renderMessageHandler = new Handler() {
            public void handleMessage (Message msg) {
                updateProgressMessage();
            }
        };

        restoreProject();
    }

    /** Restore render settings from a saved file. */
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

    /** Save render settings to a file */
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

    /** GUI callback, on pressing the "render frames" button */
    public void onRender(View v) {
        //create folder "frames"
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + RENDER_FOLDER );
        if (!f.mkdir() && !f.isDirectory()) {
            Log.e("RenderSettings", "onRender making the render folder(\""+RENDER_FOLDER+"\") failed.");
            return;
        }

        //save settings in folder
        saveProject();

        //start generating frames
        Engine e = new Engine(this, renderMessageHandler, projectName, frames, a, b, P, 512, 512);
        e.render();
        updateProgressMessage();
    }

    /** GUI callback, on pressing the "play result" button */
    public void onView(View v) {
        //switch to render view
        Intent intent = new Intent(this, Playback.class);
        startActivity(intent);
    }

    /** Count the number of files in the render directory. */
    public static int getNumFramesRendered(Context app, String project) {
        File f = new File(app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), project + File.separator + RENDER_FOLDER );
        if (f.isDirectory() && f.canRead()) {
            return f.list().length;
        } else {
            return 0;
        }
    }

    /** Update the progress message to show the current number of files rendered. */
    public void updateProgressMessage() {
        pb.setMax(frames);
        int rendered = getNumFramesRendered(this, projectName);
        pb.setProgress(Math.min(pb.getMax(), rendered));
        int n = pb.getMax();
        int ratio = (rendered * 100) / n;
        String temp = String.format("Progress: %d / %d (%d%%)", rendered, n, ratio);
        progressMessage.setText(temp);
    }

    /** GUI callback to link the EditViews to the progress bars */
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

    /** GUI callback to link the progress bars to the EditViews */
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

    /** Setter for number of frames to render */
    private void setFrames(int nFrames) {
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
    /** Setter for weighting equation; A is the offset, to prevent division by 0. */
    private void setA(float dA) {
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
    /** Setter for weighting equation; B controls falloff, 1 is linear, 2 is exponential. */
    private void setB(float dB) {
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
    /** Setter for weighting equation; P is the effect that the line length has on strength.  */
    private void setP(float dP) {
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
