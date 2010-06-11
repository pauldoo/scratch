/*
    Copyright (C) 2009, 2010  Paul Richards.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fractals;

import fractals.math.Complex;
import fractals.math.Matrix;
import fractals.math.Triplex;
import java.awt.Color;
import java.util.Collection;
import javax.swing.JComponent;

final class Mandelbulb {
    public static final int maxIterations = 30;

    public static boolean evaluate(final Triplex c, final int maxIter)
    {
        Triplex z = c;
        boolean inside = true;
        for (int i = 0; (inside = z.magnitudeSquared() < 4.0) && i < maxIter; i++) {
            z = stepNormal(c, z, null).first;
        }
        return inside;
    }

    private static Triplex iteratePoint(final Triplex c, final int iterations)
    {
        Triplex z = c;
        for (int i = 0; i < iterations; i++) {
            z = stepNormal(c, z, null).first;
        }
        return z;
    }

    /**
        Hardcoded for power-8 mandelbulbs.

        See "MandelBulb Normals.png" from Sean.
    */
    public static Triplex computeNormal(final Triplex c, final int maxIter)
    {
        Pair<Triplex, Matrix> state = new Pair<Triplex, Matrix>(
            c,
            Matrix.createIdentity(3));
        for (int i = 0; i < maxIter; i++) {
            state = stepNormal(c, state.first, state.second);
        }

        final Matrix normal = Matrix.multiply(
                Matrix.create1x3(state.first.x, state.first.y, state.first.z),
                state.second);
        final Triplex result = new Triplex(normal.get(0, 0), normal.get(0, 1), normal.get(0, 2));
        return Triplex.normalize(result);
    }

    private static Pair<Triplex, Double> powN(Triplex z, double zr0, double dr)
    {
        final int power = 8;
        double zo0 = Math.acos(z.z / zr0);
        double zi0 = Math.atan2(z.y, z.x);
        double zr = Math.pow(zr0, power - 1.0);
        double zo = (zo0) * power;
        double zi = (zi0) * power;
        double czo = Math.sin(zo);

        dr = zr * dr * power + 1.0;
        zr *= zr0;

        z = Triplex.multiply(
                new Triplex(czo*Math.cos(zi), czo*Math.sin(zi), Math.cos(zo)),
                zr);

        return new Pair<Triplex, Double>(z, dr);
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
                Pair<Triplex, Double> newValues = powN(z, r, dr);
                z = newValues.first;
                dr = newValues.second;
                z = Triplex.add(z, c);

                r = z.magnitude();
                if (r > 4) {
                    break;
                }
            }

            return 0.5 * Math.log(r) * r / dr;
        }
    }

    /**
        Performs a single iteration-worth of the mandelbulb
        function, but in a way that allows us to compute the mandelbulb
        surface normal.

        @see computeNormal
    */
    private static Pair<Triplex, Matrix> stepNormal(final Triplex c, final Triplex z, final Matrix jz)
    {
        final int n = 8;

        final double w2 = z.x * z.x + z.y * z.y;
        final double r2 = w2 + z.z * z.z;
        final double w = Math.sqrt(w2);
        final double r = Math.sqrt(r2);
        final double cosTheta = z.x / w;
        final double sinTheta = z.y / w;
        final double cosPhi = z.z / r;
        final double sinPhi = w / r;

        final Complex theta8 = new Complex(cosTheta, sinTheta);
        Complex.squareReplace(theta8);
        Complex.squareReplace(theta8);
        Complex.squareReplace(theta8);
        final Complex phi8 = new Complex(cosPhi, sinPhi);
        Complex.squareReplace(phi8);
        Complex.squareReplace(phi8);
        Complex.squareReplace(phi8);
        final double r4 = r2 * r2;
        final double r8 = r4 * r4;
        final double cosThetaBar = Utilities.assertNotNaN(theta8.getReal());
        final double sinThetaBar = Utilities.assertNotNaN(theta8.getImaginary());
        final double cosPhiBar = Utilities.assertNotNaN(phi8.getReal());
        final double sinPhiBar = Utilities.assertNotNaN(phi8.getImaginary());

        final Triplex zNew = Triplex.add(
                new Triplex(
                    r8 * cosThetaBar * sinPhiBar,
                    r8 * sinThetaBar * sinPhiBar,
                    r8 * cosPhiBar),
                c);

        Matrix jzNew = null;
        if (jz != null) {
            final Matrix A = Matrix.assertNotNaN(Matrix.create3x5(
                    cosThetaBar * sinPhiBar, r8 * sinPhiBar, 0.0, 0.0, r8 * cosThetaBar,
                    sinThetaBar * sinPhiBar, 0.0, r8 * sinPhiBar, 0.0, r8 * sinThetaBar,
                    cosPhiBar, 0.0, 0.0, r8, 0.0));
            final Matrix subThetaB = Matrix.power7(Matrix.assertNotNaN(Matrix.create2x2(
                    cosTheta, -sinTheta,
                    sinTheta, cosTheta)));
            final Matrix subPhiB = Matrix.power7(Matrix.assertNotNaN(Matrix.create2x2(
                    cosPhi, -sinPhi,
                    sinPhi, cosPhi)));
            final Matrix B = Matrix.assertNotNaN(Matrix.create5x5(
                    n * (r4 * (r2 * r)), 0.0, 0.0, 0.0, 0.0,
                    0.0, n * subThetaB.get(0, 0), n * subThetaB.get(0, 1), 0.0, 0.0,
                    0.0, n * subThetaB.get(1, 0), n * subThetaB.get(1, 1), 0.0, 0.0,
                    0.0, 0.0, 0.0, n * subPhiB.get(0, 0), n * subPhiB.get(0, 1),
                    0.0, 0.0, 0.0, n * subPhiB.get(1, 0), n * subPhiB.get(1, 1)));
            final Matrix C = Matrix.assertNotNaN(Matrix.create5x5(
                    1.0, 0.0, 0.0, 0.0, 0.0,
                    0.0, -z.x / w2, 1.0 / w, 0.0, 0.0,
                    0.0, -z.y / w2, 0.0, 1.0 / w, 0.0,
                    -z.z / r2, 0.0, 0.0, 0.0, 1.0 / r,
                    -w / r2, 1.0 / r, 0.0, 0.0, 0.0));
            final Matrix D = Matrix.assertNotNaN(Matrix.create5x3(
                    z.x / r, z.y / r, z.z / r,
                    z.x / w, z.y / w, 0.0,
                    1.0, 0.0, 0.0,
                    0.0, 1.0, 0.0,
                    0.0, 0.0, 1.0));

            jzNew =
                Matrix.add(
                    Matrix.assertNotNaN(Matrix.multiply(Matrix.multiply(Matrix.multiply(Matrix.multiply(A, B), C), D), jz)),
                    Matrix.createIdentity(3));
        }

        return new Pair<Triplex, Matrix>(zNew, jzNew);
    }

    private static final class Evaluator implements Runnable
    {
        final ProjectorComponent renderComponent;

        public Evaluator(ProjectorComponent renderComponent) {
            this.renderComponent = renderComponent;
        }

        @Override
        public void run() {
            OctTree tree = OctTree.createEmpty();
            for (int level = 1; level <= 8; level++) {
                final long startTime = System.currentTimeMillis();

                final int resolution = 2 << level;
                for (int iz = -resolution; iz < resolution; iz++) {
                    for (int iy = -resolution; iy < resolution; iy++) {
                        for (int ix = -resolution; ix < resolution; ix++) {
                            double x = (ix + 0.5) / resolution;
                            double y = (iy + 0.5) / resolution;
                            double z = (iz + 0.5) / resolution;

                            boolean inside = Mandelbulb.evaluate(new Triplex(x * 1.5, y * 1.5, z * 1.5), maxIterations);
                            double scale = 0.5 / resolution;
                            tree = tree.repSetRegion(x - scale, y - scale, z - scale, x + scale, y + scale, z + scale, inside);
                        }
                    }
                }

                final long endTime = System.currentTimeMillis();

                final int nodeCount = tree.nodeCount();
                System.out.println("Level " + level + ", resolution " + resolution + ", nodeCount " + nodeCount + ", nodeCount/resolution^2 " + (nodeCount / (resolution * resolution)) + ", time " + (endTime - startTime) + "ms");
                renderComponent.setSurface(new OctTreeSurfaceProvider(tree, new NormalProvider()));
            }
        }
    }

    final static class NormalProvider implements OctTreeSurfaceProvider.NormalProvider
    {
        @Override
        public Triplex normalAtPosition(Triplex p) {
            return computeNormal(p, maxIterations);
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

    public static JComponent createViewUsingOctTreeSurface()
    {
        final ProjectorComponent renderComponent = new ProjectorComponent(null);

        /*
            TODO: Remove this thread, and do it as part of the OctTreeRenderComponent.
            That way evaluation of a region will only occur as a consequence of a rendering.
        */
        new Thread(new Evaluator(renderComponent)).start();
        return renderComponent;
    }

    public static JComponent createViewUsingAnalyticalSurface()
    {
        final ProjectorComponent renderComponent = new ProjectorComponent(new SurfaceProvider());
        return renderComponent;
    }
}
