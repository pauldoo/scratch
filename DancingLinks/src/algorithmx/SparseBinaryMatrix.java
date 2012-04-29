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

package algorithmx;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
    Simplistic sparse binary matrix.
*/
public final class SparseBinaryMatrix {

    public static final class BitLocation implements Comparable<BitLocation> {
        public final int fRow;
        public final int fColumn;

        public BitLocation(int row, int column) {
            fRow = row;
            fColumn = column;
        }

        public int compareTo(BitLocation other) {
            if (this == other) {
                return 0;
            } else if (this.fRow != other.fRow) {
                return (new Integer(this.fRow)).compareTo(other.fRow);
            } else {
                return (new Integer(this.fColumn)).compareTo(other.fColumn);
            }
        }
    }

    private final int fRows;
    private final int fColumns;
    private final SortedSet<BitLocation> fOnes;

    public SparseBinaryMatrix(final int rows, final int columns) {
        this.fRows = rows;
        this.fColumns = columns;
        this.fOnes = new TreeSet<BitLocation>();
    }

    public int rowCount() {
        return fRows;
    }

    public int columnCount() {
        return fColumns;
    }

    public void setBit(final BitLocation location, boolean value) {
        if (location.fRow >= 0 && location.fRow < fRows &&
            location.fColumn >= 0 && location.fColumn < fColumns) {
            if (value) {
                fOnes.add(location);
            } else {
                fOnes.remove(location);
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean getBit(final BitLocation location) {
        if (location.fRow >= 0 && location.fRow < fRows &&
            location.fColumn >= 0 && location.fColumn < fColumns) {
            return fOnes.contains(location);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public SortedSet<BitLocation> allOnes() {
        return Collections.synchronizedSortedSet(fOnes);
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append("Rows: " + fRows + "\n");
        result.append("Columns: " + fColumns + "\n");
        for (int row = 0; row < fRows; row++) {
            for (int column = 0; column < fColumns; column++) {
                boolean value = getBit(new BitLocation(row, column));
                result.append(value ? '1' : '0');
                if (column != fColumns - 1) {
                    result.append(",");
                }
            }
            result.append("\n");
        }
        return result.toString();
    }
}
