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

import java.util.Date;
import junit.framework.*;

/**
 *
 * @author pauldoo
 */
public final class ClockTest extends TestCase {

    public ClockTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClockTest.class);

        return suite;
    }

    /**
     * Test of ConvertMemberTimeToMasterTime method, of final class pigeon.model.Clock.
     * @throws ValidationException
     */
    public void testConvertMemberTimeToMasterTime() throws ValidationException {
        final long today = pigeon.model.Utilities.beginningOfCalendarDay(new Date()).getTime();

        Clock clock = Clock.createEmpty();
        clock = clock.repSetTimeOnMasterWhenSet(new Date(today + 200));
        clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + 10000600));
        clock = clock.repSetTimeOnMemberWhenSet(new Date(today + 3000));
        clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + 10007000));

        assertEquals(today + 200, clock.getTimeOnMasterWhenSet().getTime());
        assertEquals(today + 10000600, clock.getTimeOnMasterWhenOpened().getTime());
        assertEquals(today + 3000, clock.getTimeOnMemberWhenSet().getTime());
        assertEquals(today + 10007000, clock.getTimeOnMemberWhenOpened().getTime());

        Race race = Race.createEmpty();
        race = race.repSetLiberationDate(new Date(today));
        race = race.repSetDaysCovered(1);

        assertEquals(today + 200, clock.convertMemberTimeToMasterTime(new Date(3000), race).getTime());
        assertEquals(today + 5000200, clock.convertMemberTimeToMasterTime(new Date(5005000), race).getTime());
        assertEquals(today + 10000200, clock.convertMemberTimeToMasterTime(new Date(10007000), race).getTime());
    }

    /**
     * Test clock variation example in rulebook.
     * @throws ValidationException
     */
    public void testClockVariationCalculationExample() throws ValidationException {
        final long today = pigeon.model.Utilities.beginningOfCalendarDay(new Date()).getTime();

        {
            // Member clock gains 2m 40s
            Clock clock = Clock.createEmpty();
            clock = clock.repSetTimeOnMasterWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMemberWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + toMs(1, 21, 40, 50)));
            clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + toMs(1, 21, 43, 30)));

            Race race = Race.createEmpty();
            race = race.repSetLiberationDate(new Date(today));
            race = race.repSetDaysCovered(2);

            final long birdTime = toMs(1, 14, 30, 25);
            final long correctedTime = clock.convertMemberTimeToMasterTime(new Date(birdTime), race).getTime() - today;
            assertEquals(-113 * 1000, correctedTime - birdTime);
        }
        {
            // Member clock is spot on
            Clock clock = Clock.createEmpty();
            clock = clock.repSetTimeOnMasterWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMemberWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + toMs(1, 21, 40, 50)));
            clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + toMs(1, 21, 40, 50)));

            Race race = Race.createEmpty();
            race = race.repSetLiberationDate(new Date(today));
            race = race.repSetDaysCovered(2);

            final long birdTime = toMs(1, 14, 30, 25);
            final long correctedTime = clock.convertMemberTimeToMasterTime(new Date(birdTime), race).getTime() - today;
            assertEquals(0, correctedTime - birdTime);
        }
        {
            // Member clock loses 2m 40s
            Clock clock = Clock.createEmpty();
            clock = clock.repSetTimeOnMasterWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMemberWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + toMs(1, 21, 40, 50)));
            clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + toMs(1, 21, 38, 10)));

            Race race = Race.createEmpty();
            race = race.repSetLiberationDate(new Date(today));
            race = race.repSetDaysCovered(2);

            final long birdTime = toMs(1, 14, 30, 25);
            final long correctedTime = clock.convertMemberTimeToMasterTime(new Date(birdTime), race).getTime() - today;
            assertEquals(114 * 1000, correctedTime - birdTime);
        }
        {
            // Member clock loses 5m (trigger double correction)
            Clock clock = Clock.createEmpty();
            clock = clock.repSetTimeOnMasterWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMemberWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + toMs(1, 21, 40, 50)));
            clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + toMs(1, 21, 35, 50)));

            Race race = Race.createEmpty();
            race = race.repSetLiberationDate(new Date(today));
            race = race.repSetDaysCovered(2);

            final long birdTime = toMs(1, 14, 30, 25);
            final long correctedTime = clock.convertMemberTimeToMasterTime(new Date(birdTime), race).getTime() - today;
            assertEquals(428 * 1000, correctedTime - birdTime); // corrected by over 7 minutes
        }
        {
            // Member clock gains 5m (trigger no correction)
            Clock clock = Clock.createEmpty();
            clock = clock.repSetTimeOnMasterWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMemberWhenSet(new Date(today + toMs(0, 20, 55, 20)));
            clock = clock.repSetTimeOnMasterWhenOpened(new Date(today + toMs(1, 21, 40, 50)));
            clock = clock.repSetTimeOnMemberWhenOpened(new Date(today + toMs(1, 21, 45, 50)));

            Race race = Race.createEmpty();
            race = race.repSetLiberationDate(new Date(today));
            race = race.repSetDaysCovered(2);

            final long birdTime = toMs(1, 14, 30, 25);
            final long correctedTime = clock.convertMemberTimeToMasterTime(new Date(birdTime), race).getTime() - today;
            assertEquals(0, correctedTime - birdTime);
        }
    }

    private static long toMs(int days, int hours, int minutes, int seconds) {
        if (hours < 0 || hours >= 24) {
            throw new IllegalArgumentException("Hours outwith one day");
        }
        if (minutes < 0 || minutes >= 60) {
            throw new IllegalArgumentException("Minutes outwith one hour");
        }
        if (seconds < 0 || seconds >= 60) {
            throw new IllegalArgumentException("Seconds outwith one minute");
        }
        hours += days * 24;
        minutes += hours * 60;
        seconds += minutes * 60;
        return seconds * 1000;
    }
}
