package com.joepolygon.imagetest;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Joe on 2016-01-05.
 */

public class Project {
    private Uri leftImage;
    private Uri rightImage;
    private Uri imgToEdit;
    //TODO: store image here too.  Add getRightImg and getLeftImg returning Bitmap
    private boolean isLeftLoaded;
    private boolean isRightLoaded;

    public void setLeftLoaded(boolean isLoaded) { isLeftLoaded = isLoaded; }
    public void setRightLoaded(boolean isLoaded) { isRightLoaded = isLoaded; }
    public boolean isLeftLoaded() { return isLeftLoaded; }
    public boolean isRightLoaded() { return isRightLoaded; }

    public void setEditImage(Uri imageUri) {
        imgToEdit = imageUri;
    }

    public Uri getEditImage() {
        return imgToEdit;
    }

    public Uri getLeft() {
        return leftImage;
    }

    public Uri getRight() {
        return rightImage;
    }

    public void setLeft(Uri newLeft) {
        leftImage = newLeft;
    }

    public void setRight(Uri newRight) {
        rightImage = newRight;
    }

}
