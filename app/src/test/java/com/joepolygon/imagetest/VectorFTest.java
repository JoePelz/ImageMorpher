package com.joepolygon.imagetest;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by Joe on 2016-01-13.
 */
public class VectorFTest {
    @Test
    public void VectorF_lengthSimple() {
        VectorF v = new VectorF(4, 3);
        assertThat(v.length(), is(5.0f));
        v = new VectorF(-4, 3);
        assertThat(v.length(), is(5.0f));
        v = new VectorF(4, -3);
        assertThat(v.length(), is(5.0f));
        v = new VectorF(-4, -3);
        assertThat(v.length(), is(5.0f));
    }

    @Test
    public void VectorF_lengthSquaredSimple() {
        VectorF v = new VectorF(4, 3);
        assertThat(v.lengthSquared(), is(25.0f));
        v = new VectorF(-4, 3);
        assertThat(v.lengthSquared(), is(25.0f));
        v = new VectorF(4, -3);
        assertThat(v.lengthSquared(), is(25.0f));
        v = new VectorF(-4, -3);
        assertThat(v.lengthSquared(), is(25.0f));
    }

    @Test
    public void VectorF_normal() {
        VectorF v = new VectorF(1, 2);
        assertEquals(v.normal(), new VectorF(-2, 1));
    }

    @Test
    public void VectorF_scale() {
        VectorF v = new VectorF(1, 2);
        v.scale(5);
        assertThat(v.x, is(5.0f));
        assertThat(v.y, is(10.0f));
        v = new VectorF(1, 2);
        v.scale(5.0f);
        assertThat(v.x, is(5.0f));
        assertThat(v.y, is(10.0f));
    }

    @Test
    public void VectorF_dotProduct() {
        VectorF af = new VectorF( 2.0f,  3.0f);
        VectorF bf = new VectorF( 1.0f, 10.0f);
        assertThat(af.dotProduct(bf), is(32.0f));
        assertThat(bf.dotProduct(af), is(32.0f));

        VectorF a = new VectorF( 2,  3);
        VectorF b = new VectorF( 1, 10);
        assertThat(af.dotProduct(b), is(32.0f));
        assertThat(bf.dotProduct(a), is(32.0f));
    }

    @Test
    public void VectorF_projection() {
        VectorF a = new VectorF(6, 8);
        Vector h = new Vector(3, 0);
        Vector v = new Vector(0, -1);
        Vector d = new Vector(-6, -8);

        VectorF resultAH = new VectorF(6, 0);
        VectorF resultAV = new VectorF(0, 8);
        VectorF resultAD = new VectorF(6, 8);
        assertEquals(resultAH, a.projection(h));
        assertEquals(resultAV, a.projection(v));
        assertEquals(resultAD, a.projection(d));
        assertNotEquals(resultAV, a.projection(d));

        VectorF u = new VectorF(2, 1);
        v = new Vector(-3, 4);
        VectorF resultUV = new VectorF(6.0f/25.0f, -8.0f/25.0f);
        assertThat(u.projection(v).equals(resultUV), is(true));
        assertEquals(resultUV, u.projection(v));
    }

    @Test
    public void VectorF_projectionF() {
        VectorF a = new VectorF(6, 8);
        VectorF h = new VectorF(3, 0);
        VectorF v = new VectorF(0, -1);
        VectorF d = new VectorF(-6, -8);

        VectorF resultAH = new VectorF(6, 0);
        VectorF resultAV = new VectorF(0, 8);
        VectorF resultAD = new VectorF(6, 8);
        assertEquals(resultAH, a.projection(h));
        assertEquals(resultAV, a.projection(v));
        assertEquals(resultAD, a.projection(d));
        assertNotEquals(resultAV, a.projection(d));

        VectorF u = new VectorF(2, 1);
        v = new VectorF(-3, 4);
        VectorF resultUV = new VectorF(6.0f/25.0f, -8.0f/25.0f);
        assertThat(u.projection(v).equals(resultUV), is(true));
        assertEquals(resultUV, u.projection(v));
    }

    @Test
    public void VectorF_projectionLength() {
        VectorF a = new VectorF(6, 8);
        Vector h = new Vector(3, 0);
        Vector v = new Vector(0, -1);
        Vector d = new Vector(-6, -8);

        float ah = 6;
        float av = 8;
        float ad = 10;
        assertThat(a.projectionLength(h), is(6.0f));
        assertThat(a.projectionLength(v), is(-8.0f));
        assertThat(a.projectionLength(d), is(-10.0f));
    }

    @Test
    public void VectorF_projectionLengthF() {
        VectorF a = new VectorF(6, 8);
        VectorF h = new VectorF(3.0f, 0.0f);
        VectorF v = new VectorF(0.0f, -1.0f);
        VectorF d = new VectorF(-6.0f, -8.0f);

        float ah = 6;
        float av = 8;
        float ad = 10;
        assertThat(a.projectionLength(h), is(6.0f));
        assertThat(a.projectionLength(v), is(-8.0f));
        assertThat(a.projectionLength(d), is(-10.0f));
    }

    @Test
    public void VectorF_distToPoint() {
        VectorF h = new VectorF(3, 0);
        VectorF v = new VectorF(0, -1);
        VectorF v2 = new VectorF(0, 1000);
        VectorF d = new VectorF(-6, -8);
        float x = 12;
        float y = 16;

        assertThat(h.distToPoint(x, y), is(-16.0f));
        assertThat(v.distToPoint(x, y), is(-12.0f));
        assertThat(v2.distToPoint(x, y), is(12.0f));
        assertThat(d.distToPoint(x, y), is(0.0f));
    }

    @Test
    public void VectorF_distanceAlong() {
        VectorF a = new VectorF(100, 0);
        assertEquals(0.25f, a.distanceAlong(25,  36, 0, 0), 0.00001);
        assertEquals(0.75f, a.distanceAlong(75, -36, 0, 0), 0.00001);
        assertEquals(-0.75f, a.distanceAlong(25,  36, 100, 0), 0.00001);
        assertEquals(-0.25f, a.distanceAlong(75, -36, 100, 0), 0.00001);

        VectorF b = new VectorF(0, 100);
        assertEquals(0.36f, b.distanceAlong(25,  36, 0, 0), 0.00001);
        assertEquals(-0.72f, b.distanceAlong(75, -72, 0, 0), 0.00001);
        assertEquals(-0.28f, b.distanceAlong(25,  72, 0, 100), 0.00001);
        assertEquals(-1.36f, b.distanceAlong(75, -36, 0, 100), 0.00001);

        VectorF c = new VectorF(3, 3);
        assertEquals(0.5f, c.distanceAlong(0, 3, 0, 0), 0.00001);
        assertEquals(0.5f, c.distanceAlong(3, 0, 0, 0), 0.00001);
        assertEquals(-0.5f, c.distanceAlong(3, 0, 3, 3), 0.00001);
        assertEquals(-0.5f, c.distanceAlong(0, 3, 3, 3), 0.00001);
    }
}
