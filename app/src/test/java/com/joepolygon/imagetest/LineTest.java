package com.joepolygon.imagetest;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * For testing the line class
 * Created by Joe on 2016-01-17.
 */
public class LineTest {

    @Test
    public void testDistanceFromLine() throws Exception {
        Line l = new Line(5, 4, 10, 4);

        assertEquals(5.0f, l.distanceFromLine(2, 8), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(5, 8), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(6, 0), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(7, 8), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(8, 0), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(9, 8), 0.0001);
        assertEquals(4.0f, l.distanceFromLine(10, 0), 0.0001);
        assertEquals(5.0f, l.distanceFromLine(13, 0), 0.0001);
    }
}