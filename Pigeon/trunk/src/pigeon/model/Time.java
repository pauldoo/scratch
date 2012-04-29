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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
    Represents the clocking time for a single ring number.

    TODO: Rename, to maybe RingTime, PigeonEntry, ...
*/
public final class Time implements Comparable<Time>, Serializable
{
    private static final long serialVersionUID = 5746826265321182248L;

    private final String ringNumber;
    private final String color;
    private final Sex sex;
    private final int time;

    /// In CLUB mode, this list stores the club competitions.
    private final Set<String> openCompetitionsEntered;
    /// Only populated in FEDERATION mode, empty in CLUB mode.
    private final Set<String> sectionCompetitionsEntered;

    private Time(String ringNumber, String color, Sex sex, int time, Set<String> openCompetitionsEntered, Set<String> sectionCompetitionsEntered) {
        this.ringNumber = ringNumber.trim();
        this.color = color.trim();
        this.sex = sex;
        this.time = time;
        this.openCompetitionsEntered = Utilities.unmodifiableSetCopy(openCompetitionsEntered);
        this.sectionCompetitionsEntered = Utilities.unmodifiableSetCopy(sectionCompetitionsEntered);
    }

    public static Time createEmpty()
    {
        return new Time("", "", Sex.COCK, 0, Utilities.createEmtpySet(String.class), Utilities.createEmtpySet(String.class));
    }

    public String getRingNumber()
    {
        return ringNumber;
    }

    public Time repSetRingNumber(String ringNumber)
    {
        return new Time(ringNumber, color, sex, time, openCompetitionsEntered, sectionCompetitionsEntered);
    }

    public long getMemberTime()
    {
        return time;
    }

    /**
        Member time is measured in ms since the midnight before liberation.
     */
    public Time repSetMemberTime(long time, int daysInRace) throws ValidationException
    {
        if (time < 0 || time >= daysInRace * Constants.MILLISECONDS_PER_DAY) {
            throw new ValidationException("Time is outwith the length of the race (" + daysInRace + " days)");
        }
        return new Time(ringNumber, color, sex, (int)time, openCompetitionsEntered, sectionCompetitionsEntered);
    }

    @Override
    public boolean equals(Object other)
    {
        return equals((Time)other);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Time other)
    {
        return this.getRingNumber().equals(other.getRingNumber());
    }

    @Override
    public int compareTo(Time other)
    {
        return this.getRingNumber().compareTo(other.getRingNumber());
    }

    public Collection<String> getOpenCompetitionsEntered()
    {
        return Collections.unmodifiableCollection(openCompetitionsEntered);
    }

    public Time repSetOpenCompetitionsEntered(Set<String> competitions)
    {
        return new Time(ringNumber, color, sex, time, competitions, sectionCompetitionsEntered);
    }

    public Collection<String> getSectionCompetitionsEntered()
    {
        return Collections.unmodifiableCollection(sectionCompetitionsEntered);
    }

    public Time repSetSectionCompetitionsEntered(Set<String> competitions)
    {
        return new Time(ringNumber, color, sex, time, openCompetitionsEntered, competitions);
    }

    public String getColor()
    {
        String result = color;
        if (result == null) {
            result = "";
        }
        return result;
    }

    public Time repSetColor(String color)
    {
        return new Time(ringNumber, color, sex, time, openCompetitionsEntered, sectionCompetitionsEntered);
    }

    public Sex getSex()
    {
        Sex result = sex;
        if (result == null) {
            result = Sex.COCK;
        }
        return sex;
    }

    public Time repSetSex(Sex sex)
    {
        return new Time(ringNumber, color, sex, time, openCompetitionsEntered, sectionCompetitionsEntered);
    }
}
