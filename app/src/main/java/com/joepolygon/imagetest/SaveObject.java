package com.joepolygon.imagetest;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Joe on 2016-01-09.
 */
public class SaveObject implements Serializable {
    public String projectName;
    public String rightUriEncoded;
    public int imgEdit;
    public int selection;
    public ArrayList<Line> lLines;
    public ArrayList<Line> rLines;
}
