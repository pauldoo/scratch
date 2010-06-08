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

import fractals.math.Matrix;
import fractals.math.Quaternion;
import fractals.math.Triplex;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.event.MouseInputListener;

/**
    Projects 3D surfaces to 2D.
*/
final class ProjectorComponent extends BackgroundRenderingComponent implements MouseInputListener, KeyListener
{

    private static final long serialVersionUID = 4417921502019642371L;

    private static final int superSample = 2;
    private static final int subSample = 16;
    private final Color backgroundColor = Color.DARK_GRAY;
    private double shiftDistance = Double.NaN;
    private SurfaceProvider surfaceProvider;
    private Camera3D camera = new Camera3D(new Triplex(0.0, 0.5, -1.5), Quaternion.identityRotation());
    private Point previousDragPoint = null;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (previousDragPoint != null) {
            final Point currentDragPoint = e.getPoint();
            final int width = getWidth();
            final int height = getHeight();
            final double halfSize = getSupersampledHalfSize() / superSample;
            final double x1 = (previousDragPoint.x - (width / 2.0)) / halfSize;
            final double y1 = (previousDragPoint.y - (height / 2.0)) / halfSize;
            final double x2 = (currentDragPoint.x - (width / 2.0)) / halfSize;
            final double y2 = (currentDragPoint.y - (height / 2.0)) / halfSize;
            final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
            final Triplex previous = recoverDirectionVector(invertedProjectionMatrix, x1, y1).normalize();
            final Triplex current = recoverDirectionVector(invertedProjectionMatrix, x2, y2).normalize();

            previousDragPoint = currentDragPoint;
            if (Double.isNaN(shiftDistance) == false) {
                final Triplex displacement = Triplex.multiply(Triplex.subtract(current, previous).negate(), shiftDistance);
                camera = camera.replicateAddShift(displacement);
            } else {
                final Triplex axis = Triplex.crossProduct(previous, current).normalize();
                final double angle = Math.acos(Triplex.dotProduct(previous, current));
                Quaternion update = Quaternion.createRotation(axis, angle);
                camera = camera.replicateAddRotation(update);
            }
            super.rerender();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        previousDragPoint = e.getPoint();
        if (e.isShiftDown()) {
            final Point currentDragPoint = e.getPoint();
            final int width = getWidth();
            final int height = getHeight();
            final double halfSize = getSupersampledHalfSize() / superSample;
            final double x1 = (previousDragPoint.x - (width / 2.0)) / halfSize;
            final double y1 = (previousDragPoint.y - (height / 2.0)) / halfSize;

            final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
            final Triplex cameraCenter = recoverCameraCenter(invertedProjectionMatrix);

            final Triplex rayVector = recoverDirectionVector(invertedProjectionMatrix, x1, y1).normalize();
            shiftDistance = distanceToSurface(cameraCenter, rayVector);
        }
    }

