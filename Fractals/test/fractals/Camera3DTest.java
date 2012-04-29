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

package fractals;

import fractals.math.Matrix;
import fractals.math.Quaternion;
import fractals.math.Triplex;
import junit.framework.TestCase;

/**
 *
 * @author pauldoo
 */
public class Camera3DTest extends TestCase {

    public Camera3DTest(String testName) {
        super(testName);
    }

    public void testProjectionInvertability() {
        {
            Triplex position = new Triplex(2.0, 3.0, 5.0);
            Camera3D camera = new Camera3D(position, Quaternion.identityRotation());
            Matrix projectionMatrix = camera.toProjectionMatrix();
            Matrix invertedProjectionMatrix = Matrix.invert4x4(projectionMatrix);
            Triplex recoveredPosition = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(0.0, 0.0, 1.0, 0.0)).toTriplex();
            assertTrue(recoveredPosition.approximatelyEquals(position));
            Triplex recoveredForward = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(0.0, 0.0, 1.0, 1.0)).toTriplex();
            assertTrue(recoveredForward.approximatelyEquals(Triplex.add(position, new Triplex(0.0, 0.0, 1.0))));
            Triplex recoveredForwardRight = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(1.0, 0.0, 1.0, 1.0)).toTriplex();
            assertTrue(recoveredForwardRight.approximatelyEquals(Triplex.add(position, new Triplex(1.0, 0.0, 1.0))));
            Triplex recoveredForwardUp = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(0.0, 1.0, 1.0, 1.0)).toTriplex();
            assertTrue(recoveredForwardUp.approximatelyEquals(Triplex.add(position, new Triplex(0.0, 1.0, 1.0))));
        }
        {
            Triplex position = new Triplex(7.0, -8.0, 9.0);
            Camera3D camera = new Camera3D(position, Quaternion.createRotation(new Triplex(1.0, 0.0, 0.0), 4.0));
            Matrix projectionMatrix = camera.toProjectionMatrix();
            Matrix invertedProjectionMatrix = Matrix.invert4x4(projectionMatrix);
            Triplex recoveredPosition = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(0.0, 0.0, 1.0, 0.0)).toTriplex();
            assertTrue(recoveredPosition.approximatelyEquals(position));
        }
    }

}
