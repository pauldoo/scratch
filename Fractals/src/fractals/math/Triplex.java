/*
    Copyright (c) 2009, 2010, 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and/or distribute this software for any
    purpose with or without fee is hereby granted, provided that the above
    copyright notice and this permission notice appear in all copies.

    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package fractals.math;

import fractals.Utilities;

public final class Triplex
{
    public final double x;
    public final double y;
    public final double z;

    public Triplex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
        Asserts that no values are NaN, then returns 'this'.
    */
    Triplex assertNotNaN()
    {
        Utilities.assertNotNaN(x);
        Utilities.assertNotNaN(y);
        Utilities.assertNotNaN(z);
        return this;
    }

    public static Triplex add(Triplex a, Triplex b)
    {
        return new Triplex(
                a.x + b.x,
                a.y + b.y,
                a.z + b.z);
    }

    public static Triplex subtract(Triplex a, Triplex b)
    {
        return new Triplex(
                a.x - b.x,
                a.y - b.y,
                a.z - b.z);
    }

    public static Triplex multiply(Triplex a, double b)
    {
        return new Triplex(
                a.x * b,
                a.y * b,
                a.z * b);
    }

    public final double magnitudeSquared()
    {
        return x*x + y*y + z*z;
    }

    public final double magnitude()
    {
        return Math.sqrt(magnitudeSquared());
    }

    public final Triplex negate()
    {
        return new Triplex(-x, -y, -z);
    }

    public static Triplex normalize(Triplex a)
    {
        final double mag = a.magnitude();
        return new Triplex(
                a.x / mag,
                a.y / mag,
                a.z / mag);
    }

    public static Triplex crossProduct(Triplex a, Triplex b)
    {
        return new Triplex(
                a.y * b.z - b.y * a.z,
                a.z * b.x - b.z * a.x,
                a.x * b.y - b.x * a.y);
    }

    public static double dotProduct(Triplex a, Triplex b)
    {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public Triplex normalize()
    {
        return normalize(this);
    }

    public boolean approximatelyEquals(Triplex other, double tollerance) {
        return subtract(this, other).magnitudeSquared() <= (tollerance*tollerance);
    }
    public boolean approximatelyEquals(Triplex other) {
        return approximatelyEquals(other, 1e-6);
    }
}
