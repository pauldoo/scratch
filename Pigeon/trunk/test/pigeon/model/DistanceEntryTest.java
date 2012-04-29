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
 * @author Paul
 */
public final class DistanceEntryTest extends TestCase {

    public DistanceEntryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DistanceEntryTest.class);

        return suite;
    }

    public void testEquality() throws ValidationException {
        Member memberFoo = Member.createEmpty();
        memberFoo = memberFoo.repSetName("foo");
        Member memberBar = Member.createEmpty();
        memberBar = memberBar.repSetName("bar");

        Racepoint racepointFoo = Racepoint.createEmpty();
        racepointFoo = racepointFoo.repSetName("foo");
        Racepoint racepointBar = Racepoint.createEmpty();
        racepointBar = racepointBar.repSetName("bar");

        Distance distanceFoo = Distance.createFromMetric(1);
        Distance distanceBar = Distance.createFromMetric(2);
        DistanceEntry entry1 = new DistanceEntry(memberFoo, racepointFoo, distanceFoo);

        {
            DistanceEntry entry2 = new DistanceEntry(memberFoo, racepointFoo, distanceFoo);
            assertEquals(entry1, entry2);
        }
        {
            DistanceEntry entry2 = new DistanceEntry(memberFoo, racepointFoo, distanceBar);
            assertEquals(entry1, entry2);
        }

        {
            DistanceEntry entry2 = new DistanceEntry(memberFoo, racepointBar, distanceFoo);
            assertFalse(entry1.equals(entry2));
        }
        {
            DistanceEntry entry2 = new DistanceEntry(memberFoo, racepointBar, distanceBar);
            assertFalse(entry1.equals(entry2));
        }

        {
            DistanceEntry entry2 = new DistanceEntry(memberBar, racepointFoo, distanceFoo);
            assertFalse(entry1.equals(entry2));
        }
        {
            DistanceEntry entry2 = new DistanceEntry(memberBar, racepointFoo, distanceBar);
            assertFalse(entry1.equals(entry2));
        }

        {
            DistanceEntry entry2 = new DistanceEntry(memberBar, racepointBar, distanceFoo);
            assertFalse(entry1.equals(entry2));
        }
        {
            DistanceEntry entry2 = new DistanceEntry(memberBar, racepointBar, distanceBar);
            assertFalse(entry1.equals(entry2));
        }
    }

}
