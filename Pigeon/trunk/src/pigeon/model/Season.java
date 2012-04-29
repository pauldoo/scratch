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
import java.util.List;

/**
    Stores information for a single season.

    Contains an Orgainzation object and a collecion of Race objects.
    Organization objects should not be shared between seasons since the
    set of members and racepoints might change from season to season.
*/
public final class Season implements Serializable {

    private static final long serialVersionUID = 2185370002566545845L;

    private final String name;
    private final Organization organization;
    private final List<Race> races;

    private Season(String name, Organization organization, List<Race> races) {
        this.name = name;
        this.organization = organization;
        this.races = Utilities.unmodifiableSortedListCopy(races);
    }

    public static Season createEmpty() {
        return new Season("", Organization.createEmpty(), Utilities.createEmptyList(Race.class));
    }

    public Organization getOrganization() {
        return organization;
    }

    public Season repSetOrganization(Organization organization) {
        return new Season(
                this.name,
                organization,
                this.races);
    }

    public String getName() {
        return name;
    }

    public Season repSetName(String name) {
        return new Season(
                name,
                this.organization,
                this.races);
    }

    public Season repAddRace(Race race) throws ValidationException {
        return new Season(
                this.name,
                this.organization,
                Utilities.replicateListAdd(this.races, race));
    }

    public Season repRemoveRace(Race race) throws ValidationException {
        return new Season(
                this.name,
                this.organization,
                Utilities.replicateListRemove(this.races, race));
    }

    public Season repReplaceRace(Race oldRace, Race newRace) throws ValidationException {
        return repRemoveRace(oldRace).repAddRace(newRace);
    }

    public List<Race> getRaces() {
        return this.races;
    }

    public Season repReplaceMember(Member oldMember, Member newMember) throws ValidationException
    {
        List<Race> newRaces = Utilities.createEmptyList(Race.class);
        for (Race r: races) {
            newRaces.add(r.repReplaceMember(oldMember, newMember));
        }
        return new Season(name, organization.repReplaceMember(oldMember, newMember), newRaces);
    }

    public Season repReplaceRacepoint(Racepoint oldRacepoint, Racepoint newRacepoint) throws ValidationException
    {
        List<Race> newRaces = Utilities.createEmptyList(Race.class);
        for (Race r: races) {
            if (r.getRacepoint().equals(oldRacepoint)) {
                newRaces.add(r.repSetRacepoint(newRacepoint));
            } else {
                newRaces.add(r);
            }
        }
        return new Season(name, organization.repReplaceRacepoint(oldRacepoint, newRacepoint), newRaces);
    }
}
