package com.joepolygon.warpertoy;

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
 * This project model handles the control line array,
 * which image is selected,
 * which line is selected,
 * and saving/loading state.
 */

class Project {
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

    /**
     * Constructor, loads last project if it exists.
     * @param app The running application
     */
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

    /**
     * Save the current project under the given name.
     * @param name The name of the project to save.
     * @return True if saving succeeded.
     */
    public boolean saveProject(String name) {
        String oldName = projectName;
        projectName = name;
        boolean newName = !projectName.equals(name);

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

    /**
     * Save the name of the current project so it can be restored later.
     * @param ctx The current application context.
     * @param name The project name to save.
     * @return True if saving succeeded.
     */
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

    /**
     * Determine the name of the current project, from a file.
     * @param ctx The app context to search within.
     * @return The name of the last project.
     */
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

    /**
     * Open a project based on it's name, or create it if it doesn't exist.
     * @param name The name of the project to open
     * @return True if opening succeeded.
     */
    public boolean openProject(String name) {
        boolean result = false;
        File f = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name);
        if (!f.isDirectory()) {
            if (!f.mkdir() && !f.isDirectory()) {
                Log.e("Project", "Could not create the project.");
                return false;
            }
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

    /**
     * Clear out project information to default values.
     */
    private void resetProject() {
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

    /**
     * @return The project's saved name
     */
    public String getProject() {
        return projectName;
    }

    /**
     * Track listeners, who want to be updated when the images are changed.
     * @param listener The listener to be triggered when images are changed.
     */
    public void addUpdateListener(ProjectUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Send update message to all registered listeners
     */
    public void fireUpdate() {
        for (ProjectUpdateListener listener : listeners)
        {
            listener.onProjectUpdate();
        }
    }

    /**
     * Set a boolean that the given image has been loaded.
     * @param image The image to change (IMG_LEFT, IMG_RIGHT)
     * @param loaded The state where TRUE means the image loaded successfully
     */
    public void setLoaded(int image, boolean loaded) {
        if (image == IMG_LEFT) {
            isLeftLoaded = loaded;
        } else {
            isRightLoaded = loaded;
        }
    }
    /** return true if left image is loaded. */
    public boolean isLeftLoaded() {
        return isLeftLoaded;
    }
    /** return true if right image is loaded. */
    public boolean isRightLoaded() {
        return isRightLoaded;
    }

    /** return the array of lines for the currently-being-edited image. */
    public ArrayList<Line> getLines() {
        return imgToEdit == IMG_LEFT ? leftLines : rightLines;
    }

    /**
     * Mark the given line as being selected.
     * @param line The line to select
     * @return True if line exists in the line array
     */
    public boolean selectLine(Line line) {
        selectedLineIndex = line == null ? -1 : getLines().indexOf(line);
        return selectedLineIndex != -1;
    }

    /** Get the currently selected line for the current image */
    public Line getSelectedLine() {
        if (selectedLineIndex == -1) return null;
        return (imgToEdit == IMG_LEFT ? leftLines : rightLines).get(selectedLineIndex);
    }

    /** Get the currently selected line, for the given IMG_LEFT or IMG_RIGHT */
    public Line getSelectedLine(int src) {
        if (selectedLineIndex == -1) return null;
        return (src == IMG_LEFT ? leftLines : rightLines).get(selectedLineIndex);
    }

    /** Get the lines for the give image (IMG_LEFT, IMG_RIGHT) */
    public ArrayList<Line> getLines(int src) {
        return src == IMG_LEFT ? leftLines : rightLines;
    }

    /** Add a new line to the control line arrays. */
    public void addLine(Line l) {
        leftLines.add(l);
        rightLines.add(l.copy());
        selectedLineIndex = leftLines.size() - 1;
        fireUpdate();
    }

    /** Remove the selected line from the control line arrays. */
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

    /** Get the Bitmap for the left or right image (IMG_LEFT, IMG_RIGHT) */
    public Bitmap getImage(int image) {
        if (image == IMG_EDIT)
            image = imgToEdit;
        return image == IMG_LEFT ? leftImage : rightImage;
    }

    /** Check which image is currently being edited. */
    public int getEditImage() {
        return imgToEdit;
    }

    /** Switch which image is currently being edited. */
    public void setEditImage(int image) {
        imgToEdit = image;
    }

    /** Set the left image based on an image URI. */
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

    /** Set the right image based on an image URI. */
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

    /** Given a set project, attempt to import the left and right images. */
    private void importImages() {
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

    /** Export the current images to the project folder. */
    private void exportImages() {
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

    /** return the length of the shortest side of the given bitmap. */
    private int minSide(Bitmap srcBmp) {
        return (srcBmp.getWidth() <= srcBmp.getHeight()) ? srcBmp.getWidth() : srcBmp.getHeight();
    }

    /** Load state from the given Bundle. */
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

    /** Save state to the given bundle. */
    public void saveState(Bundle map) {
        map.putString("projName", projectName);
        map.putInt("imgEdit", imgToEdit);
        map.putInt("controlLineSelection", selectedLineIndex);
        map.putSerializable("controlLinesLeft", leftLines);
        map.putSerializable("controlLinesRight", rightLines);
    }

    /** Save the state to an output stream. */
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
    /** Read the state from an input stream. */
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
