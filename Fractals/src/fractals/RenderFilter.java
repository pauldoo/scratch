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

import java.awt.Color;

final class RenderFilter implements TileProvider<RenderableTile>
{
    private final TileProvider<IntegerTile> source;
    private final double exposure;

    RenderFilter(TileProvider<IntegerTile> source, double exposure)
    {
        this.source = source;
        this.exposure = exposure;
    }

    public RenderableTile getTile(TilePosition position)
    {
        IntegerTile sourceTile = source.getTile(position);
        RenderableTile result = new RenderableTile(position);
        for (int y = position.getMinY(); y <= position.getMaxY(); y++) {
            for (int x = position.getMinX(); x <= position.getMaxX(); x++) {
                float v = (float)Utilities.expose(sourceTile.getValue(x, y), exposure);
                result.setPixel(x, y, new Color(v, v, v).getRGB());
            }
        }
        return result;
    }
}
