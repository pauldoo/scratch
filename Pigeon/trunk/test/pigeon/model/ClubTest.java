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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;


/**
 *
 * @author Paul
 */
public final class ClubTest extends TestCase {

    public ClubTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ClubTest.class);

        return suite;
    }

    public void testSerialization() throws IOException, ClassNotFoundException, ValidationException {
        Organization club = Organization.createEmpty();
        {
            List<Member> members = new ArrayList<Member>();
            List<Racepoint> racepoints = new ArrayList<Racepoint>();

            for (int i = 0; i <= 9; i++) {
                Member m = Member.createEmpty();
                m = m.repSetName("member_" + i);
                members.add( m );
                club = club.repAddMember(m);

                Racepoint r = Racepoint.createEmpty();
                r = r.repSetName("racepoint_" + i);
                racepoints.add( r );
                club = club.repAddRacepoint(r);
            }
        }

        verifyReferences(club);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject( club );
        club = null;
        objOut.close();

        // Organization written out

        InputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInput objIn = new ObjectInputStream(byteIn);
        club = (Organization)objIn.readObject();

        verifyReferences(club);
    }

    /**
     * Verifies that the object instances used as keys within the distances
     * structure are the same instances that are present in the member/racepoint
     * lists.
     */
    private void verifyReferences(Organization club) {
        for (Member member: club.getMembers()) {
            for (Racepoint racepoint: club.getRacepoints()) {
                DistanceEntry e = club.getDistanceEntry(member, racepoint);
                assertTrue("Member instance in member list is same instance as that in distance list", e.getMember() == member);
                assertTrue("Racepoint instance in racepoint list is same instance as that in distance list", e.getRacepoint() == racepoint);
            }
        }
    }

    public void testClashes() throws ValidationException {
        Organization club = Organization.createEmpty();
        {
            Member m = Member.createEmpty();
            m = m.repSetName("foo");
            club = club.repAddMember(m);
        }
        {
            Member m = Member.createEmpty();
            m = m.repSetName("bar");
            club = club.repAddMember(m);
        }
        {
            Member m = Member.createEmpty();
            m = m.repSetName("foo");
            try {
                club.repAddMember(m);
                assertTrue("Expected exception", false);
            } catch (ValidationException ex) {
                assertEquals("Exception contents as expected", "Member already exists", ex.toString());
            }
        }
    }

}
