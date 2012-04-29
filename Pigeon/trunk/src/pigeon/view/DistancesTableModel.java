/*
    Copyright (c) 2005, 2006, 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.view;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import pigeon.model.Distance;
import pigeon.model.Member;

/**
 * Generic final class to edit distances associated with a list of objects (members or racepoints).
 *
 * Used by the DistanceEditor.
 */
final class DistancesTableModel<Target> extends AbstractTableModel {

    private static final long serialVersionUID = 3953261892856570881L;

    private final String targetTitle;
    private final Map<Target, Distance> distances;
    private final boolean editable;

    /** Creates a new instance of DistancesTableModel */
    public DistancesTableModel(String targetTitle, Map<Target, Distance> distances, boolean editable) {
        this.targetTitle = targetTitle;
        this.distances = distances;
        this.editable = editable;
    }

    public int getRowCount() {
        return distances.size();
    }

    public int getColumnCount() {
        return 3;
    }

    private Map.Entry<Target, Distance> getEntry(int row) {
        Set<Map.Entry<Target, Distance>> entries = distances.entrySet();
        Iterator<Map.Entry<Target, Distance>> iter = entries.iterator();
        for (int i = 0; i < row; i++) {
           iter.next();
        }
        return iter.next();
    }

    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Object getValueAt(int row, int column) {
        Map.Entry<Target, Distance> entry = getEntry(row);

        switch (column) {
            case 0:
                return entry.getKey();
            case 1:
                return entry.getValue().getMiles();
            case 2:
                return entry.getValue().getYardsRemainder();
            default:
                throw new IllegalArgumentException();
        }
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return targetTitle;
            case 1:
                return "Miles";
            case 2:
                return "Yards";
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean isCellEditable(int row, int column) {
        switch (column) {
            case 0:
                return false;
            case 1:
            case 2:
                return true;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setValueAt(Object value, int row, int column) {
        Map.Entry<Target, Distance> entry = getEntry(row);
        switch (column) {
            case 1: {
                int miles = Integer.parseInt(value.toString());
                int yards = entry.getValue().getYardsRemainder();
                Distance newValue = Distance.createFromImperial(miles,  yards);
                entry.setValue( newValue );
                fireTableRowsUpdated(row, row);
                break;
            }
            case 2: {
                int miles = entry.getValue().getMiles();
                int yards = Integer.parseInt(value.toString());
                Distance newValue = Distance.createFromImperial(miles,  yards);
                entry.setValue( newValue );
                fireTableRowsUpdated(row, row);
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

}
