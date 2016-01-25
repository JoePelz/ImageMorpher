package com.joepolygon.warpertoy;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Small class that represents saved project information, mostly just the lines.
 * TODO: get rid of this, its stupid.
 * Created by Joe on 2016-01-09.
 */
class SaveObject implements Serializable {
    public int imgEdit;
    public int selection;
    public ArrayList<Line> lLines;
    public ArrayList<Line> rLines;
}
