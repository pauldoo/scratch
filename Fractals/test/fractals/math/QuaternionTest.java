/*
    Copyright (C) 2010  Paul Richards.

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

import junit.framework.TestCase;

/**
 *
 * @author pauldoo
 */
public class QuaternionTest extends TestCase {

    public QuaternionTest(String testName) {
        super(testName);
    }

    public void testIdentity() {
        Quaternion q = Quaternion.identityRotation();
        q = Quaternion.multiply(q, q);
        assertEquals(q, Quaternion.identityRotation());
    }

    private static boolean approximatelyEqual(Quaternion a, Quaternion b)
    {
        return
                Quaternion.subtract(a, b).magnitudeSquared() <= 1e-6 ||
                Quaternion.subtract(a.negate(), b).magnitudeSquared() <= 1e-6;
    }

    public void testCreateRotation()
    {
        {
            Quaternion q = Quaternion.createRotation(new Triplex(1.0, 0.0, 0.0), 0.0);
            assertTrue(approximatelyEqual(q, Quaternion.identityRotation()));
        }
        {
            Quaternion q = Quaternion.createRotation(new Triplex(1.0, 0.0, 0.0), Math.PI * 2.0);
            assertTrue(approximatelyEqual(q, Quaternion.identityRotation()));
        }
        {
            Quaternion q = Quaternion.createRotation(new Triplex(0.0, 1.0, 0.0), Math.PI * 2.0 / 3.0);
            q = Quaternion.multiply(Quaternion.multiply(q, q), q).normalize();
            assertTrue(approximatelyEqual(q, Quaternion.identityRotation()));
        }
        {
            Quaternion q = Quaternion.createRotation(new Triplex(0.0, 0.0, 1.0), Math.PI * 2.0 / 4.0);
            q = Quaternion.multiply(Quaternion.multiply(q, q), q).normalize();
            assertFalse(approximatelyEqual(q, Quaternion.identityRotation()));
        }
        {
            Quaternion q = Quaternion.createRotation(new Triplex(0.0, 0.0, 1.0), Math.PI * 2.0 / 4.0);
            q = Quaternion.multiply(Quaternion.multiply(Quaternion.multiply(q, q), q), q).normalize();
            assertTrue(approximatelyEqual(q, Quaternion.identityRotation()));
        }
    }
}