    private double distanceToSurface(final Triplex cameraCenter, final Triplex rayVector) {
        final SurfaceProvider.HitAndColor hit = surfaceProvider.firstHit(cameraCenter, rayVector, rayArcAngle(), null);
        return (hit == null) ? Double.NaN : Triplex.subtract(hit.position, cameraCenter).magnitude();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        previousDragPoint = null;
        shiftDistance = Double.NaN;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case '+':
                zoomBy(0.2);
                break;
            case '-':
                zoomBy(-0.2);
                break;
            default:
        }
    }

    private void zoomBy(double factor)
    {
        final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
        final Triplex cameraCenter = recoverCameraCenter(invertedProjectionMatrix);

        final Triplex rayVector = recoverDirectionVector(invertedProjectionMatrix, 0.0, 0.0).normalize();
        final double distance = distanceToSurface(cameraCenter, rayVector);
        if (Double.isNaN(distance) == false) {
            final Triplex shift = Triplex.multiply(rayVector, distance * factor);
            camera = camera.replicateAddShift(shift);
            super.rerender();
        }
    }

    /**
        Callback interface used by ProjectorComponent to
        provide geometry information.
    */
    static interface SurfaceProvider
    {
        final static class HitAndColor
        {
            final Triplex position;
            final Color color;

            public HitAndColor(Triplex position, Color color) {
                this.position = position;
                this.color = color;
            }
        }

        /**
            Returns null if there is no hit.
         */
        HitAndColor firstHit(
            Triplex cameraCenter,
            Triplex rayVector,
            final double rayWidthInRadians,
            Collection<Pair<Triplex, Color>> lights);
    }

    public ProjectorComponent(final SurfaceProvider surfaceProvider) {
        super(superSample);
        this.surfaceProvider = surfaceProvider;
        this.setFocusable(true);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addKeyListener(this);
    }

    private double rayArcAngle()
    {
        final Matrix projectionMatrix = camera.toProjectionMatrix();
        final double result = screenSizeToWorldAngle(
            Matrix.invert4x4(projectionMatrix),
            1.0 / (Math.max(super.getSupersampledWidth(), super.getSupersampledHeight()) / 2.0));
        return result;
    }

    private double getSupersampledHalfSize()
    {
        return Math.max(super.getSupersampledWidth(), super.getSupersampledHeight()) / 1.5;
    }

    @Override
    protected void render(Graphics2D g) throws InterruptedException {
        Utilities.setGraphicsToLowQuality(g);

        AffineTransform originalTransform = g.getTransform();
        final double theta = 2.5;//System.currentTimeMillis() * 0.0001;

        final Matrix projectionMatrix = camera.toProjectionMatrix();

        for (int downscale = subSample * superSample; downscale >= 1; downscale /= 2) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            g.setTransform(originalTransform);
            g.transform(AffineTransform.getScaleInstance(downscale, downscale));
            doRender(
                    surfaceProvider,
                    projectionMatrix,
                    g,
                    super.getSupersampledWidth() / downscale,
                    super.getSupersampledHeight() / downscale,
                    getSupersampledHalfSize() / downscale,
                    rayArcAngle(),
                    backgroundColor);

            super.bufferIsNowOkayToBlit();
        }

        g.setTransform(originalTransform);
    }

    public void setSurface(SurfaceProvider surfaceProvider)
    {
        this.surfaceProvider = surfaceProvider;
        super.rerender();
    }

    private static Triplex recoverCameraCenter(
            Matrix invertedProjectionMatrix)
    {
        return Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(0.0, 0.0, 1.0, 0.0)).toTriplex();
    }

    /**
        Given a coordinate in screen space (typically [(-1, -1), (1, 1)]),
        returns the direction of the ray in world space.
    */
    private static Triplex recoverDirectionVector(
            Matrix invertedProjectionMatrix,
            double x,
            double y)
    {
        Triplex position = Matrix.multiply(invertedProjectionMatrix, Matrix.create4x1(x, y, 1.0, 1.0)).toTriplex();
        return Triplex.subtract(position, recoverCameraCenter(invertedProjectionMatrix));
    }

    /**
        Given a size in screen space (where the entire screen is [(-1, -1), (1, 1)]),
        returns the size of the corresponding ray in world space radians.
    */
    private static double screenSizeToWorldAngle(
        Matrix invertedProjectionMatrix,
        double screenSize)
    {
        return
                Math.acos(
                    Triplex.dotProduct(
                        recoverDirectionVector(
                            invertedProjectionMatrix,
                            0.0,
                            0.0).normalize(),
                        recoverDirectionVector(
                            invertedProjectionMatrix,
                            screenSize,
                            screenSize).normalize())) / Math.sqrt(2.0);
    }

    private static void doRender(
            final SurfaceProvider surfaceProvider,
            final Matrix projectionMatrix,
            final Graphics g,
            final int width,
            final int height,
            final double halfSize,
            final double rayArcAngle,
            final Color backgroundColor) throws InterruptedException
    {
        final Matrix invertedProjectionMatrix = Matrix.invert4x4(projectionMatrix);
        final Triplex cameraCenter = recoverCameraCenter(invertedProjectionMatrix);

        final List<Pair<Triplex, Color> > lights = new ArrayList<Pair<Triplex, Color> >();
        lights.add(new Pair<Triplex, Color>(new Triplex(+1.0, 0.0, 0.0), Color.RED));
        lights.add(new Pair<Triplex, Color>(new Triplex(-0.5, -0.866, 0.0), Color.GREEN));
        lights.add(new Pair<Triplex, Color>(new Triplex(-0.5, +0.866, 0.0), Color.BLUE));
        lights.add(new Pair<Triplex, Color>(new Triplex(0.0, 0.0, -1.0), Color.DARK_GRAY));
        lights.add(new Pair<Triplex, Color>(new Triplex(0.0, 0.0, +1.0), Color.DARK_GRAY));

        List<Future> futures = new ArrayList<Future>();
        try {
            final Object lockObject = new Object();
            for (int y = 0; y < height; y++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int iy = y;
                Runnable runner = new Runnable() {
                    @Override
                    public void run()
                    {
                        for (int ix = 0; ix < width; ix++) {
                            final double sx = (ix - width/2.0) / halfSize;
                            final double sy = (iy - height/2.0) / halfSize;
                            final Triplex rayVector = recoverDirectionVector(invertedProjectionMatrix, sx, sy);

                            Color color = null;
                            try {
                                final SurfaceProvider.HitAndColor hitAndColor = surfaceProvider.firstHit(cameraCenter, rayVector, rayArcAngle * 0.1, lights);
                                if (hitAndColor == null) {
                                    color = backgroundColor;
                                } else {
                                    color = hitAndColor.color;
                                }
                            } catch (NotANumberException ex) {
                                color = Color.BLUE;
                            }
                            synchronized (lockObject) {
                                g.setColor(color);
                                g.fillRect(ix, iy, 1, 1);
                            }
                        }
                    }
                };
                futures.add(Utilities.getHeavyThreadPool().submit(runner));
            }
            for (Future future: futures) {
                future.get();
            }
        } catch (ExecutionException ex) {
            ex.printStackTrace();
        } finally {
            for (Future future: futures) {
                future.cancel(true);
            }
        }
    }
}

