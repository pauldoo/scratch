/*
    Copyright (C) 2008  Paul Richards.

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

package fractals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.OverlayLayout;
import javax.swing.event.MouseInputListener;

/**
    GUI component for creating curves as input to the
    bifurcation diagram fractal.
*/
final class CurveEditor implements ComponentListener, MouseInputListener
{
    private final JPanel panel = new JPanel();
    private final double[] controlPointValues;

    CurveEditor(double[] values)
    {
        this.controlPointValues = Utilities.copyDoubleArray(values);

        panel.addComponentListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseListener(this);

        repopulatePanel();
    }

    JComponent asComponent()
    {
        return panel;
    }

    double[] getControlPointValues()
    {
        return Utilities.copyDoubleArray(controlPointValues);
    }

    private void repopulatePanel()
    {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        JLayeredPane layers = new JLayeredPane();
        panel.add(layers);
        layers.setLayout(new OverlayLayout(layers));

        for (int i = 0; i < controlPointValues.length; i++) {
            double x = ((i + 0.5) / controlPointValues.length) * panel.getWidth();
            double y = (1.0 - controlPointValues[i]) * panel.getHeight();
            layers.add(new ShapeComponent(new Ellipse2D.Double(x - 4, y - 4, 8, 8), Color.RED), JLayeredPane.DEFAULT_LAYER);
        }

        InterpolatingCubicSpline spline = new InterpolatingCubicSpline(controlPointValues);
        for (int i = 0; i < panel.getWidth(); i+=4) {
            double x = ((i + 0.5) / panel.getWidth()) * controlPointValues.length - 0.5;
            double y = (1.0 - spline.sample(x)) * panel.getHeight();
            layers.add(new ShapeComponent(new Ellipse2D.Double(i + 0.5 - 1, y - 1, 2, 2), Color.BLACK), JLayeredPane.DEFAULT_LAYER);
        }

        for (int i = 1; i <= 9; i++) {
            double f = i / 10.0;
            layers.add(new ShapeComponent(new Line2D.Double(0, f * panel.getHeight(), panel.getWidth(), f * panel.getHeight()), Color.LIGHT_GRAY), JLayeredPane.DEFAULT_LAYER);
            layers.add(new ShapeComponent(new Line2D.Double(f * panel.getWidth(), 0, f * panel.getWidth(), panel.getHeight()), Color.LIGHT_GRAY), JLayeredPane.DEFAULT_LAYER);
        }

        layers.add(new ShapeComponent(new Rectangle2D.Double(0, 0, panel.getWidth(), panel.getHeight()), Color.WHITE), JLayeredPane.DEFAULT_LAYER);

        layers.setMinimumSize(new Dimension(400, 400));
        layers.setPreferredSize(new Dimension(400, 400));

        JRootPane rootPane = panel.getRootPane();
        if (rootPane != null) {
            rootPane.validate();
        }
    }

    private void updateControlPoint(Point p) {
        int index = (int)Math.floor(p.getX() * controlPointValues.length / panel.getWidth());
        index = Utilities.clamp(0, index, controlPointValues.length-1);
        controlPointValues[index] = 1.0 - (p.getY() / panel.getHeight());
        repopulatePanel();
    }

    public void componentResized(ComponentEvent e) {
        repopulatePanel();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        repopulatePanel();
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        updateControlPoint(e.getPoint());
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        updateControlPoint(e.getPoint());
    }

    public void mouseMoved(MouseEvent e) {
    }
}
