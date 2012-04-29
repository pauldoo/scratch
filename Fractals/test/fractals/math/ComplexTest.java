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

import junit.framework.TestCase;

public final class ComplexTest extends TestCase
{

    private final Complex a = new Complex(2, 3);
    private final Complex b = new Complex(4, -5);

    public ComplexTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateFromPolar()
    {
        double r = 7;
        double theta = 8;
        Complex expResult = new Complex(r * Math.cos(theta), r * Math.sin(theta));
        Complex result = Complex.createFromPolar(r, theta);
        assertEquals(expResult, result);
        assertApproximatelyEquals(7.0, result.polarR(), 1e-6);
        assertApproximatelyEquals(8.0 - 2 * Math.PI, result.polarTheta(), 1e-6);
    }

    public void testGetReal()
    {
        assertEquals(2.0, a.getReal());
        assertEquals(4.0, b.getReal());
    }

    public void testGetImaginary()
    {
        assertEquals(3.0, a.getImaginary());
        assertEquals(-5.0, b.getImaginary());
    }

    public void testMagnitudeSquared()
    {
        assertEquals(13.0, a.magnitudeSquared());
        assertEquals(41.0, b.magnitudeSquared());
    }

    /**
     * Test of magnitude method, of class Complex.
     */
    public void testMagnitude()
    {
        assertEquals(Math.sqrt(13.0), a.magnitude());
        assertEquals(Math.sqrt(41.0), b.magnitude());
    }

    public void testConjugate()
    {
        assertEquals(new Complex(2.0, -3.0), a.conjugate());
        assertEquals(new Complex(4.0, 5.0), b.conjugate());
    }

    public void testInverse()
    {
        assertEquals(new Complex(1.0, 0.0), a.multiply(a.inverse()));
        assertEquals(new Complex(1.0, 0.0), b.multiply(b.inverse()));
    }

    public void testAdd()
    {
        assertEquals(new Complex(6.0, -2.0), a.add(b));
    }

    public void testSubtract()
    {
        assertEquals(new Complex(-2.0, 8.0), a.subtract(b));
    }

    public void testMultiply()
    {
        assertEquals(new Complex(8.0 + 15.0, 12.0 - 10.0 ), a.multiply(b));
        assertEquals(new Complex(8.0 + 15.0, 12.0 - 10.0 ), b.multiply(a));
    }

    public void testDivide()
    {
        assertEquals(new Complex((8.0 - 15.0) / 41.0, (12.0 + 10.0) / 41.0 ), a.divide(b));
        //assertEquals(new Complex(8.0 + 15.0, 12.0 - 10.0 ), b.divide(a));
    }

    public void testPower()
    {
        assertApproximatelyEquals(a.multiply(a).multiply(a), a.power(new Complex(3.0, 0.0)), 1e-6);
        assertApproximatelyEquals(b.multiply(b).inverse(), b.power(new Complex(-2.0, 0.0)), 1e-6);
        assertApproximatelyEquals(new Complex(-1.0, 0.0) , new Complex(Math.E, 0.0).power(new Complex(0.0, Math.PI)), 1e-6);
    }

    private void assertApproximatelyEquals(double a, double b, double tollerance)
    {
        if (Math.abs(a - b) > tollerance) {
            assertEquals(a, b);
        }
    }

    private void assertApproximatelyEquals(Complex a, Complex b, double tollerance)
    {
        if (a.subtract(b).magnitudeSquared() > tollerance) {
            assertEquals(a, b);
        }
    }
}
