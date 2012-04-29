/*
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import javax.swing.JComponent;

/**
    Simple component that paints a static Shape object.
*/
final class ShapeComponent extends JComponent
{
    private static final long serialVersionUID = 2630737940019331686L;

    private final Shape shape;
    private final Color color;

    ShapeComponent(Shape shape, Color color)
    {
        this.shape = shape;
        this.color = color;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        paintComponent((Graphics2D)g);
    }

    private void paintComponent(Graphics2D g)
    {
        Utilities.setGraphicsToHighQuality(g);
        g.setColor(color);
        if (shape instanceof Line2D) {
            g.draw(shape);
        } else {
            g.fill(shape);
        }
    }
}
