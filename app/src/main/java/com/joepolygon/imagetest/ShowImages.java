package com.joepolygon.imagetest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ShowImages extends AppCompatActivity {
    private final static int SELECT_PHOTO_LEFT = 0x10;
    private final static int SELECT_PHOTO_RIGHT = 0x20;
    private final static int PICK_CAMERA = 0x1;
    private final static int PICK_GALLERY = 0x2;
    private final static int MY_REQUEST_CODE = 0x1;

    private Button btnLoadLeft;
    private Button btnLoadRight;
    private ImageView imgLeft;
    private ImageView imgRight;
    private ImageView imgEdit;

    private Project model;

    //=====================   Life Cycle Methods ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        btnLoadLeft  = (Button) findViewById(R.id.btnLoadLeft);
        btnLoadRight = (Button) findViewById(R.id.btnLoadRight);
        imgLeft  = (ImageView) findViewById(R.id.imgLeft);
        imgRight = (ImageView) findViewById(R.id.imgRight);
        imgEdit  = (ImageView) findViewById(R.id.imgEdit);

        model = new Project();

        if (savedInstanceState != null) {
            InputStream inputStream;
            Bitmap image;
            Uri imageUri = savedInstanceState.getParcelable("LeftURI");
            if (imageUri != null) {
                setLeftImage(imageUri);
            }
            imageUri = savedInstanceState.getParcelable("RightURI");
            if (imageUri != null) {
                setRightImage(imageUri);
            }
            imageUri = savedInstanceState.getParcelable("Selected");
            if (imageUri != null) {
                setEditImage(imageUri);
            }
        }

        btnLoadLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(SELECT_PHOTO_LEFT);
            }
        });
        btnLoadRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(SELECT_PHOTO_RIGHT);
            }
        });

        imgLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!model.isLeftLoaded()) {
                    Log.v("ShowImages", "Left is NOT loaded");
                    selectImage(SELECT_PHOTO_LEFT);
                } else {
                    if (model.getEditImage().equals(model.getLeft())) {
                        Log.v("ShowImages", "Left: getEditImage == getLeft");
                        selectImage(SELECT_PHOTO_LEFT);
                    } else {
                        Log.v("ShowImages", "Left: getEditImage != getLeft");
                        setEditImage(model.getLeft());
                    }
                }
            }
        });

        imgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!model.isRightLoaded() || model.getEditImage().equals(model.getRight())) {
                    selectImage(SELECT_PHOTO_RIGHT);
                } else {
                    setEditImage(model.getRight());
                }
            }
        });
    }

    private void setLeftImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            imgLeft.setImageBitmap(image);
            model.setLeft(imageUri);
            model.setLeftLoaded(true);
            setEditImage(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open left image", Toast.LENGTH_LONG).show();
        }
    }

    private void setRightImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            imgRight.setImageBitmap(image);
            model.setRight(imageUri);
            model.setRightLoaded(true);
            setEditImage(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open right image", Toast.LENGTH_LONG).show();
        }
    }

    private void setEditImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            imgEdit.setImageBitmap(image);
            model.setEditImage(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open image to edit", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (model != null) {
            outState.putParcelable("LeftURI", model.getLeft());
            outState.putParcelable("RightURI", model.getRight());
            outState.putParcelable("Selected", model.getEditImage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // if we are here, everything processed successfully.
            if ((requestCode & PICK_GALLERY) == PICK_GALLERY) {
                // if we are here, we are hearing back from the image gallery.

                Uri imageUri = data.getData();

                if ((requestCode & SELECT_PHOTO_LEFT) == SELECT_PHOTO_LEFT) {
                    setLeftImage(imageUri);
                } else {
                    setRightImage(imageUri);
                }

            } else if ((requestCode & PICK_CAMERA) == PICK_CAMERA) {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }

                Uri imageUri = Uri.fromFile(f);
                if ((requestCode & SELECT_PHOTO_LEFT) == SELECT_PHOTO_LEFT) {
                    setLeftImage(imageUri);
                } else {
                    setRightImage(imageUri);
                }
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
                    File f = null;
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

    @NonNull
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
