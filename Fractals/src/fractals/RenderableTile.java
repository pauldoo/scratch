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

package fractals;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class RenderableTile extends Tile
{
    private final BufferedImage image;

    RenderableTile(TilePosition position)
    {
        super(position);
        image = new BufferedImage(TilePosition.SIZE, TilePosition.SIZE, BufferedImage.TYPE_INT_RGB);
    }

    public void render(Graphics2D g)
    {
        if (false) {
            boolean success = g.drawImage(image, position.getMinX(), position.getMinY(), null);
            if (!success) {
                throw new RuntimeException("Failed to blit");
            }
        } else {
            AffineTransform saved = g.getTransform();
            g.scale(position.relativeScale(), position.relativeScale());
            g.translate(position.getMinX(), position.getMinY());
            g.drawImage(image, 0, 0, null);
            g.setTransform(saved);
        }
    }

    public void setPixel(int x, int y, int rgb)
    {
        image.setRGB(x - position.getMinX(), y - position.getMinY(), rgb);
    }
}
