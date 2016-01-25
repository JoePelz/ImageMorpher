package com.joepolygon.warpertoy;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by Joe on 2016-01-13.
 */
public class VectorTest {
    @Test
    public void Vector_lengthSimple() {
        Vector v = new Vector(4, 3);
        assertThat(v.length(), is(5.0f));
        v = new Vector(-4, 3);
        assertThat(v.length(), is(5.0f));
        v = new Vector(4, -3);
        assertThat(v.length(), is(5.0f));
        v = new Vector(-4, -3);
        assertThat(v.length(), is(5.0f));
    }

    @Test
    public void Vector_lengthSquaredSimple() {
        Vector v = new Vector(4, 3);
        assertThat(v.lengthSquared(), is(25));
        v = new Vector(-4, 3);
        assertThat(v.lengthSquared(), is(25));
        v = new Vector(4, -3);
        assertThat(v.lengthSquared(), is(25));
        v = new Vector(-4, -3);
        assertThat(v.lengthSquared(), is(25));
    }

    @Test
    public void Vector_normal() {
        Vector v = new Vector(1, 2);
        assertEquals(v.normal(), new Vector(-2, 1));
    }

    @Test
    public void Vector_scale() {
        Vector v = new Vector(1, 2);
        v.scale(5);
        assertThat(v.x, is(5));
        assertThat(v.y, is(10));
        v = new Vector(1, 2);
        v.scale(5.0f);
        assertThat(v.x, is(5));
        assertThat(v.y, is(10));
    }

    @Test
    public void Vector_dotProduct() {
        Vector a = new Vector( 2,  3);
        Vector b = new Vector( 1, 10);
        assertThat(a.dotProduct(b), is(32));
        assertThat(b.dotProduct(a), is(32));

        VectorF af = new VectorF( 2.0f,  3.0f);
        VectorF bf = new VectorF( 1.0f, 10.0f);
        assertThat(a.dotProduct(bf), is(32.0f));
        assertThat(b.dotProduct(af), is(32.0f));
    }

    @Test
    public void Vector_projection() {
        Vector a = new Vector(6, 8);
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

        Vector u = new Vector(2, 1);
        v = new Vector(-3, 4);
        VectorF resultUV = new VectorF(6.0f/25.0f, -8.0f/25.0f);
        assertThat(u.projection(v).equals(resultUV), is(true));
        assertEquals(resultUV, u.projection(v));
    }

    @Test
    public void Vector_projectionF() {
        Vector a = new Vector(6, 8);
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

        Vector u = new Vector(2, 1);
        v = new VectorF(-3, 4);
        VectorF resultUV = new VectorF(6.0f/25.0f, -8.0f/25.0f);
        assertThat(u.projection(v).equals(resultUV), is(true));
        assertEquals(resultUV, u.projection(v));
    }

    @Test
    public void Vector_projectionLength() {
        Vector a = new Vector(6, 8);
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
    public void Vector_projectionLengthF() {
        Vector a = new Vector(6, 8);
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
    public void Vector_distToPoint() {
        Vector h = new Vector(3, 0);
        Vector v = new Vector(0, -1);
        Vector v2 = new Vector(0, 1000);
        Vector d = new Vector(-6, -8);
        float x = 12;
        float y = 16;

        assertThat(h.distToPoint(x, y), is(-16.0f));
        assertThat(v.distToPoint(x, y), is(-12.0f));
        assertThat(v2.distToPoint(x, y), is(12.0f));
        assertThat(d.distToPoint(x, y), is(0.0f));
    }
}
