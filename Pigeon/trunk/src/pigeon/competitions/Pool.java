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

public final class Pool extends Competition
{
    private final int payoutPeriod;

    public Pool(String name, double entryCost, double clubTake, boolean availableInOpen, int payoutPeriod)
    {
        super(name, entryCost, clubTake, availableInOpen);
        this.payoutPeriod = payoutPeriod;
    }

    public int maximumNumberOfWinners(int entrants)
    {
        double winners = ((1.0 - getClubTake()) * entrants)  / payoutPeriod;
        return (int)Math.ceil(winners);
    }

    public double prize(int place, int entrants)
    {
        checkPlaceIsInRange(place, entrants);
        final double prize = Math.min(1.0, ((1.0 - getClubTake()) * entrants / payoutPeriod) - (place - 1)) * entryCost * payoutPeriod;
        return prize;
    }
}
