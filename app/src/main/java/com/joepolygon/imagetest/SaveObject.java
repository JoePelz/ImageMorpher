package com.joepolygon.imagetest;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Small class that represents saved project information, mostly just the lines.
 * Created by Joe on 2016-01-09.
 */
public class SaveObject implements Serializable {
    public int imgEdit;
    public int selection;
    public ArrayList<Line> lLines;
    public ArrayList<Line> rLines;
}
