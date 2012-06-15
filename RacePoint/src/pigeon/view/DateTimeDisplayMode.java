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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
    Enum class which handles the different date and time formats
    used within the app.

    Some Date instances are real absolute values whose values can
    be treated literally.  Other Date instances are relative, and are
    an offset within a single 24hour day.  This latter kind are stored
    as Date instances whose values are within 24hours of the epoch.
*/
public enum DateTimeDisplayMode
{
    DATE,
    DATE_HOURS_MINUTES,
    DATE_HOURS_MINUTES_SECONDS,
    HOURS_MINUTES,
    HOURS_MINUTES_SECONDS;

    public int getDisplayColumns()
    {
        return getFormat().toPattern().length();
    }

    /**
        Returns true iff this mode is intended to represent a 24 hour
        time with a range of a single day (within 24hours of the epoch).
    */
    public boolean isIntendedFor24HourRelativeFormat()
    {
        switch (this)
        {
            case DATE:
            case DATE_HOURS_MINUTES:
            case DATE_HOURS_MINUTES_SECONDS:
                return false;
            case HOURS_MINUTES:
            case HOURS_MINUTES_SECONDS:
                return true;
            default:
                throw new IllegalArgumentException();
        }
    }

    public SimpleDateFormat getFormat()
    {
        SimpleDateFormat result;
        switch (this)
        {
            case DATE:
                result = new SimpleDateFormat("dd/MM/yy");
                break;
            case DATE_HOURS_MINUTES:
                result = new SimpleDateFormat("dd/MM/yy HH:mm");
                break;
            case DATE_HOURS_MINUTES_SECONDS:
                result = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                break;
            case HOURS_MINUTES:
                result = new SimpleDateFormat("HH:mm");
                break;
            case HOURS_MINUTES_SECONDS:
                result = new SimpleDateFormat("HH:mm:ss");
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (isIntendedFor24HourRelativeFormat()) {
            result.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return result;
    }
}
