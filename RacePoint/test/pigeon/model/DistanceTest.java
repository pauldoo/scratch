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
import java.io.Serializable;

/**
 *
 * @author Paul
 */
public final class DistanceTest extends TestCase {

    public DistanceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DistanceTest.class);

        return suite;
    }

    /**
     * Test of createFromImperial method, of final class pigeon.model.Distance.
     */
    public void testImperial() {
        final int YARDS_PER_MILE = 1760;
        // Test that the Distance final class is accurate to the nearest yard for distances up to 3000 miles
        for (int yards = 0; yards < 3000 * YARDS_PER_MILE; yards++) {
            Distance d = Distance.createFromImperial(0, yards);
            assertEquals(yards, Math.round(d.getYards()));
        }
    }

}
