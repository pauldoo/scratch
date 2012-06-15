/*
    Copyright (c) 2005, 2006, 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.view;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import pigeon.model.Clock;
import pigeon.model.Member;

/**
    Represents a list of clocks for placing into a JTable.

    Displays the associated member and the number of rings entered.
*/
final class ClocksTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 6245142229764744394L;

    private final List<Clock> clocks;

    public ClocksTableModel(List<Clock> clocks)
    {
        this.clocks = clocks;
    }

    public int getRowCount() {
        return clocks.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Member.class;
            case 1:
                return Integer.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Object getValueAt(int row, int column) {
        Clock clock = clocks.get(row);
        switch (column) {
            case 0:
                return clock.getMember();
            case 1:
                return new Integer(clock.getBirdsEntered());
            case 2:
                return new Integer(clock.getTimes().size());
            default:
                throw new IllegalArgumentException();
        }
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Member";
            case 1:
                return "Birds entered";
            case 2:
                return "Birds clocked";
            default:
                throw new IllegalArgumentException();
        }
    }
}
