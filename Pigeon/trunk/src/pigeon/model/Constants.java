/*
    Copyright (c) 2005, 2006, 2007, 2008, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.model;

/**
    Some simple constants used for conversions.
*/
public final class Constants
{
    // Non-Creatable
    private Constants()
    {
    }

    public static final double METRES_PER_YARD = 0.9144;
    public static final int YARDS_PER_MILE = 1760;
    public static final double METRES_PER_SECOND_TO_YARDS_PER_MINUTE = 60 / METRES_PER_YARD;

    public static final long MILLISECONDS_PER_SECOND = 1000;
    public static final long MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
    public static final long MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;
}
