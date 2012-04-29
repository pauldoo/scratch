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

import junit.framework.*;
import java.util.Date;

/**
 *
 * @author Paul
 */
public final class RaceTest extends TestCase {

    public RaceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RaceTest.class);

        return suite;
    }

    public void testClockOpenOffset() throws ValidationException {
        final long today = pigeon.model.Utilities.beginningOfCalendarDay(new Date()).getTime();

        Race race = Race.createEmpty();
        race = race.repSetLiberationDate(new Date(today + 1000000));
        race = race.repSetDaysCovered(2);
        assertEquals(today, race.liberationDayOffset().getTime());
    }

    public void testEquality() throws ValidationException {
        Date dateFoo = new Date(1);
        Date dateBar = new Date(2);
        Racepoint racepointFoo = Racepoint.createEmpty().repSetName("Foo");
        Racepoint racepointBar = Racepoint.createEmpty().repSetName("Bar");

        Race race1 = Race.createEmpty();
        race1 = race1.repSetLiberationDate(dateFoo);
        race1 = race1.repSetRacepoint(racepointFoo);

        {
            Race race2 = Race.createEmpty();
            race2 = race2.repSetLiberationDate(dateFoo);
            race2 = race2.repSetRacepoint(racepointFoo);
            assertEquals(race1, race2);
        }
        {
            Race race2 = Race.createEmpty();
            race2 = race2.repSetLiberationDate(dateFoo);
            race2 = race2.repSetRacepoint(racepointBar);
            assertFalse(race1.equals(race2));
        }
        {
            Race race2 = Race.createEmpty();
            race2 = race2.repSetLiberationDate(dateBar);
            race2 = race2.repSetRacepoint(racepointFoo);
            assertFalse(race1.equals(race2));
        }
    }

}
