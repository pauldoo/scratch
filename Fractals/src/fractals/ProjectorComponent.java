/*
    Copyright (c) 2009, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputListener;

/**
    Projects 3D surfaces to 2D.
*/
final class ProjectorComponent extends BackgroundRenderingComponent implements MouseInputListener, KeyListener
{

    private static final long serialVersionUID = 4417921502019642371L;

    private static enum DragType {
        ROTATE_ON_THE_SPOT,
        PAN,
        ROTATE_AROUND_SURFACE
    }

    private static final int superSample = 2;
    private static final int subSample = 8;
    private final Color backgroundColor = Color.DARK_GRAY;
    private DragType dragType;
    private SurfaceProvider surfaceProvider;
    private Camera3D camera = new Camera3D(
            new Triplex(0.0, 3.0, -0.5),
            Quaternion.createRotation(new Triplex(1.0, 0.0, 0.0), (-Math.PI / 2) * 0.9));
    private Point previousDragPoint = null;

    private JButton createPanButton(
        final JButton button,
        final double dx,
        final double dy)
    {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panBy(dx, dy);
            }
        });
        return button;
    }

    private JButton createRotateOnTheSpotButton(
        final JButton button,
        final double dx,
        final double dy)
    {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotateOnTheSpotBy(dx, dy);
            }
        });
        return button;
    }

    private JButton createRotateAroundSurfaceButton(
        final JButton button,
        final double dx,
        final double dy)
    {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotateAroundTheSurface(dx, dy);
            }
        });
        return button;
    }

    private JButton createZoomButton(
        final JButton button,
        final double dz)
    {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomBy(dz);
            }
        });
        return button;
    }

    public static JComponent createView(final SurfaceProvider surface)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        ProjectorComponent projector = new ProjectorComponent(surface);
        panel.add(projector, BorderLayout.CENTER);

        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            {
                JPanel panControls = new JPanel();
                panControls.setBorder(BorderFactory.createTitledBorder("Pan"));
                panControls.setLayout(new BorderLayout());

                JPanel panButtons = new JPanel();
                panButtons.setLayout(new GridLayout(2, 2));
                panButtons.add(projector.createPanButton(new JButton("\u2191"), 0.0, 0.1));
                panButtons.add(projector.createPanButton(new JButton("\u2192"), -0.1, 0.0));
                panButtons.add(projector.createPanButton(new JButton("\u2190"), 0.1, 0.0));
                panButtons.add(projector.createPanButton(new JButton("\u2193"), 0.0, -0.1));
                panControls.add(panButtons, BorderLayout.CENTER);

                panControls.add(new JLabel("(or shift click and drag)"), BorderLayout.SOUTH);
                controlPanel.add(panControls);
            }
            {
                JPanel rotateOnTheSpotControls = new JPanel();
                rotateOnTheSpotControls.setBorder(BorderFactory.createTitledBorder("Rotate on the spot"));
                rotateOnTheSpotControls.setLayout(new BorderLayout());

                JPanel rotateOnTheSpotButtons = new JPanel();
                rotateOnTheSpotButtons.setLayout(new GridLayout(2, 2));
                rotateOnTheSpotButtons.add(projector.createRotateOnTheSpotButton(new JButton("\u2191"), 0.0, 0.1));
                rotateOnTheSpotButtons.add(projector.createRotateOnTheSpotButton(new JButton("\u2192"), -0.1, 0.0));
                rotateOnTheSpotButtons.add(projector.createRotateOnTheSpotButton(new JButton("\u2190"), 0.1, 0.0));
                rotateOnTheSpotButtons.add(projector.createRotateOnTheSpotButton(new JButton("\u2193"), 0.0, -0.1));
                rotateOnTheSpotControls.add(rotateOnTheSpotButtons, BorderLayout.CENTER);

                rotateOnTheSpotControls.add(new JLabel("(or click and drag)"), BorderLayout.SOUTH);
                controlPanel.add(rotateOnTheSpotControls);
            }
            {
                JPanel rotateAroundSurfaceControls = new JPanel();
                rotateAroundSurfaceControls.setBorder(BorderFactory.createTitledBorder("Rotate around the surface"));
                rotateAroundSurfaceControls.setLayout(new BorderLayout());

                JPanel rotateAroundSurfaceButtons = new JPanel();
                rotateAroundSurfaceButtons.setLayout(new GridLayout(2, 2));
                rotateAroundSurfaceButtons.add(projector.createRotateAroundSurfaceButton(new JButton("\u2191"), 0.0, 0.1));
                rotateAroundSurfaceButtons.add(projector.createRotateAroundSurfaceButton(new JButton("\u2192"), -0.1, 0.0));
                rotateAroundSurfaceButtons.add(projector.createRotateAroundSurfaceButton(new JButton("\u2190"), 0.1, 0.0));
                rotateAroundSurfaceButtons.add(projector.createRotateAroundSurfaceButton(new JButton("\u2193"), 0.0, -0.1));
                rotateAroundSurfaceControls.add(rotateAroundSurfaceButtons, BorderLayout.CENTER);

                rotateAroundSurfaceControls.add(new JLabel("(or alt click and drag)"), BorderLayout.SOUTH);
                controlPanel.add(rotateAroundSurfaceControls);
            }
            {
                JPanel zoomControls = new JPanel();
                zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
                zoomControls.setLayout(new BorderLayout());

                JPanel zoomButtons = new JPanel();
                zoomButtons.setLayout(new GridLayout(2, 0));
                zoomButtons.add(projector.createZoomButton(new JButton("\u2191"), 0.2));
                zoomButtons.add(projector.createZoomButton(new JButton("\u2193"), -0.2));
                zoomControls.add(zoomButtons, BorderLayout.CENTER);

                zoomControls.add(new JLabel("(or +/- keys)"), BorderLayout.SOUTH);
                controlPanel.add(zoomControls);

            }
            panel.add(controlPanel, BorderLayout.SOUTH);
        }

        return panel;
    }

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
            previousDragPoint = currentDragPoint;

            switch (dragType) {
                case PAN:
                    panBy(x2 - x1, y2 - y1);
                    break;
                case ROTATE_ON_THE_SPOT:
                    rotateOnTheSpotBy(x2 - x1, y2 - y1);
                    break;
                case ROTATE_AROUND_SURFACE:
                    rotateAroundTheSurface(x2 - x1, y2 - y1);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised DragType");
            }
        }
    }

    private void rotateAroundTheSurface(double dx, double dy) {
        final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
        final Triplex previous = recoverDirectionVector(invertedProjectionMatrix, 0.0, 0.0).normalize();
        final Triplex current = recoverDirectionVector(invertedProjectionMatrix, -dx, -dy).normalize();

        final Triplex rayVector = recoverDirectionVector(invertedProjectionMatrix, 0.0, 0.0).normalize();
        final Triplex cameraCenter = recoverCameraCenter(invertedProjectionMatrix);
        final double shiftDistance = distanceToSurface(cameraCenter, rayVector);
        if (Double.isNaN(shiftDistance) == false) {
            final Triplex displacement = Triplex.multiply(Triplex.subtract(current, previous).negate(), shiftDistance);

            final Triplex axis = Triplex.crossProduct(previous, current);
            final double angle = Math.acos(Triplex.dotProduct(previous, current));
            final Quaternion rotationUpdate = Quaternion.createRotation(axis, -angle);

            camera = camera.replicateAddRotation(rotationUpdate);
            camera = camera.replicateAddShift(displacement);
            super.rerender();
        }
    }

    private void rotateOnTheSpotBy(double dx, double dy) {
        final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
        final Triplex previous = recoverDirectionVector(invertedProjectionMatrix, 0.0, 0.0).normalize();
        final Triplex current = recoverDirectionVector(invertedProjectionMatrix, dx, dy).normalize();
        final Triplex axis = Triplex.crossProduct(previous, current);
        final double angle = Math.acos(Triplex.dotProduct(previous, current));
        final Quaternion update = Quaternion.createRotation(axis, angle);
        camera = camera.replicateAddRotation(update);
        super.rerender();
    }

    private void panBy(double dx, double dy) {
        final Matrix invertedProjectionMatrix = Matrix.invert4x4(camera.toProjectionMatrix());
        final Triplex rayVector = recoverDirectionVector(invertedProjectionMatrix, 0.0, 0.0).normalize();
        final Triplex cameraCenter = recoverCameraCenter(invertedProjectionMatrix);
        final double shiftDistance = distanceToSurface(cameraCenter, rayVector);
        if (Double.isNaN(shiftDistance) == false) {
            final Triplex previous = recoverDirectionVector(invertedProjectionMatrix, 0, 0);
            final Triplex current = recoverDirectionVector(invertedProjectionMatrix, dx, dy);
            final Triplex displacement = Triplex.multiply(Triplex.subtract(current, previous).negate(), shiftDistance);
            camera = camera.replicateAddShift(displacement);
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
        dragType = DragType.ROTATE_ON_THE_SPOT;
        if (e.isShiftDown()) {
            dragType = DragType.PAN;
        } else if (e.isAltDown()) {
            dragType = DragType.ROTATE_AROUND_SURFACE;
        }
    }

    private double distanceToSurface(final Triplex cameraCenter, final Triplex rayVector) {
        final SurfaceProvider.HitAndColor hit = surfaceProvider.firstHit(cameraCenter, rayVector, rayArcAngle(), null);
        return (hit == null) ? Double.NaN : Triplex.subtract(hit.position, cameraCenter).magnitude();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        previousDragPoint = null;
        dragType = null;
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
        final long startTime = System.currentTimeMillis();
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

        final long endTime = System.currentTimeMillis();
        if (false) {
            System.out.println(
                    "Render complete: " + (width * height) + " pixels in " + (endTime - startTime) + " milliseconds.\n" +
                    "\t" + ((double)(width * height) / (endTime - startTime)) + " pixels/ms");
        }
    }
}

