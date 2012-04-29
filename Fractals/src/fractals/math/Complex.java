/*
    Copyright (c) 2008, 2012 Paul Richards <paul.richards@gmail.com>

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
    Complex number class.

    This class is not immutable but most methods behave as if it were.
    That is they return new instances rather than modify any existing
    instance.

    If the overhead of creating new instances is deemed too high, then the
    "xxxReplace()" methods should be used instead.  These perform their
    operation and write the result out to an existing instance.
*/
public final class Complex implements Cloneable
{
    /// Real part
    private double real;

    /// Imaginary part
    private double imaginary;

    public Complex(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex createFromPolar(double r, double theta)
    {
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    @Override
    public Complex clone()
    {
        try {
            return (Complex)super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new Error("Clone should always succeed", ex);
        }
    }

    @Override
    public int hashCode()
    {
        return
            new Double(R()).hashCode() ^
            new Double(I()).hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return equals((Complex)o);
    }

    public boolean equals(Complex other)
    {
        return
            this == other || (
                this.R() == other.R() &&
                this.I() == other.I());
    }

    @Override
    public String toString()
    {
        return R() + " + " + I() + "i";
    }

    /**
        The real part of this complex number.
    */
    public double getReal()
    {
        return real;
    }

    public void setReal(double real)
    {
        this.real = real;
    }

    /**
        The imaginary part of this complex number.
    */
    public double getImaginary()
    {
        return imaginary;
    }

    public void setImaginary(double imaginary)
    {
        this.imaginary = imaginary;
    }

    /**
        Shorter named version of getReal() to make method bodies more readable.
    */
    private double R()
    {
        return getReal();
    }

    /**
        Shorter named version of getImaginary() to make method bodies more readable.
    */
    private double I()
    {
        return getImaginary();
    }

    public double polarR()
    {
        return magnitude();
    }

    public double polarTheta()
    {
        return Math.atan2(I(), R());
    }

    public double magnitudeSquared()
    {
        return R() * R() + I() * I();
    }

    public double magnitude()
    {
        return Math.sqrt(magnitudeSquared());
    }

    public Complex conjugate()
    {
        return new Complex(R(), -I());
    }

    public Complex inverse()
    {
        return conjugate().divide(magnitudeSquared());
    }

    public Complex negate()
    {
        return new Complex(-R(), -I());
    }

    public static void addReplace(Complex a, Complex b)
    {
        a.setReal(a.R() + b.R());
        a.setImaginary(a.I() + b.I());
    }

    public static Complex add(Complex a, Complex b)
    {
        Complex result = a.clone();
        addReplace(result, b);
        return result;
    }

    public Complex add(Complex b)
    {
        return add(this, b);
    }

    public static void subtractReplace(Complex a, Complex b)
    {
        a.setReal(a.R() - b.R());
        a.setImaginary(a.I() - b.I());
    }

    public static Complex subtract(Complex a, Complex b)
    {
        Complex result = a.clone();
        subtractReplace(result, b);
        return result;
    }

    public Complex subtract(Complex b)
    {
        return subtract(this, b);
    }

    public static void multiplyReplace(Complex a, Complex b)
    {
        double r = a.R() * b.R() - a.I() * b.I();
        double i = a.I() * b.R() + a.R() * b.I();
        a.setReal(r);
        a.setImaginary(i);
    }

    public static Complex multiply(Complex a, Complex b)
    {
        Complex result = a.clone();
        multiplyReplace(result, b);
        return result;
    }

    public Complex multiply(Complex b)
    {
        return multiply(this, b);
    }

    public static void divideReplace(Complex a, double b)
    {
        a.setReal(a.R() / b);
        a.setImaginary(a.I() / b);
    }

    public static Complex divide(Complex a, double b)
    {
        Complex result = a.clone();
        divideReplace(result, b);
        return result;
    }

    public Complex divide(double b)
    {
        return divide(this, b);
    }

    public static void divideReplace(Complex a, Complex b)
    {
        multiplyReplace(a, b.conjugate());
        divideReplace(a, b.magnitudeSquared());
    }

    public static Complex divide(Complex a, Complex b)
    {
        Complex result = a.clone();
        divideReplace(result, b);
        return result;
    }

    public Complex divide(Complex b)
    {
        return divide(this, b);
    }

    public static Complex power(Complex a, Complex b)
    {
        return createFromPolar(
                Math.pow(a.polarR(), b.R()) * Math.exp(-b.I() * a.polarTheta()),
                b.I() * Math.log(a.polarR()) + b.R() * a.polarTheta());
    }

    public Complex power(Complex b)
    {
        return power(this, b);
    }

    public static void squareReplace(Complex a)
    {
        multiplyReplace(a, a);
    }

    public static Complex square(Complex a)
    {
        Complex result = a.clone();
        squareReplace(result);
        return result;
    }

    public Complex square()
    {
        return square(this);
    }
}
