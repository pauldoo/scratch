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

final class IntegerTile extends Tile
{
    private final int[][] values;

    /** Creates a new instance of IntegerTile */
    public IntegerTile(TilePosition position)
    {
        super(position);
        this.values = new int[TilePosition.SIZE][];
        for (int i = 0; i < TilePosition.SIZE; i++) {
            this.values[i] = new int[TilePosition.SIZE];
        }
    }

    public void setValue(final int x, final int y, final int value)
    {
        int offsetX = x - position.getMinX();
        int offsetY = y - position.getMinY();
        if (offsetX >= 0 && offsetX < TilePosition.SIZE && offsetY >= 0 && offsetY < TilePosition.SIZE) {
            values[offsetY][offsetX] = value;
        } else {
            throw new IllegalArgumentException("Out of bounds");
        }
    }

    public int getValue(final int x, final int y)
    {
        int offsetX = x - position.getMinX();
        int offsetY = y - position.getMinY();
        if (offsetX >= 0 && offsetX < TilePosition.SIZE && offsetY >= 0 && offsetY < TilePosition.SIZE) {
            return values[offsetY][offsetX];
        } else {
            throw new IllegalArgumentException("Out of bounds");
        }
    }
}
