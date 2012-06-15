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

package pigeon.model;

import junit.framework.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author pauldoo
 */
public final class UtilitiesTest extends TestCase
{

    public UtilitiesTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
    }

    protected void tearDown() throws Exception
    {
    }

    public void testRoundToNearestSecond()
    {
        assertEquals(0, Utilities.roundToNearestSecond(0));
        assertEquals(0, Utilities.roundToNearestSecond(499));
        assertEquals(1000, Utilities.roundToNearestSecond(500));
        assertEquals(1000, Utilities.roundToNearestSecond(501));

        assertEquals(0, Utilities.roundToNearestSecond(-0));
        assertEquals(0, Utilities.roundToNearestSecond(-499));
        assertEquals(-1000, Utilities.roundToNearestSecond(-500));
        assertEquals(-1000, Utilities.roundToNearestSecond(-501));
    }

}
