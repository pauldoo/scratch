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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
    Stores top level information about a racing organization (be that a federation or club).

    Maintains a list of members, racepoints, and distance entries.

    This final class was once called "Club", and sadly many places in the codebase still refer to
    the old name.
*/
public final class Organization implements Serializable
{
    private static final long serialVersionUID = 5358293332608930714L;

    private final String name;
    private final List<Member> members;
    private final List<Racepoint> racepoints;
    private final List<DistanceEntry> distances;

    private Organization(
            String name,
            List<Member> members,
            List<Racepoint> racepoints,
            List<DistanceEntry> distances) {
        this.name = name;
        this.members = Utilities.unmodifiableSortedListCopy(members);
        this.racepoints = Utilities.unmodifiableSortedListCopy(racepoints);
        this.distances = Utilities.unmodifiableSortedListCopy(distances);
    }

    public static Organization createEmpty()
    {
        return new Organization(
                "",
                Utilities.createEmptyList(Member.class),
                Utilities.createEmptyList(Racepoint.class),
                Utilities.createEmptyList(DistanceEntry.class));
    }

    public String getName() {
        return name;
    }

    public Organization repSetName(String name) throws ValidationException {
        name = name.trim();
        if (name.length() == 0) {
            throw new ValidationException("Organisation name is empty");
        }
        return new Organization(name, members, racepoints, distances);
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<Racepoint> getRacepoints() {
        return racepoints;
    }

    public Organization repAddMember(Member member) throws ValidationException {
        List<Member> newMembers = Utilities.replicateListAdd(this.members, member);

        List<DistanceEntry> newDistances = Utilities.modifiableListCopy(distances);
        for (Racepoint racepoint: racepoints) {
            DistanceEntry entry = new DistanceEntry(member, racepoint, Distance.createFromMetric(0));
            if (newDistances.contains(entry) || !newDistances.add(entry)) {
                throw new IllegalStateException("Failed to add distance entries");
            }
        }

        return new Organization(name, newMembers, racepoints, newDistances);
    }

    public Organization repAddRacepoint(Racepoint racepoint) throws ValidationException {
        List<Racepoint> newRacepoints = Utilities.replicateListAdd(this.racepoints, racepoint);

        List<DistanceEntry> newDistances = Utilities.modifiableListCopy(distances);
        for (Member member: members) {
            DistanceEntry entry = new DistanceEntry(member, racepoint, Distance.createFromMetric(0));
            if (newDistances.contains(entry) || !newDistances.add(entry)) {
                throw new IllegalStateException("Failed to add distance entries");
            }
        }

        return new Organization(name, members, newRacepoints, newDistances);
    }

    public Organization repRemoveMember(Member member) {
        List<Member> newMembers = Utilities.replicateListRemove(this.members, member);

        List<DistanceEntry> newDistances = Utilities.modifiableListCopy(distances);
        for (Racepoint racepoint: racepoints) {
            DistanceEntry entry = new DistanceEntry(member, racepoint, Distance.createFromMetric(0));
            if (!newDistances.contains( entry ) || !newDistances.remove(entry)) {
                throw new IllegalStateException("Failed to remove distance entries");
            }
        }

        return new Organization(name, newMembers, racepoints, newDistances);
    }

    public Organization repRemoveRacepoint(Racepoint racepoint) {
        List<Racepoint> newRacepoints = Utilities.replicateListRemove(this.racepoints, racepoint);

        List<DistanceEntry> newDistances = Utilities.modifiableListCopy(distances);
        for (Member member: members) {
            DistanceEntry entry = new DistanceEntry(member, racepoint, Distance.createFromMetric(0));
            if (!newDistances.contains( entry ) || !newDistances.remove(entry)) {
                throw new IllegalStateException("Failed to remove distance entries");
            }
        }

        return new Organization(name, members, newRacepoints, newDistances);
    }

    public int getNumberOfMembers() {
        return members.size();
    }

    public int getNumberOfRacepoints() {
        return racepoints.size();
    }

    public DistanceEntry getDistanceEntry(Member member, Racepoint racepoint) {
        DistanceEntry entry = new DistanceEntry(member, racepoint, null);
        for (DistanceEntry stored: distances) {
            if (entry.equals(stored)) {
                return stored;
            }
        }
        throw new IllegalArgumentException("Member / Racepoint doesn't exist");
    }

    public Distance getDistance(Member member, Racepoint racepoint) {
        return getDistanceEntry(member, racepoint).getDistance();
    }

    public Organization repSetDistance(Member member, Racepoint racepoint, Distance distance)
    {
        DistanceEntry currentEntry = getDistanceEntry(member, racepoint);
        DistanceEntry newEntry = currentEntry.repSetDistance(distance);
        return new Organization(name, members, racepoints,
                Utilities.replicateListReplace(distances, currentEntry, newEntry));
    }

    public Map<Racepoint, Distance> getDistancesForMember(Member member) {
        Map<Racepoint, Distance> retval = new TreeMap<Racepoint, Distance>();
        for (Racepoint racepoint: racepoints) {
            retval.put(racepoint, getDistance(member, racepoint));
        }
        return retval;
    }

    public Map<Member, Distance> getDistancesForRacepoint(Racepoint racepoint) {
        Map<Member, Distance> retval = new TreeMap<Member, Distance>();
        for (Member member: members) {
            retval.put(member, getDistance(member, racepoint));
        }
        return retval;
    }

    public Organization repReplaceRacepoint(Racepoint oldRacepoint, Racepoint newRacepoint) throws ValidationException
    {
        Organization result = this.repRemoveRacepoint(oldRacepoint).repAddRacepoint(newRacepoint);
        for(Map.Entry<Member, Distance> e: getDistancesForRacepoint(oldRacepoint).entrySet()) {
            result = result.repSetDistance(e.getKey(), newRacepoint, e.getValue());
        }
        return result;
    }

    public Organization repReplaceMember(Member oldMember, Member newMember) throws ValidationException
    {
        Organization result = this.repRemoveMember(oldMember).repAddMember(newMember);
        for(Map.Entry<Racepoint, Distance> e: getDistancesForMember(oldMember).entrySet()) {
            result = result.repSetDistance(newMember, e.getKey(), e.getValue());
        }
        return result;
    }
}
