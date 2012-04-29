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

import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pigeon.model.Race;
import pigeon.model.Racepoint;

/**
 * Represents a list of races for use in a JTable, displays liberation date and racepoint name.
 */
final class RacesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 6868265813681434543L;

    private final List<Race> races;

    public RacesTableModel(List<Race> races) {
        this.races = races;
    }

    public int getRowCount() {
        return races.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Date.class;
            case 1:
                return Racepoint.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Object getValueAt(int row, int column) {
        Race race = races.get(row);
        switch (column) {
            case 0:
                return race.getLiberationDate();
            case 1:
                return race.getRacepoint();
            default:
                throw new IllegalArgumentException();
        }
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Date";
            case 1:
                return "Racepoint";
            default:
                throw new IllegalArgumentException();
        }
    }
}
