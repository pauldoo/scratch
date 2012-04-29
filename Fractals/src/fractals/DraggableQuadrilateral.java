/*
    Copyright (c) 2008, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.event.MouseInputListener;

/**
    A Quadrilateral gui widget that sits without a layout manager and
    can be dragged around by the 4 corners.
*/
final class DraggableQuadrilateral extends JComponent implements MouseInputListener
{
    private static final long serialVersionUID = 4705086044490724806L;

    /// How close must the mouse pointer be to count as selecting.
    private static final double SELECTING_FUZZ = 10.0;
    /// Stroke used to render the quad.
    private static final Stroke NORMAL_STROKE = new BasicStroke(2.0f);
    /// Stroke used to render the quad.
    private static final Stroke FINE_STROKE = new BasicStroke(1.0f);
    /// Stroke used to render an outline shape used for selection.
    private static final Stroke SELECTING_STROKE = new BasicStroke((float)SELECTING_FUZZ);

    private Point2D.Double cornerA;
    private Point2D.Double cornerB;
    private Point2D.Double cornerC;
    private Point2D.Double cornerD;

    private boolean isBeingHoveredOver = false;
    private Point dragStart = null;
    /**
        If the user is dragging a corner of the quad then this value aliases
        whichever corner is being dragged (only for the duration of the drag).
    */
     private Point2D.Double dragCorner = null;

     private final List<Listener> listeners = new ArrayList<Listener>();

    DraggableQuadrilateral()
    {
        Random rng = new Random();
        cornerA = new Point2D.Double(100 + rng.nextInt(100), 100 + rng.nextInt(100));
        cornerB = new Point2D.Double(300 + rng.nextInt(100), 100 + rng.nextInt(100));
        cornerC = new Point2D.Double(300 + rng.nextInt(100), 300 + rng.nextInt(100));
        cornerD = new Point2D.Double(100 + rng.nextInt(100), 300 + rng.nextInt(100));

        setOpaque(false);
        setRequestFocusEnabled(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    void addListener(Listener listener){
        listeners.add(listener);
    }

    private void notifyListeners()
    {
        for (Listener listener: listeners) {
            listener.draggableQuadrilateralHasMoved(this);
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        paintComponent((Graphics2D)g);
    }

    public void paintComponent(Graphics2D g)
    {
        Utilities.setGraphicsToHighQuality(g);

        g.setColor(isBeingHoveredOver ? Color.RED : Color.BLACK);
        g.setStroke(NORMAL_STROKE);
        g.draw(getShape());
        if (isBeingHoveredOver) {
            g.setStroke(FINE_STROKE);
            final double width = getBounds().width;
            final double height = getBounds().height;
            g.draw(new Line2D.Double(IteratedFunctionSystem.GAP_FROM_EDGE, IteratedFunctionSystem.GAP_FROM_EDGE, cornerA.getX(), cornerA.getY()));
            g.draw(new Line2D.Double(width - IteratedFunctionSystem.GAP_FROM_EDGE, IteratedFunctionSystem.GAP_FROM_EDGE, cornerB.getX(), cornerB.getY()));
            g.draw(new Line2D.Double(width - IteratedFunctionSystem.GAP_FROM_EDGE, height - IteratedFunctionSystem.GAP_FROM_EDGE, cornerC.getX(), cornerC.getY()));
            g.draw(new Line2D.Double(IteratedFunctionSystem.GAP_FROM_EDGE, height - IteratedFunctionSystem.GAP_FROM_EDGE, cornerD.getX(), cornerD.getY()));
        }
    }

    public Shape getShape()
    {
        Polygon result = new Polygon();
        result.addPoint((int)Math.round(cornerA.getX()), (int)Math.round(cornerA.getY()));
        result.addPoint((int)Math.round(cornerB.getX()), (int)Math.round(cornerB.getY()));
        result.addPoint((int)Math.round(cornerC.getX()), (int)Math.round(cornerC.getY()));
        result.addPoint((int)Math.round(cornerD.getX()), (int)Math.round(cornerD.getY()));
        return result;
    }

    Shape getSelectingOutlineShape()
    {
        return SELECTING_STROKE.createStrokedShape(getShape());
    }

    private static Point2D.Double displacePoint(Point2D.Double p, double dx, double dy)
    {
        return new Point2D.Double(p.getX() + dx, p.getY() + dy);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        if (getSelectingOutlineShape().contains(e.getPoint())) {
            dragStart = e.getPoint();
            Point2D.Double closestCorner = cornerA;
            if (e.getPoint().distance(cornerB) < e.getPoint().distance(closestCorner)) {
                closestCorner = cornerB;
            }
            if (e.getPoint().distance(cornerC) < e.getPoint().distance(closestCorner)) {
                closestCorner = cornerC;
            }
            if (e.getPoint().distance(cornerD) < e.getPoint().distance(closestCorner)) {
                closestCorner = cornerD;
            }

            if (e.getPoint().distance(closestCorner) <= SELECTING_FUZZ) {
                dragCorner = closestCorner;
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        dragStart = null;
        dragCorner = null;
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
        if (dragStart != null) {
            if (dragCorner != null) {
                dragCorner.setLocation(e.getPoint());
            } else {
                Point p = e.getPoint();
                double dx = p.x - dragStart.x;
                double dy = p.y - dragStart.y;
                dragStart = p;
                cornerA = displacePoint(cornerA, dx, dy);
                cornerB = displacePoint(cornerB, dx, dy);
                cornerC = displacePoint(cornerC, dx, dy);
                cornerD = displacePoint(cornerD, dx, dy);
            }
            notifyListeners();
        }
    }

    public void mouseMoved(MouseEvent e)
    {
        boolean nowBeingHoveredOver = getSelectingOutlineShape().contains(e.getPoint());
        if (nowBeingHoveredOver != isBeingHoveredOver) {
            isBeingHoveredOver = nowBeingHoveredOver;
            repaint();
        }
        if (!isBeingHoveredOver) {
            /*
                This appears to be a bit of a hack.  We move ourselves to the back
                of the stack and so when the mouse moves by the next pixel a different
                object will get a chance to do hit detection.
                So with N objects on the canvas, each one only gets a chance to check
                for mouse floating every N pixels of mouse movement.
            */
            JLayeredPane canvas = (JLayeredPane)getParent();
            Component c = canvas.getComponentAt(e.getPoint());
            canvas.moveToBack(this);
        }
    }

    public Point2D.Double getCornerA()
    {
        return (Point2D.Double)cornerA.clone();
    }

    public Point2D.Double getCornerB()
    {
        return (Point2D.Double)cornerB.clone();
    }

    public Point2D.Double getCornerC()
    {
        return (Point2D.Double)cornerC.clone();
    }

    public Point2D.Double getCornerD()
    {
        return (Point2D.Double)cornerD.clone();
    }

    static interface Listener
    {
        public void draggableQuadrilateralHasMoved(DraggableQuadrilateral source);
    }
}
