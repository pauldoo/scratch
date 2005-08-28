/*
 * Member.java
 *
 * Created on 21 August 2005, 15:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package pigeon.model;

import java.io.Serializable;

/**
 *
 * @author Paul
 */
public class Member implements Serializable, Comparable<Member> {
    
    private static final long serialVersionUID = 42L;
    
    private String name;
    
    /** Creates a new instance of Member */
    public Member() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public int compareTo(Member other) {
        if (this == other) {
            return 0;
        } else {
            return name.compareTo(other.name);
        }
    }
    
}