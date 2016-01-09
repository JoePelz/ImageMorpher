package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Holds program state in a single object.
 * Created by Joe on 2016-01-05.
 */

public class Project {
    public static final int IMG_LEFT = 0;
    public static final int IMG_RIGHT = 1;

    //Images for display and manipulation
    private Bitmap  leftImage;
    private Uri     leftImageUri;
    private Bitmap  rightImage;
    private Uri     rightImageUri;
    private int     imgToEdit;
    private boolean isLeftLoaded;
    private boolean isRightLoaded;
    private final Context appContext;

    //Lines for manipulating images

    public void setLeftLoaded(boolean isLoaded) {
        isLeftLoaded = isLoaded;
    }
    public void setRightLoaded(boolean isLoaded) {
        isRightLoaded = isLoaded;
    }
    public boolean isLeftLoaded() {
        return isLeftLoaded;
    }
    public boolean isRightLoaded() {
        return isRightLoaded;
    }


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
        Bitmap temp = BitmapFactory.decodeStream(inputStream);
        if (temp != null) {
            int size = minSide(temp);
            leftImage = ThumbnailUtils.extractThumbnail(temp, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            leftImageUri = imageUri;
        }
    }

    public void setRight(Uri imageUri) throws FileNotFoundException {
        InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri);
        Bitmap temp = BitmapFactory.decodeStream(inputStream);
        if (temp != null) {
            int size = minSide(temp);
            rightImage = ThumbnailUtils.extractThumbnail(temp, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            rightImageUri = imageUri;
        }
    }

    private int minSide(Bitmap srcBmp) {
        return (srcBmp.getWidth() <= srcBmp.getHeight()) ? srcBmp.getWidth() : srcBmp.getHeight();
    }

    public void loadState(Bundle map) {
        if (map == null) {
            return;
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
    }

    public void saveState(Bundle map) {
        map.putParcelable("imgLeftUri", leftImageUri);
        map.putParcelable("imgRightUri", rightImageUri);
        map.putInt("imgEdit", imgToEdit);
    }

}
