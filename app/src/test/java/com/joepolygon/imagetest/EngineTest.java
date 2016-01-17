package com.joepolygon.imagetest;

import android.content.Context;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EngineTest {
    @Mock
    Context mMockContext;

    @Test
    public void Engine_testWeight() {
        Engine e = new Engine(mMockContext, "default", 10, 0.1f, 2.0f, 0.0f, 512, 512);
        assertEquals(Math.pow(1 / 19.1, 2), e.weight(100, 19), 0.00001);
        assertEquals(Math.pow(1 /  5.1, 2), e.weight(100,  5), 0.00001);
        Engine f = new Engine(mMockContext, "default", 10, 0.1f, 2.0f, 0.5f, 512, 512);
        assertEquals(Math.pow(3 / 12.1, 2), f.weight(9, 12), 0.00001);
    }

    @Test
    public void Engine_testEquivalentPoint() {
        Engine e = new Engine(mMockContext, "default", 10, 0.1f, 2.0f, 0.0f, 512, 512);
        Line A = new Line(5, -2, 5, 2);
        Line B = new Line(7, -1, 11, -1);
        Point src = new Point(3, 1);
        PointF dst = new PointF(10, 1);

        assertEquals(dst, e.findEquivalentPoint(A, B, src.x, src.y));
        B = new Line(7, -1, 15, -1);
        dst = new PointF(13, 1);
        assertEquals(dst, e.findEquivalentPoint(A, B, src.x, src.y));
        B = new Line(15, -1, 7, -1);
        dst = new PointF(9, -3);
        assertEquals(dst, e.findEquivalentPoint(A, B, src.x, src.y));
    }

    @Test
    public void Engine_testVectorForPoint() {
        Engine e = new Engine(mMockContext, "default", 10, 0.1f, 2.0f, 0.0f, 512, 512);
        ArrayList<Line> As = new ArrayList<>();
        ArrayList<Line> Bs = new ArrayList<>();
        As.add(new Line(1, 2, 1, 6));
        As.add(new Line(2, 1, 6, 1));
        Point src = new Point(3, 3);
        Bs.add(new Line(5, 7, 1, 7));
        Bs.add(new Line(6, 8, 6, 12));
        //Point dst = new Point(4, 9);
        Point offset = new Point(1, 6);

        Point result = e.vectorForPoint(As, Bs, src.x, src.y);
        assertEquals(offset, result);


        Bs.clear();
        Bs.add(new Line(5, 5, 1, 5)); //moved y by -2
        Bs.add(new Line(6, 8, 6, 12));
        //dst = new Point(4, 8);
        offset = new Point(1, 5); //adjusted offset by -1
        result = e.vectorForPoint(As, Bs, src.x, src.y);
        assertEquals(offset, result);
    }

    @Test
    public void Engine_testColorBlend() {
        Engine e = new Engine(mMockContext, "default", 10, 0.1f, 2.0f, 0.0f, 512, 512);
        int a = 0x000000;
        int b = 0x888888;
        assertEquals(0x444444, e.blendColors(a, b, 0.5f));
        int c = 0x123456;
        int d = 0x345678;
        assertEquals(0x234567, e.blendColors(c, d, 0.5f));
        int f = 0x113355;
        int g = 0x557799;
        assertEquals(0x113355, e.blendColors(f, g, 0.00f));
        assertEquals(0x224466, e.blendColors(f, g, 0.25f));
        assertEquals(0x335577, e.blendColors(f, g, 0.50f));
        assertEquals(0x446688, e.blendColors(f, g, 0.75f));
        assertEquals(0x557799, e.blendColors(f, g, 1.00f));
    }
}
