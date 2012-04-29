/*
    Copyright (c) 2005, 2006, 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.competitions;

public abstract class Competition
{
    /**
        The textual name of the competition as it
        appears on reports etc.
    */
    private final String name;

    /**
        The cost per bird to enter.
    */
    protected final double entryCost;

    /**
        The fraction of the total prize fund that is kept by the
        club before prizes are calculated.
    */
    private final double clubTake;

    /**
        Is this competition ran in the Open?
    */
    private final boolean availableInOpen;

    public Competition(String name, double entryCost, double clubTake, boolean availableInOpen)
    {
        this.name = name;
        this.entryCost = entryCost;
        this.clubTake = clubTake;
        this.availableInOpen = availableInOpen;
    }

    public String getName()
    {
        return name;
    }

    protected void checkPlaceIsInRange(int place, int entrants) throws IllegalArgumentException
    {
        if (place < 1 || place > maximumNumberOfWinners(entrants)) {
            throw new IllegalArgumentException("Place expected to be within the range [1, maximumNumberOfWinners(entrants)].");
        }
    }

    /**
        Given the number of entrants for this competition,
        calculate the maximum number of winners there could
        be.  The actual number of winners may be smaller if
        not enough birds complete the race.
    */
    public abstract int maximumNumberOfWinners(int entrants);

    /**
        Given the position that a bird has come in the competition
        and the total number of entrants, calculate the prize.

        @param place Must be in the range [1, maximumNumberOfWinners(entrants)].
    */
    public abstract double prize(int place, int entrants);

    public double totalPoolMoney(int entrants)
    {
        return entrants * entryCost;
    }

    public double totalClubTake(int entrants)
    {
        return totalPoolMoney(entrants) * getClubTake();
    }

    public boolean isAvailableInOpen()
    {
        return availableInOpen;
    }

    public double getClubTake()
    {
        return clubTake;
    }
}
