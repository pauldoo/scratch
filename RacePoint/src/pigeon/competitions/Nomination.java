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

package pigeon.competitions;

import javax.naming.OperationNotSupportedException;

public final class Nomination extends Competition
{
    private final double[] payouts;

    public Nomination(String name, double cost, double clubTake, boolean availableInOpen, double[] payouts)
    {
        super(name, cost, clubTake, availableInOpen);
        this.payouts = payouts;

        double totalPayout = 0;
        for (double v: payouts) {
            totalPayout += v;
        }
        if (Math.abs(totalPayout - 1.0) > 1e-6) {
            /**
                We get small errors since binary floating point numbers
                can't precisely represent some finite decimals.
                We must only report an error if we go outside
                some small tollerance.
            */
            throw new IllegalArgumentException("Payouts should total 1.0");
        }
    }

    public int maximumNumberOfWinners(int entrants)
    {
        return Math.min(entrants, payouts.length);
    }

    public double prize(int place, int entrants)
    {
        checkPlaceIsInRange(place, entrants);
        return payouts[place - 1] * entrants * entryCost * (1.0 - getClubTake());
    }
}
