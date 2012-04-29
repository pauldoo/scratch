/*
    Copyright (c) 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

import fractals.math.Triplex;

/**
    Quaternion class.

    See: http://en.wikipedia.org/wiki/Quaternion

    Normalized quaternions (q = w + x*i + y*j + z*k, |q| = 1.0)
    are used in some contexts to represent rotations in 3D.
    In this usage the rotation axis is (x, y, z), and the angle of
    rotation is "a = 2*acos(w) = 2*asin((x^2+y^2+z^2)^0.5)".

    Using quaternions to represent rotations in this way allows
    quaternion multiplication to be used to compose rotations.
*/
public final class Quaternion implements Comparable<Quaternion>
{
    /**
        Real part.
    */
    public final double a;

    /**
        I part.
    */
    public final double b;

    /**
        J part.
    */
    public final double c;

    /**
        K part.
    */
    public final double d;

    public Quaternion(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static Quaternion createRotation(Triplex axis, double angle)
    {
        axis = Triplex.normalize(axis);
        return new Quaternion(
                Math.cos(angle / 2.0),
                Math.sin(angle / 2.0) * axis.x,
                Math.sin(angle / 2.0) * axis.y,
                Math.sin(angle / 2.0) * axis.z);
    }

    public final double magnitudeSquared()
    {
        return a*a + b*b + c*c + d*d;
    }

    public final double magnitude()
    {
        return Math.sqrt(magnitudeSquared());
    }

    public final Quaternion normalize()
    {
        final double magnitude = magnitude();
        return new Quaternion(
                a / magnitude,
                b / magnitude,
                c / magnitude,
                d / magnitude);
    }

    public final Quaternion negate()
    {
        return new Quaternion(-a, -b, -c, -d);
    }

    public static Quaternion multiply(Quaternion lhs, Quaternion rhs)
    {
        return new Quaternion(
                lhs.a*rhs.a - lhs.b*rhs.b - lhs.c*rhs.c - lhs.d*rhs.d,
                lhs.a*rhs.b + lhs.b*rhs.a + lhs.c*rhs.d - lhs.d*rhs.c,
                lhs.a*rhs.c - lhs.b*rhs.d + lhs.c*rhs.a + lhs.d*rhs.b,
                lhs.a*rhs.d + lhs.b*rhs.c - lhs.c*rhs.b + lhs.d*rhs.a);
    }

    public static Quaternion subtract(Quaternion lhs, Quaternion rhs)
    {
        return new Quaternion(
                lhs.a - rhs.a,
                lhs.b - rhs.b,
                lhs.c - rhs.c,
                lhs.d - rhs.d);
    }

    public static Quaternion identityRotation()
    {
        return new Quaternion(1.0, 0.0, 0.0, 0.0);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return equals((Quaternion)obj);
    }

    public boolean equals(Quaternion other) {
        return compareTo(other) == 0;
    }

    @Override
    public int compareTo(Quaternion o) {
        int result;

        result = Double.compare(this.a, o.a);
        if (result != 0) {
            return result;
        }

        result = Double.compare(this.b, o.b);
        if (result != 0) {
            return result;
        }

        result = Double.compare(this.c, o.c);
        if (result != 0) {
            return result;
        }

        result = Double.compare(this.d, o.d);
        return result;
    }
}
