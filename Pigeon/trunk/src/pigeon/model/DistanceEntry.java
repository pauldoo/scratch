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

import java.io.Serializable;

/**
    Associates a distance measurement with a member and racepoint pair.
*/
public final class DistanceEntry implements Serializable, Comparable<DistanceEntry> {

    private static final long serialVersionUID = 8618199323315444879L;

    private final Member member;
    private final Racepoint racepoint;
    private final Distance distance;

    public DistanceEntry(Member member, Racepoint racepoint, Distance distance) {
        this.member = member;
        this.racepoint = racepoint;
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        return member.hashCode() ^ racepoint.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return equals((DistanceEntry) other);
    }

    public boolean equals(DistanceEntry other) {
        if (this == other) {
            return true;
        } else {
            return this.member.equals(other.member) && this.racepoint.equals(other.racepoint);
        }
    }

    @Override
    public int compareTo(DistanceEntry other) {
        int retval;
        retval = this.member.compareTo( other.member );
        if (retval != 0) {
            return retval;
        }
        retval = this.racepoint.compareTo( other.racepoint );
        if (retval != 0) {
            return retval;
        }
        return 0;
    }

    public Member getMember() {
        return member;
    }

    public Racepoint getRacepoint() {
        return racepoint;
    }

    public Distance getDistance() {
        return distance;
    }

    public DistanceEntry repSetDistance(Distance distance)
    {
        return new DistanceEntry(member, racepoint, distance);
    }
}
