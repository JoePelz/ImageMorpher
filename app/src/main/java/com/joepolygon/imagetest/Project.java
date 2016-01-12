package com.joepolygon.imagetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
    private String  projectName;
    private Bitmap  leftImage;
    private Bitmap  rightImage;
    private int     imgToEdit;
    private boolean isLeftLoaded;
    private boolean isRightLoaded;
    private final Context appContext;
    private ArrayList<Line> leftLines;
    private ArrayList<Line> rightLines;
    private int selectedLineIndex;
    private boolean bImagesDirty;

    ArrayList<ProjectUpdateListener> listeners = new ArrayList<ProjectUpdateListener>();

    public Project(Context app) {
        appContext = app;
        leftLines = new ArrayList<Line>();
        rightLines = new ArrayList<Line>();
        selectedLineIndex = -1;
        openProject("default");
    }

    public boolean saveProject(String name) {
        String oldName = projectName;
        projectName = name;
        boolean newName = projectName.equals(name);

        File f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName);
        if (f.isDirectory()) {
            Log.v("Project", "Project directory exists. Replacing.");
        } else {
            if (f.mkdir()) {
                Log.v("Project", "Project directory created successfully.");
            } else {
                Log.v("Project", "Failed to create project directory.");
                projectName = oldName;
                Toast.makeText(appContext, "Failed. Reverting to " + oldName, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        if (newName || bImagesDirty) {
            exportImages();
        }
        f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name + File.separator + "data.prj");
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f))) {
            saveToFile(os);
            importImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean openProject(String name) {
        File f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name);
        if (!f.isDirectory()) {
            return false;
        }
        projectName = name;
        f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name + File.separator + "data.prj");
        if (f.isFile()) {
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(f))) {
                importImages();
                loadFromFile(is);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        fireUpdate();
        return true;
    }

    public String getProject() {
        return projectName;
    }

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
        try (InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri)) {
            Bitmap temp = BitmapFactory.decodeStream(inputStream);
            if (temp != null) {
                int size = minSide(temp);
                leftImage = ThumbnailUtils.extractThumbnail(temp, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
        } catch (IOException e) {
            Log.v("Project", "setLeft: couldn't close inputStream??");
            e.printStackTrace();
        }
        bImagesDirty = true;
    }

    public void setRight(Uri imageUri) throws FileNotFoundException {
        try (InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri)) {
            Bitmap temp = BitmapFactory.decodeStream(inputStream);
            if (temp != null) {
                int size = minSide(temp);
                rightImage = ThumbnailUtils.extractThumbnail(temp, size, size, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
        } catch (IOException e) {
            Log.v("Project", "setRight: couldn't close inputStream??");
            e.printStackTrace();
        }
        bImagesDirty = true;
    }

    public void importImages() {
        File f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "rightImage.jpg");
        try (InputStream inputStream = new FileInputStream(f)) {
            rightImage = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.v("Project", "importImages suffered a FileNotFound exception");
            Log.v("Project", "importImagesRight couldn't open " + f.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("Project", "importImages suffered an IO exception");
            e.printStackTrace();
        }

        f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "leftImage.jpg");
        try (InputStream inputStream = new FileInputStream(f)) {
            leftImage = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.v("Project", "importImages suffered a FileNotFound exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("Project", "importImages suffered an IO exception");
            e.printStackTrace();
        }
        bImagesDirty = false;
    }

    public void exportImages() {
        File f;
        if (leftImage != null) {
            f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "leftImage.jpg");
            try (OutputStream stream = new FileOutputStream(f)) {
                leftImage.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                Log.v("Project", "exportImages: Wrote out the file leftImage.jpg to " + f.getAbsolutePath());
            } catch (IOException e) {
                Log.v("Project", "exportImages: Couldn't save leftImage.jpg. ");
                e.printStackTrace();
            }
        }
        if (rightImage != null) {
            f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "rightImage.jpg");
            try (OutputStream stream = new FileOutputStream(f)) {
                rightImage.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                Log.v("Project", "exportImages: Wrote out the file rightImage.jpg to " + f.getAbsolutePath());
            } catch (IOException e) {
                Log.v("Project", "exportImages: Couldn't save rightImage.jpg. ");
                e.printStackTrace();
            }
        }
        bImagesDirty = false;
    }

    private int minSide(Bitmap srcBmp) {
        return (srcBmp.getWidth() <= srcBmp.getHeight()) ? srcBmp.getWidth() : srcBmp.getHeight();
    }

    public void loadState(Bundle map) {
        String temp;
        if (map != null) {
            projectName = map.getString("projName");
            imgToEdit = map.getInt("imgEdit");
            selectedLineIndex = map.getInt("controlLineSelection");
            leftLines = (ArrayList<Line>) map.getSerializable("controlLinesLeft");
            rightLines = (ArrayList<Line>) map.getSerializable("controlLinesRight");
        }
        importImages();

        return;
    }

    public void saveState(Bundle map) {
        map.putString("projName", projectName);
        map.putInt("imgEdit", imgToEdit);
        map.putInt("controlLineSelection", selectedLineIndex);
        map.putSerializable("controlLinesLeft", leftLines);
        map.putSerializable("controlLinesRight", rightLines);
    }


    private void saveToFile(ObjectOutputStream os) {
        SaveObject data = new SaveObject();
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
    private boolean loadFromFile(ObjectInputStream is) {
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

        selectedLineIndex = data.selection;
        imgToEdit = data.imgEdit;
        leftLines = data.lLines;
        rightLines = data.rLines;
        return true;
    }
}
