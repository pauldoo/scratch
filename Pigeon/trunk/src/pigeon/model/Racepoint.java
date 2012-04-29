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

import java.io.Serializable;

/**
    Represents a racepoint.
*/
public final class Racepoint implements Serializable, Comparable<Racepoint> {

    private static final long serialVersionUID = 5881572526657587494L;

    private final String name;

    private Racepoint(String name) {
        this.name = name;
    }

    public static Racepoint createEmpty()
    {
        return new Racepoint("");
    }

    public String getName() {
        return name;
    }

    public Racepoint repSetName(String name) throws ValidationException {
        name = name.trim();
        if (name.length() == 0) {
            throw new ValidationException("Racepoint name is empty");
        }
        return new Racepoint(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return equals((Racepoint)other);
    }

    public boolean equals(Racepoint other) {
        if (this == other) {
            return true;
        } else {
            return name.equals(other.name);
        }
    }

    @Override
    public int compareTo(Racepoint other) {
        if (this == other) {
            return 0;
        } else {
            return name.compareTo(other.name);
        }
    }
}
