package com.joepolygon.imagetest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ShowImages extends AppCompatActivity {
    private final static int SELECT_PHOTO_LEFT = 0x10;
    private final static int SELECT_PHOTO_RIGHT = 0x20;
    private final static int PICK_CAMERA = 0x1;
    private final static int PICK_GALLERY = 0x2;
    private final static int MY_REQUEST_CODE = 0x1;

    private SeekBar seekBar;
    private Thumbnail imgLeft;
    private Thumbnail imgRight;
    private EditView  imgEdit;

    private Project model;

    //=====================   Life Cycle Methods ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        Button btnBuild = (Button) findViewById(R.id.btnBuild);
        Button btnSave = (Button)  findViewById(R.id.btnSave);
        Button btnOpen = (Button)  findViewById(R.id.btnOpen);
        seekBar  = (SeekBar)   findViewById(R.id.seekBar);
        imgLeft  = (Thumbnail) findViewById(R.id.imgLeft);
        imgLeft.setRole(Project.IMG_LEFT);
        imgRight = (Thumbnail) findViewById(R.id.imgRight);
        imgRight.setRole(Project.IMG_RIGHT);
        imgEdit  = (EditView) findViewById(R.id.imgEdit);

        model = new Project(this);
        imgEdit.setModel(model);
        imgLeft.setModel(model);
        imgRight.setModel(model);
        model.addUpdateListener(imgLeft);
        model.addUpdateListener(imgRight);

        model.loadState(savedInstanceState);

        updateImages();

        btnBuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("ShowImages", "Building " + seekBar.getProgress() + " frames...");
            }
        });

        imgLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!model.isLeftLoaded() || model.getEditImage() == Project.IMG_LEFT) {
                    selectImage(SELECT_PHOTO_LEFT);
                } else {
                    model.setEditImage(Project.IMG_LEFT);
                }
                updateImages();
            }
        });

        imgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!model.isRightLoaded() || model.getEditImage() == Project.IMG_RIGHT) {
                    selectImage(SELECT_PHOTO_RIGHT);
                } else {
                    model.setEditImage(Project.IMG_RIGHT);
                }
                updateImages();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //doStuff...
                actionSave();
            }
        });
        btnOpen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                actionOpen();
            }
        });
    }

    private void actionSave() {
        //save the current Project object to a file.
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "save1.proj");
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f))) {
            model.saveToFile(os);
            Toast.makeText(this, "File saved, I think!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("ShowImages", "actionSave (writeObject?) ran into IOException. " + e.getMessage());
        }
    }

    private void actionOpen() {
        //replace the current Project object.
        File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "save1.proj");
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(f))) {
            if (model.loadFromFile(is)) {
                Toast.makeText(this, "Project opened successfully. Maybe.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Could not open project. Sorry!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("ShowImages", "actionSave (writeObject?) ran into IOException. " + e.getMessage());
        }
    }

    private void updateImages() {
        imgRight.updateImage();
        imgLeft.updateImage();
        imgEdit.updateImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        model.saveState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // if we are here, everything processed successfully.
            if ((requestCode & PICK_GALLERY) == PICK_GALLERY) {
                // if we are here, we are hearing back from the image gallery.

                Uri imageUri = data.getData();

                if ((requestCode & SELECT_PHOTO_LEFT) == SELECT_PHOTO_LEFT) {
                    try {
                        model.setLeft(imageUri);
                        model.setEditImage(Project.IMG_LEFT);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    try {
                        model.setRight(imageUri);
                        model.setEditImage(Project.IMG_RIGHT);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                updateImages();

            } else if ((requestCode & PICK_CAMERA) == PICK_CAMERA) {
                String target;
                if ((requestCode & SELECT_PHOTO_LEFT) == SELECT_PHOTO_LEFT) {
                    target = "tempLeft.jpg";
                } else {
                    target = "tempRight.jpg";
                }
                File f = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (f == null) {
                    Toast.makeText(this, "Couldn't open file system", Toast.LENGTH_LONG).show();
                    return;
                }
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(target)) {
                        f = temp;
                        break;
                    }
                }

                Uri imageUri = Uri.fromFile(f);
                if ((requestCode & SELECT_PHOTO_LEFT) == SELECT_PHOTO_LEFT) {
                    try {
                        model.setLeft(imageUri);
                        model.setEditImage(Project.IMG_LEFT);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    try {
                        model.setRight(imageUri);
                        model.setEditImage(Project.IMG_RIGHT);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                updateImages();
            }
        }
    }

    private void selectImage(final int origin) {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ShowImages.this);
        builder.setTitle("Choose Photo From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_REQUEST_CODE);
                        return;
                    }

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f;
                    if (origin == SELECT_PHOTO_LEFT) {
                        f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tempLeft.jpg");
                    } else {
                        f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "tempRight.jpg");
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, origin | PICK_CAMERA);
                }
                else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, origin | PICK_GALLERY);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                Log.v("ShowImages", "Camera permission granted!");
            }
            else {
                Log.v("ShowImages", "FAILED: Camera request denied!");
                //cannot use camera.  I should disable the camera option in this case.
            }
        }
    }
}
