/*
 * Pigeon: A pigeon club race result management program.
 * Copyright (C) 2005-2006  Paul Richards
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package pigeon.model;

import java.io.Serializable;

/**
 *
 * @author Paul
 */
public class Racepoint implements Serializable, Comparable<Racepoint> {
    
    private static final long serialVersionUID = 42L;
    
    private String name;
    
    /** Creates a new instance of Racepoint */
    public Racepoint() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws ValidationException {
        name = name.trim();
        if (name.length() == 0) {
            throw new ValidationException("Racepoint name is empty");
        }
        this.name = name;
    }
    
    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }
    
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
    
    public int compareTo(Racepoint other) {
        if (this == other) {
            return 0;
        } else {
            return name.compareTo(other.name);
        }
    }
    
}