/*
    Copyright (c) 2009, 2012 Paul Richards <paul.richards@gmail.com>

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

package sknat.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.swing.JComponent;
import sknat.model.UnitProperties;

/**
    Simple Swing widget that renders a single unit scaled to fit.
*/
public final class UnitView extends JComponent
{
    private UnitProperties unitProperties = null;

    public UnitView()
    {
        setPreferredSize(new Dimension(200, 200));
    }

    public void setUnitProperties(UnitProperties properties)
    {
        this.unitProperties = properties;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        paintComponentImpl((Graphics2D)g);
    }

    private void paintComponentImpl(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        g.setBackground(Color.BLACK);
        Rectangle clipBounds = g.getClipBounds();
        g.clearRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        g.setColor(Color.red);
        if (unitProperties == null) {
            g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        } else {
            g.translate(getWidth() * 0.5, getHeight() * 0.5);
            final double scale = Math.min(getWidth(), getHeight()) * 0.5 * 0.75;
            g.scale(scale, scale);
            g.rotate(System.currentTimeMillis() * 0.001);

            g.setStroke(new BasicStroke((float)(unitProperties.defense * 0.24 + 0.01)));
            Shape body = new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0);
            g.draw(body);
        }
    }
}
