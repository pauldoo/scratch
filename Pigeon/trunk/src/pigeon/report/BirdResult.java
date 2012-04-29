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

package pigeon.report;

import java.util.Date;
import pigeon.model.Distance;
import pigeon.model.Time;

/**
    Used to represent a bird's position within a result table.

    Used by the RaceReporter and CompetitionReporter classes.
*/
final class BirdResult implements Comparable<BirdResult>
{
    public final double velocityInMetresPerSecond;
    public final Time time;
    public final Date correctedClockTime;
    public final Distance distance;
    public final StringBuffer html = new StringBuffer();

    public BirdResult(double velocityInMetresPerSecond, Time time, Date correctedClockTime, Distance distance)
    {
        this.velocityInMetresPerSecond = velocityInMetresPerSecond;
        this.time = time;
        this.correctedClockTime = correctedClockTime;
        this.distance = distance;
    }

    public boolean equals(Object rhs)
    {
        return equals((BirdResult)rhs);
    }

    public boolean equals(BirdResult rhs)
    {
        return compareTo(rhs) == 0;
    }

    public int compareTo(BirdResult rhs)
    {
        final BirdResult lhs = this;
        if (lhs == rhs) {
            return 0;
        }

        int result = -Double.compare(lhs.velocityInMetresPerSecond, rhs.velocityInMetresPerSecond);
        if (result == 0) {
            result = lhs.time.compareTo(rhs.time);
        }
        return result;
    }
}
