package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Holds program state in a single object.
 * Created by Joe on 2016-01-05.
 */

public class Project {
    public static final int IMG_LEFT = 0;
    public static final int IMG_RIGHT = 1;
    public static final int IMG_EDIT = 2;

    //Images for display and manipulation
    private Bitmap  leftImage;
    private Uri     leftImageUri;
    private Bitmap  rightImage;
    private Uri     rightImageUri;
    private int     imgToEdit;
    private boolean isLeftLoaded;
    private boolean isRightLoaded;
    private final Context appContext;
    private ArrayList<Line> leftLines;
    private ArrayList<Line> rightLines;
    private int selectedLineIndex;

    ArrayList<ProjectUpdateListener> listeners = new ArrayList<ProjectUpdateListener>();

    public void addUpdateListener(ProjectUpdateListener listener) {
        listeners.add(listener);
    }

    public void fireUpdate() {
        for (ProjectUpdateListener listener : listeners)
        {
            listener.onProjectUpdate();
        }
    }

    public void setLoaded(int image, boolean isLoaded) {
        if (image == IMG_LEFT) {
            isLeftLoaded = isLoaded;
        } else {
            isRightLoaded = isLoaded;
        }
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

    public ArrayList<Line> getLines() {
        return imgToEdit == IMG_LEFT ? leftLines : rightLines;
    }

    public boolean selectLine(Line line) {
        selectedLineIndex = line == null ? -1 : getLines().indexOf(line);
        return selectedLineIndex != -1;
    }

    public Line getSelectedLine() {
        if (selectedLineIndex == -1) return null;
        return (imgToEdit == IMG_LEFT ? leftLines : rightLines).get(selectedLineIndex);
    }

    public Line getSelectedLine(int src) {
        if (selectedLineIndex == -1) return null;
        return (src == IMG_LEFT ? leftLines : rightLines).get(selectedLineIndex);
    }

    public ArrayList<Line> getLines(int src) {
        return src == IMG_LEFT ? leftLines : rightLines;
    }

    public void addLine(Line l) {
        leftLines.add(l);
        rightLines.add(l.copy());
        selectedLineIndex = leftLines.size() - 1;
        fireUpdate();
    }

    public boolean removeLine(Line l) {
        int index;
        if (imgToEdit == IMG_LEFT) {
            index = leftLines.indexOf(l);
        } else {
            index = rightLines.indexOf(l);
        }
        if (index == -1) {
            return false;
        }
        leftLines.remove(index);
        rightLines.remove(index);
        fireUpdate();
        return true;
    }

    public boolean removeSelected() {
        if (selectedLineIndex == -1) {
            return false;
        }
        try {
            leftLines.remove(selectedLineIndex);
            rightLines.remove(selectedLineIndex);
        } catch (IndexOutOfBoundsException e) {
            Log.e("Project.removeSelected", "selectedLineIndex was out of bounds.");
            return false;
        } finally {
            selectedLineIndex = -1;
        }
        fireUpdate();
        return true;
    }

    public Project(Context app) {
        appContext = app;
        leftLines = new ArrayList<Line>();
        rightLines = new ArrayList<Line>();
        selectedLineIndex = -1;
    }

    public Bitmap getImage(int image) {
        if (image == IMG_EDIT)
            image = imgToEdit;
        return image == IMG_LEFT ? leftImage : rightImage;
    }

    public int getEditImage() {
        return imgToEdit;
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
        String temp;
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
                Log.e("Project", "Load State: cannot load right image.");
                e.printStackTrace();
            }
        }
        imgToEdit = map.getInt("imgEdit");
        selectedLineIndex = map.getInt("controlLineSelection");
        leftLines = (ArrayList<Line>) map.getSerializable("controlLinesLeft");
        rightLines = (ArrayList<Line>) map.getSerializable("controlLinesRight");
    }

    public void saveState(Bundle map) {
        map.putParcelable("imgLeftUri", leftImageUri);
        map.putParcelable("imgRightUri", rightImageUri);
        map.putInt("imgEdit", imgToEdit);
        map.putInt("controlLineSelection", selectedLineIndex);
        map.putSerializable("controlLinesLeft", leftLines);
        map.putSerializable("controlLinesRight", rightLines);
    }
    public void saveToFile(ObjectOutputStream os) {
        SaveObject data = new SaveObject();
        data.leftUriEncoded = leftImageUri.toString();
        data.rightUriEncoded = rightImageUri.toString();
        data.imgEdit = imgToEdit;
        data.selection = selectedLineIndex;
        data.lLines = leftLines;
        data.rLines = rightLines;
        try {
            os.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean loadFromFile(ObjectInputStream is) {
        SaveObject data = null;
        try {
            data = (SaveObject) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) {
            return false;
        }
        leftImageUri = Uri.parse(data.leftUriEncoded);
        rightImageUri = Uri.parse(data.rightUriEncoded);

        if (leftImageUri != null) {
            try {
                setLeft(leftImageUri);
            } catch (FileNotFoundException e) {
                Log.e("Project", "Load From File: cannot load left image.");
                e.printStackTrace();
            }
        }
        if (rightImageUri != null) {
            try {
                setRight(rightImageUri);
            } catch (FileNotFoundException e) {
                Log.e("Project", "Load From File: cannot load right image.");
                e.printStackTrace();
            }
        }

        selectedLineIndex = data.selection;
        imgToEdit = data.imgEdit;
        leftLines = data.lLines;
        rightLines = data.rLines;
        return true;
    }
}
