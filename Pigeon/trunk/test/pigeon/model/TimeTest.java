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

package pigeon.model;

import junit.framework.*;

/**
 *
 * @author pauldoo
 */
public final class TimeTest extends TestCase {

    public TimeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TimeTest.class);

        return suite;
    }

    public void testEquality() throws ValidationException {
        String ringNumberFoo = "foo";
        String ringNumberBar = "bar";
        final int days = 3;

        Time result1 = Time.createEmpty();
        result1 = result1.repSetRingNumber(ringNumberFoo);
        result1 = result1.repSetMemberTime(1, days);

        {
            Time result2 = Time.createEmpty();
            result2 = result2.repSetRingNumber(ringNumberFoo);
            result2 = result2.repSetMemberTime(1, days);
            assertEquals(result1, result2);
        }
        {
            Time result2 = Time.createEmpty();
            result2 = result2.repSetRingNumber(ringNumberFoo);
            result2 = result2.repSetMemberTime(2, days);
            assertEquals(result1, result2);
        }

        {
            Time result2 = Time.createEmpty();
            result2 = result2.repSetRingNumber(ringNumberBar);
            result2 = result2.repSetMemberTime(1, days);
            assertFalse(result1.equals(result2));
        }
        {
            Time result2 = Time.createEmpty();
            result2 = result2.repSetRingNumber(ringNumberBar);
            result2 = result2.repSetMemberTime(2, days);
            assertFalse(result1.equals(result2));
        }
    }
}
