/*
    Copyright (c) 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputListener;

final class CanvasViewInputHandler implements MouseInputListener, MouseWheelListener, KeyListener
{

    CanvasView canvasView;
    private Point previousPoint;

    public CanvasViewInputHandler(CanvasView canvasView)
    {
        super();
        this.canvasView = canvasView;
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
        //System.out.println(e);
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            updateDrag(e.getPoint(), e.isShiftDown());
        }
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseMoved(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
        //System.out.println(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            previousPoint = e.getPoint();
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        //System.out.println(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
            updateDrag(e.getPoint(), e.isShiftDown());
            previousPoint = null;
        }
    }

    private void updateDrag(Point currentPoint, boolean isRotate)
    {
        if (isRotate) {
            double angleOld = Math.atan2(previousPoint.y - 300, previousPoint.x - 400);
            double angleNew = Math.atan2(currentPoint.y - 300, currentPoint.x - 400);
            canvasView.rotateBy(angleNew - angleOld);
        } else {
            int dispX = currentPoint.x - previousPoint.x;
            int dispY = currentPoint.y - previousPoint.y;
            canvasView.moveBy(dispX, dispY);
        }
        previousPoint = currentPoint;
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        switch (Integer.signum(e.getWheelRotation())) {
            case -1:
                // Up rotation
                canvasView.zoomBy(1);
                break;
            case 1:
                // Down rotation
                canvasView.zoomBy(-1);
                break;
            default:
                throw new IllegalArgumentException("getWheelRotation() reported zero");
        }
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
        //System.out.println(e);
        switch (e.getKeyChar()) {
            case '+':
                canvasView.zoomBy(1);
                break;
            case '-':
                canvasView.zoomBy(-1);
                break;
            default:
        }
    }

    public void keyTyped(KeyEvent e)
    {
    }
}
