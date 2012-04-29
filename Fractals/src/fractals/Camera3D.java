/*
    Copyright (c) 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

package fractals;

import fractals.math.Matrix;
import fractals.math.Quaternion;
import fractals.math.Triplex;

/**
    Stores all properties of a camera in 3D space.
    The representation is intentionally more restrictive than
    a complexe 3x4 or 4x4 matrix in order to preserve certain
    properties.

    The default orientation of a camera (with no rotation) is to point
    towards +Z.  X and Y in this orientation align with X and Y after
    projection.
*/
final class Camera3D
{
    public final Triplex position;
    public final Quaternion rotation;

    Camera3D(final Triplex position, final Quaternion rotation)
    {
        this.position = position;
        this.rotation = rotation.normalize();
    }

    /**
        Constructs a 4x4 matrix representing
        the complete transform from real world
        coordinates to screen space coordinates.
    */
    Matrix toProjectionMatrix()
    {
        Matrix transform = Matrix.create4x4(position.negate(), rotation);

        Matrix result = Matrix.multiply(
            createProjectionMatrix(),
            transform);
        return result;
    }

    /**
        The simplest perspective projection matrix that makes
        inverting the complete world to screen matrix easy.
    */
    private static Matrix createProjectionMatrix()
    {
        return Matrix.create4x4(
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 1.0,
                0.0, 0.0, 1.0, 0.0);
    }

    final Camera3D replicateAddRotation(Quaternion rotationStep)
    {
        return new Camera3D(
                this.position,
                Quaternion.multiply(this.rotation, rotationStep));
    }

    final Camera3D replicateAddShift(Triplex shift)
    {
        return new Camera3D(
                Triplex.add(this.position, shift),
                this.rotation);
    }
}
