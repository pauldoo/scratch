/*
    Copyright (c) 2009, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

import fractals.math.Triplex;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;

final class Mandelbulb {
    public static final int maxIterations = 30;

    private static Pair<Triplex, Double> powN(final Triplex k, final double dr)
    {
        /*
            Absolute voodoo!

            Check back in VCS history to find a more understandable implementation.
        */
        final double x = k.x;
        final double y = k.y;
        final double z = k.z;

        final double x2 = x * x;
        final double x3 = x2 * x;
        final double x4 = x2 * x2;
        final double x5 = x4 * x;
        final double x6 = x4 * x2;
        final double x7 = x4 * x3;
        final double x8 = x4 * x4;

        final double y2 = y * y;
        final double y3 = y2 * y;
        final double y4 = y2 * y2;
        final double y5 = y4 * y;
        final double y6 = y4 * y2;
        final double y7 = y4 * y3;
        final double y8 = y4 * y4;

        final double z2 = z * z;
        final double z3 = z2 * z;
        final double z4 = z2 * z2;
        final double z5 = z4 * z;
        final double z6 = z4 * z2;
        final double z7 = z4 * z3;
        final double z8 = z4 * z4;

        final double w2 = x2 + y2 + z2;
        final double w1 = Math.sqrt(w2);
        final double w4 = w2 * w2;
        final double w6 = w4 * w2;
        final double w7 = w6 * w1;
        final double w8 = w4 * w4;

        final double a = 8 * w1 * Math.sqrt((x2 + y2) / w2) * (
                z7
                - 7 * x2 * z5
                - 7 * y2 * z5
                + 14 * x2 * y2 * z3
                - y6 * z
                - 3 * x2 * y4 * z
                + 7 * y4 * z3
                - x6 * z
                - 3 * x4 * y2 * z
                + 7 * x4 * z3);

        final double c1 = x2 + y2;
        final double c2 = c1 * c1;
        final double c4 = c2 * c2;

        final double magic1 = a * 0.5 * (
                + 2 * y8
                - 56 * x2 * y6
                + 2 * x8
                - 56 * x6 * y2
                + 140 * x4 * y4) / c4;
        final double magic2 = a * 0.5 * (
                + 16 * x * y7
                - 16 * x7 * y
                + 112 * x5 * y3
                - 112 * x3 * y5) / c4;
        final double magic3 =
                - 32 * z2 * w6
                + 128 * z8
                - 256 * z6 * w2
                + 160 * z4 * w4
                + w8;
        final double magic4 = w7 * dr * 8 + 1.0;

        return new Pair<Triplex, Double>(new Triplex(magic1, magic2, magic3), magic4);
    }

    /**
        Estimate distance to mandelbulb surface by: 0.5 * |w| * log(|w|) / |δw|
    */
    public static double distanceEstimate(final Triplex z0, final int maxIter)
    {
        if (z0.magnitude() >= 1.5) {
            return z0.magnitude() - 1.49;
        } else {
            final Triplex c = z0;
            Triplex z = z0;

            double dr = 1.0;
            double r = z.magnitude();
            for (int i = 0; i < maxIter; i++) {
                Pair<Triplex, Double> newValues = powN(z, dr);
                z = newValues.first;
                dr = newValues.second;
                z = Triplex.add(z, c);

                r = z.magnitude();
                if (r > 1e10) {
                    break;
                }
            }

            return 0.5 * Math.log(r) * r / dr;
        }
    }

    final static class SurfaceProvider implements ProjectorComponent.SurfaceProvider
    {
        @Override
        public HitAndColor firstHit(
            final Triplex cameraCenter,
            final Triplex unnormalizedRayVector,
            final double rayWidthInRadians,
            final Collection<Pair<Triplex, Color>> lights)
        {
            final double shadowStrength = 0.03;
            final Triplex rayVector = unnormalizedRayVector.normalize();

            double distance = 0.0;
            int counter = 0;
            while (true) {
                final Triplex position = Triplex.add(cameraCenter, Triplex.multiply(rayVector, distance));
                if (position.magnitude() > 10.0 && Triplex.dotProduct(position, rayVector) > 0.0) {
                    return null;
                }

                final double threshold = distance * rayWidthInRadians;

                final double distanceEstimate = distanceEstimate(position, maxIterations);
                final double shade = Math.exp(-counter * shadowStrength);
                if (distanceEstimate <= threshold || shade < (1.0 / 256)) {
                    return new HitAndColor(position, new Color((float)0.0, (float)(shade * 1.0), (float)(shade * 0.5)));
                }
                distance += distanceEstimate;
                counter++;
            }
        }
    }

    public static JComponent createView()
    {
        return ProjectorComponent.createView(new SurfaceProvider());
    }
}
