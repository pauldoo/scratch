/*
    Copyright (c) 2007, 2008, 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and distribute this software for any
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

/**
    Simple struct-like object for storing 4D vectors.
*/
public final class Vector4
{
    private final double a;
    private final double b;
    private final double c;
    private final double d;

    public Vector4(double a, double b, double c, double d)
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public double getD() {
        return d;
    }

    @Override
    public int hashCode()
    {
        return
            new Double(getA()).hashCode() ^
            new Double(getB()).hashCode() ^
            new Double(getC()).hashCode() ^
            new Double(getD()).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return equals((Vector4)o);
    }

    boolean equals(Vector4 other)
    {
        return
            this == other || (
                this.getA() == other.getA() &&
                this.getB() == other.getB() &&
                this.getC() == other.getC() &&
                this.getD() == other.getD());
    }

    @Override
    public String toString()
    {
        return "(" + getA() + ", " + getB() + ", " + getC() + ", " + getD() + ")";
    }

    public static Vector4 add(Vector4 lhs, Vector4 rhs)
    {
        return new Vector4(
                lhs.getA() + rhs.getA(),
                lhs.getB() + rhs.getB(),
                lhs.getC() + rhs.getC(),
                lhs.getD() + rhs.getD());
    }

    public Vector4 add(Vector4 rhs)
    {
        return add(this, rhs);
    }

    public static Vector4 multiply(Vector4 vector, double scalar)
    {
        return new Vector4(
                vector.getA() * scalar,
                vector.getB() * scalar,
                vector.getC() * scalar,
                vector.getD() * scalar);
    }

    public Vector4 multiply(double scalar)
    {
        return multiply(this, scalar);
    }
}
