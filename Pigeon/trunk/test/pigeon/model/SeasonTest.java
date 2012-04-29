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
 * @author Paul
 */
public final class SeasonTest extends TestCase {

    public SeasonTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SeasonTest.class);

        return suite;
    }

    public void testRemove() throws ValidationException {
        Racepoint racepoint = Racepoint.createEmpty();
        Date liberationDate = new Date(1);
        Season season = Season.createEmpty();

        Race race1 = Race.createEmpty();
        race1 = race1.repSetRacepoint(racepoint);
        race1 = race1.repSetLiberationDate(liberationDate);

        assertEquals(season.getRaces().size(), 0);
        season = season.repAddRace(race1);
        assertEquals(season.getRaces().size(), 1);
        season = season.repRemoveRace(race1);
        assertEquals(season.getRaces().size(), 0);
    }

    public void testClashes() throws ValidationException {
        Racepoint racepoint = Racepoint.createEmpty();
        Date liberationDate = new Date(1);
        Season season = Season.createEmpty();

        Race race1 = Race.createEmpty();
        race1 = race1.repSetRacepoint(racepoint);
        race1 = race1.repSetLiberationDate(liberationDate);
        Race race2 = Race.createEmpty();
        race2 = race2.repSetRacepoint(racepoint);
        race2 = race2.repSetLiberationDate(liberationDate);

        season = season.repAddRace(race1);
        try {
            season = season.repAddRace(race2);
            assertFalse("Should throw", true);
        } catch (ValidationException ex) {
            assertEquals("Race already exists", ex.toString());
        }
    }

}
