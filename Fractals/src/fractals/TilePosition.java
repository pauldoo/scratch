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

final class TilePosition implements Comparable<TilePosition>
{
    /**
        Width and height (in pixels) of each tile.
    */
    public static final int SIZE = 64;

    /**
        At each zoom level, the tiles at the next layer are this many times smaller
        (in the physical sense, not in the number of pixels it stores).
    */
    public static final double SCALE_POWER = 2.0;

    private final int indexX;
    private final int indexY;
    private final int zoomIndex;

    /** Creates a new instance of TilePosition */
    public TilePosition(int indexX, int indexY, int zoomIndex)
    {
        this.indexX = indexX;
        this.indexY = indexY;
        this.zoomIndex = zoomIndex;
    }

    @Override
    public int hashCode()
    {
        return (indexX * 11) ^ (indexY * 13) ^ (zoomIndex * 17);
    }

    @Override
    public boolean equals(Object o)
    {
        return equals((TilePosition)o);
    }

    public boolean equals(TilePosition other)
    {
        return
            this == other || (
                this.indexX == other.indexX &&
                this.indexY == other.indexY &&
                this.zoomIndex == other.zoomIndex
            );
    }

    public int compareTo(TilePosition other)
    {
        int result = 0;
        if (result == 0) {
            result = Integer.signum(this.indexX - other.indexX);
        }
        if (result == 0) {
            result = Integer.signum(this.indexY - other.indexY);
        }
        if (result == 0) {
            result = Integer.signum(this.zoomIndex - other.zoomIndex);
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "{" + getMinX() + ", " + getMinY() + ", " + getZoomIndex() + "}";
    }

    public double relativeScale()
    {
        return 1.0 / Math.pow(SCALE_POWER, getZoomIndex());
    }

    public int getMinX()
    {
        return indexX * SIZE;
    }

    public int getMinY()
    {
        return indexY * SIZE;
    }

    public int getMaxX()
    {
        return getMinX() + SIZE - 1;
    }

    public int getMaxY()
    {
        return getMinY() + SIZE - 1;
    }

    public int getZoomIndex()
    {
        return zoomIndex;
    }
}
