/*
    Copyright (c) 2005, 2006, 2007, 2008, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pigeon.model.Clock;
import pigeon.model.Constants;
import pigeon.model.Sex;
import pigeon.model.Time;

/**
    Shows the times entered for a clock by listing the ring numbers and times currently entered.
*/
final class TimesTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 2820658767004438666L;

    private final Clock clock;
    private final int daysInRace;
    private final boolean editable;

    public TimesTableModel(Clock clock, int daysInRace, boolean editable)
    {
        this.clock = clock;
        this.daysInRace = daysInRace;
        this.editable = editable;
    }

    @Override
    public int getRowCount() {
        return clock.getTimes().size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    Time getEntry(int row) {
        Comparator<Time> comparator = new Comparator<Time>(){
            @Override
            public int compare(Time o1, Time o2) {
                return Long.valueOf(o1.getMemberTime()).compareTo(o2.getMemberTime());
            }
        };

        List<Time> times = pigeon.model.Utilities.modifiableListCopy(clock.getTimes());
        Collections.sort(times, comparator);
        return times.get(row);
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return Sex.class;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Time entry = getEntry(row);

        switch (column) {
            case 0:
                return entry.getRingNumber();
            case 1:
                return (entry.getMemberTime() / Constants.MILLISECONDS_PER_DAY) + 1;
            case 2:
                return Utilities.TIME_FORMAT_WITHOUT_LOCALE.format(new Date(entry.getMemberTime() % Constants.MILLISECONDS_PER_DAY));
            case 3:
                return entry.getColor();
            case 4:
                return entry.getSex();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Ring Number";
            case 1:
                return "Day";
            case 2:
                return "Clock Time";
            case 3:
                return "Bird Color";
            case 4:
                return "Bird Sex";
            default:
                throw new IllegalArgumentException();
        }
    }
}
