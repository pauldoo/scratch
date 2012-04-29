/*
    Copyright (c) 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package tuner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

/**
    A swing component that displays a line graph of the freqencies.
*/
final class FrequencyCurve extends JComponent implements SpectralView
{
    private static final long serialVersionUID = 8161190134655084490L;

    private double targetFrequency;
    private SinglePickup[] pickups;

    public FrequencyCurve()
    {
        this.setDoubleBuffered(true);
        setTargetFrequency(261);
        setMinimumSize(new Dimension(400, 100));
        setPreferredSize(new Dimension(400, 100));
    }

    public synchronized void process(AudioPacket packet)
    {
        for (SinglePickup pickup: pickups) {
            pickup.process(packet);
        }
        repaint();
    }

    @Override
    public synchronized void paint(Graphics graphicsOneDee)
    {
        Graphics2D g = (Graphics2D)graphicsOneDee;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double maximumResponse = 0;
        for (SinglePickup pickup: pickups) {
            double response = pickup.getResponseMeasure();
            if (response > maximumResponse) {
                maximumResponse = response;
            }
        }
        if (maximumResponse == 0) {
            maximumResponse = 1.0;
        }

        g.setBackground(Color.BLACK);

        Rectangle clipBounds = g.getClipBounds();
        g.clearRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        Dimension size = getSize();
        for (int i = 0; i < pickups.length; i++) {
            double x = (((double)i) / (pickups.length - 1)) * (size.width - 1);
            double y = (1.0 - (pickups[i].getResponseMeasure() / maximumResponse)) * (size.height - 1);

            int offsetFromTarget = i - ((pickups.length - 1) / 2);
            if (offsetFromTarget == 0) {
                g.setColor(Color.WHITE);
            } else if (offsetFromTarget % 8 == 0) {
                g.setColor(Color.LIGHT_GRAY);
            } else {
                g.setColor(Color.DARK_GRAY);
            }
            g.fill(new Rectangle2D.Double(x - 1, 0, 2, size.height));
            g.setColor(Color.YELLOW);
            g.fill(new Ellipse2D.Double(x - 7, y - 3, 14, 6));
        }
    }

    public synchronized void setTargetFrequency(double frequency)
    {
        System.out.println("setTargetFrequency( " + frequency + " )");
        pickups = new SinglePickup[12 * 2 + 1];
        for (int i = -12; i <= 12; i++) {
            final double semitoneDelta = i / 8.0;
            double pickupFrequency = frequency * Math.pow(2.0, semitoneDelta / 12);
            pickups[i+12] = new SinglePickup(pickupFrequency, 4.0);
        }
        repaint();
    }
}
