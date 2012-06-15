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
    Stores a single distance (stored in metres) and provides accessors
    to return the distance in imperial units.
*/
public final class Distance implements Serializable, Comparable<Distance> {

    private static final long serialVersionUID = 7289132359169706543L;

    private final double distanceInMetres;

    private Distance(double metres) {
        this.distanceInMetres = metres;
    }

    public static Distance createFromMetric(double metres) {
        return new Distance(metres);
    }

    public static Distance createFromImperial(int miles, int yards) {
        return createFromMetric((miles * Constants.YARDS_PER_MILE + yards) * Constants.METRES_PER_YARD);
    }

    // Return distance in metres
    public double getMetres() {
        return distanceInMetres;
    }

    // Return distance in yards
    public double getYards() {
        return distanceInMetres / Constants.METRES_PER_YARD;
    }

    public int getMiles() {
        return (int)Math.round(getYards()) / Constants.YARDS_PER_MILE;
    }

    public int getYardsRemainder() {
        return (int)Math.round(getYards()) % Constants.YARDS_PER_MILE;
    }

    @Override
    public String toString() {
        int miles = getMiles();
        int yards = getYardsRemainder();
        return miles + " miles " + yards + " yards";
    }

    @Override
    public int hashCode() {
        return new Double(distanceInMetres).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Distance other) {
        return Double.compare(this.distanceInMetres, other.distanceInMetres);
    }
}
