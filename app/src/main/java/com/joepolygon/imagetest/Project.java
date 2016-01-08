package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Holds program state in a single object.
 * Created by Joe on 2016-01-05.
 */

public class Project {
    public static final int IMG_LEFT = 0;
    public static final int IMG_RIGHT = 1;

    private Bitmap leftImage;
    private Uri    leftImageUri;
    private Bitmap rightImage;
    private Uri    rightImageUri;
    private int imgToEdit;
    //TODO: store image here too.  Add getRightImg and getLeftImg returning Bitmap
    private boolean isLeftLoaded;
    private boolean isRightLoaded;
    private Context appContext;

    public void setLeftLoaded(boolean isLoaded) { isLeftLoaded = isLoaded; }
    public void setRightLoaded(boolean isLoaded) { isRightLoaded = isLoaded; }
    public boolean isLeftLoaded() { return isLeftLoaded; }
    public boolean isRightLoaded() { return isRightLoaded; }


    public Project(Context app) {
        appContext = app;
    }

    public int getEditImage() {
        return imgToEdit;
    }

    public Bitmap getImage(int image) {
        if (image == IMG_LEFT) {
            return leftImage;
        } else if (image == IMG_RIGHT) {
            return rightImage;
        }
        return null;
    }

    public void setEditImage(int image) {
        imgToEdit = image;
    }

    public void setLeft(Uri imageUri) throws FileNotFoundException {
        InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri);
        leftImage = BitmapFactory.decodeStream(inputStream);
        leftImageUri = imageUri;
    }

    public void setRight(Uri imageUri) throws FileNotFoundException {
        InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri);
        rightImage = BitmapFactory.decodeStream(inputStream);
        rightImageUri = imageUri;
    }

    public boolean loadState(Bundle map) {
        if (map == null) {
            return false;
        }
        leftImageUri = map.getParcelable("imgLeftUri");
        rightImageUri = map.getParcelable("imgRightUri");

        if (leftImageUri != null) {
            try {
                setLeft(leftImageUri);
            } catch (FileNotFoundException e) {
                Log.e("Project", "Load State: cannot load left image.");
                e.printStackTrace();
            }
        }
        if (rightImageUri != null) {
            try {
                setRight(rightImageUri);
            } catch (FileNotFoundException e) {
                Log.e("Project", "Load State: cannot load left image.");
                e.printStackTrace();
            }
        }
        imgToEdit = map.getInt("imgEdit");
        return true;
    }

    public void saveState(Bundle map) {
        map.putParcelable("imgLeftUri", leftImageUri);
        map.putParcelable("imgRightUri", rightImageUri);
        map.putInt("imgEdit", imgToEdit);
    }

}
