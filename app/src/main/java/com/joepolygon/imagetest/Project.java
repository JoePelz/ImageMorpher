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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    private boolean isLeftLoaded = false;
    private boolean isRightLoaded = false;
    private final Context appContext;
    private ArrayList<Line> leftLines;
    private ArrayList<Line> rightLines;
    private int selectedLineIndex;
    private boolean bImagesDirty;

    private final ArrayList<ProjectUpdateListener> listeners = new ArrayList<>();

    public Project(Context app) {
        appContext = app;
        leftLines = new ArrayList<>();
        rightLines = new ArrayList<>();
        selectedLineIndex = -1;

        projectName = readProjectName(app);
        if (projectName == null) {
            projectName = "default";
        }
        openProject(projectName);
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
        if (newName && !writeProjectName(appContext, projectName)) {
            Toast.makeText(appContext, "Failed to save project name", Toast.LENGTH_LONG).show();
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

    public static boolean writeProjectName(Context ctx, String name) {
        File f = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "proj.txt");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
            bw.write(name);
        } catch (FileNotFoundException e) {
            Log.e("Project", "FNFE: couldn't write new project name.");
            return false;
        } catch (IOException e) {
            Log.e("Project", "IOE: couldn't write new project name.");
            return false;
        }
        return true;
    }

    public static String readProjectName(Context ctx) {
        File f = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "proj.txt");
        if (f.canRead()) {
            try ( BufferedReader isr = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
                return isr.readLine();
            } catch (FileNotFoundException e) {
                Log.e("Project", "FNFE: couldn't read last project name.");
                return null;
            } catch (IOException e) {
                Log.e("Project", "IOE: couldn't read last project name.");
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean openProject(String name) {
        boolean result = false;
        File f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name);
        if (!f.isDirectory()) {
            f.mkdir();
        }
        projectName = name;
        f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name + File.separator + "data.prj");
        if (f.isFile()) {
            Log.v("Project", "project directory opened.");
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(f))) {
                Log.v("Project", "Importing images...");
                importImages();
                Log.v("Project", "Importing lines...");
                loadFromFile(is);
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.v("Project", "Project " + name + " doesn't exist. Creating.");
            resetProject();
            result = true;
        }
        fireUpdate();
        return result;
    }

    public void resetProject() {
        leftImage = null;
        rightImage = null;
        setLoaded(IMG_LEFT, false);
        setLoaded(IMG_RIGHT, false);
        leftLines = new ArrayList<>();
        rightLines = new ArrayList<>();
        selectedLineIndex = -1;
        imgToEdit = IMG_LEFT;
        bImagesDirty = true;
        fireUpdate();
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

    public void setLoaded(int image, boolean loaded) {
        if (image == IMG_LEFT) {
            isLeftLoaded = loaded;
        } else {
            isRightLoaded = loaded;
        }
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

    public void setLeft(Uri imageUri) {
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

    public void setRight(Uri imageUri) {
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
        rightImage = null;
        if (f.canRead()) {
            try (InputStream inputStream = new FileInputStream(f)) {
                rightImage = BitmapFactory.decodeStream(inputStream);
                setLoaded(IMG_RIGHT, true);
            } catch (FileNotFoundException e) {
                Log.v("Project", "importImages suffered a FileNotFound exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.v("Project", "importImages suffered an IO exception");
                e.printStackTrace();
            }
        }

        f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), projectName + File.separator + "leftImage.jpg");
        leftImage = null;
        if (f.canRead()) {
            try (InputStream inputStream = new FileInputStream(f)) {
                leftImage = BitmapFactory.decodeStream(inputStream);
                setLoaded(IMG_LEFT, true);
            } catch (FileNotFoundException e) {
                Log.v("Project", "importImages suffered a FileNotFound exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.v("Project", "importImages suffered an IO exception");
                e.printStackTrace();
            }
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
        if (map != null) {
            projectName = map.getString("projName");
            imgToEdit = map.getInt("imgEdit");
            selectedLineIndex = map.getInt("controlLineSelection");
            Serializable sr = map.getSerializable("controlLinesLeft");
            if (sr instanceof ArrayList)
                leftLines = (ArrayList<Line>) sr;
            sr = map.getSerializable("controlLinesRight");
            if (sr instanceof ArrayList)
                rightLines = (ArrayList<Line>) sr;
        }
        importImages();
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
            Log.e("Project", "loadFromFile encountered a ClassNotFoundException.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Project", "loadFromFile encountered a IOException.");
            e.printStackTrace();
        }
        if (data == null) {
            selectedLineIndex = -1;
            imgToEdit = IMG_LEFT;
            leftLines = new ArrayList<>();
            rightLines = new ArrayList<>();
            return false;
        }

        selectedLineIndex = data.selection;
        imgToEdit = data.imgEdit;
        leftLines = data.lLines;
        rightLines = data.rLines;
        return true;
    }
}
