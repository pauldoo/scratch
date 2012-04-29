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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
    Utility functions for manipulating time.
*/
public final class Utilities
{
    // Non-Creatable
    private Utilities()
    {
    }

    /**
        Given a long value representing a time, returns the beginning of that day.

        Does not take into account timezones or locale information, so should only
        be used for times that are relative and span only a few days.
    */
    static long truncateToMidnight(long time)
    {
        if (time >= Constants.MILLISECONDS_PER_DAY * 10) {
            throw new IllegalArgumentException("Expecting a small 'relative' date");
        }
        return (time / Constants.MILLISECONDS_PER_DAY) * Constants.MILLISECONDS_PER_DAY;
    }


    /**
        Given a date returns the beggining of the day (using local timezone).
    */
    public static Date beginningOfCalendarDay(Date date)
    {
        if (date.getTime() <= Constants.MILLISECONDS_PER_DAY * 10) {
            throw new IllegalArgumentException("Expecting an 'absolute' date");
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal = new GregorianCalendar(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    /**
        Given a rate retuns the fraction of the day elapsed (using local timezone).
    */
    public static Date fractionOfCalendarDay(Date date)
    {
        if (date.getTime() <= Constants.MILLISECONDS_PER_DAY * 10) {
            throw new IllegalArgumentException("Expecting an 'absolute' date");
        }
        return new Date(date.getTime() - beginningOfCalendarDay(date).getTime());
    }

    static long roundToNearestSecond(long time) {
        return ((time + ((time >= 0) ? 500 : -500)) / 1000) * 1000;
    }

    static<T> List<T> unmodifiableListCopy(List<T> items)
    {
        List<T> result = new ArrayList<T>(items);
        return Collections.unmodifiableList(result);
    }

    /**
        Creates an unmodifiable sorted copy of the given list.

        This is only a shallow copy, in that only the list container is copied
        and not all elements within the container.
     */
    static <T extends Comparable<? super T>>
    List<T> unmodifiableSortedListCopy(List<T> items)
    {
        List<T> result = new ArrayList<T>(items);
        Collections.sort(result);
        return unmodifiableListCopy(result);
    }

    /**
        Creates a modifiable copy of the given list.

        This is only a shallow copy, in that only the list container is copied
        and not all elements within the container.
    */
    public static <T> List<T> modifiableListCopy(List<T> items)
    {
        return new ArrayList<T>(items);
    }

    /**
        Replicates a list and appends the given item to the resulting
        copy.  Throws IllegalArgumentException if an item of the same
        value already exists in the list.
    */
    static<T> List<T> replicateListAdd(List<T> items, T itemToAdd) throws ValidationException
    {
        List<T> result = new ArrayList<T>(items);
        if (result.contains(itemToAdd) || !result.add(itemToAdd)) {
            throw new ValidationException(itemToAdd.getClass().getSimpleName() + " already exists");
        }
        return result;
    }

    /**
        Replicates a list and removes the given item from the resulting
        copy.  Throws IllegalArgumentException if the item did not exist
        in the list and coult not be removed.
    */
    static <T> List<T> replicateListRemove(List<T> items, T itemToRemove) throws IllegalArgumentException
    {
        List<T> result = modifiableListCopy(items);
        if (result.remove(itemToRemove) == false) {
            throw new IllegalArgumentException(itemToRemove.getClass().getSimpleName() + " does not exist");
        }
        return result;
    }

    static <T> List<T> createEmptyList(Class<T> type)
    {
        return new ArrayList<T>();
    }

    static <T, U> Map<T, U> createEmptyMap(Class<T> typeT, Class<U> typeU)
    {
        return new TreeMap<T, U>();
    }

    static <T> Set<T> createEmtpySet(Class<T> type)
    {
        return new TreeSet<T>();
    }

    /**
        Returns a copy of the given list with "itemToRemove" removed and
        "itemToAdd" added.  Throws IllegalArgumentException if "itemToRemove"
        could not be found or if (itemToRemove == itemToAdd || itemToRemove.equals(itemToAdd) == false).
    */
    static<T> List<T> replicateListReplace(List<T> items, T itemToRemove, T itemToAdd) throws IllegalArgumentException
    {
        if (itemToRemove == itemToAdd) {
            throw new IllegalArgumentException("Cannot replace item with itself.");
        }
        if (itemToRemove.equals(itemToAdd) == false) {
            throw new IllegalArgumentException("Replacement item is expected to \"equal\" the previous.");
        }

        try {
            return replicateListAdd(replicateListRemove(items, itemToRemove), itemToAdd);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    static<T, U> Map<T, List<U> > unmodifiableMapListCopy(Map<T, List<U> > map)
    {
        Map<T, List<U> > result = new TreeMap<T, List<U> >();
        for (Map.Entry<T, List<U>> e: map.entrySet()) {
            result.put(e.getKey(), unmodifiableListCopy(e.getValue()));
        }
        return unmodifiableMapCopy(result);
    }

    static<T, U, V> Map<T, Map<U, V> > unmodifiableMapMapCopy(Map<T, Map<U, V> > map)
    {
        Map<T, Map<U, V> > result = new TreeMap<T, Map<U, V> >();
        for (Map.Entry<T, Map<U, V>> e: map.entrySet()) {
            result.put(e.getKey(), unmodifiableMapCopy(e.getValue()));
        }
        return unmodifiableMapCopy(result);
    }

    static<T, U> Map<T, U> unmodifiableMapCopy(Map<T, U> map)
    {
        Map<T, U> result = new TreeMap<T, U>(map);
        return Collections.unmodifiableMap(result);
    }

    static <T> Set<T> unmodifiableSetCopy(Set<T> set)
    {
        return Collections.unmodifiableSet(new TreeSet<T>(set));
    }
}
