/*
    Copyright (c) 2008, 2012 Paul Richards <paul.richards@gmail.com>

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

package fractals;

import java.awt.geom.Point2D;
import java.util.Random;
import junit.framework.TestCase;

public final class QuadTreePointSetTest extends TestCase
{
    public void testCreation()
    {
        PointSet p = new QuadTreePointSet();
    }

    public void testSimpleAddition()
    {
        {
            PointSet p = new QuadTreePointSet().add(new Point2D.Double(0.0, 0.0));
            assertNotNull(p);
            assertEquals(new Point2D.Double(0.0, 0.0), p.findClosest(new Point2D.Double(0.0, 0.0)));
            assertEquals(new Point2D.Double(0.0, 0.0), p.findClosest(new Point2D.Double(1.0, 0.0)));
        }

        {
            PointSet p = new QuadTreePointSet().add(new Point2D.Double(0.9, 0.0));
            assertNotNull(p);
            assertEquals(new Point2D.Double(0.9, 0.0), p.findClosest(new Point2D.Double(0.0, 0.0)));
            assertEquals(new Point2D.Double(0.9, 0.0), p.findClosest(new Point2D.Double(1.0, 0.0)));
        }
    }

    public void testRandomPoints()
    {
        final int POINT_COUNT = 10000;
        PointSet a = new SimplePointSet();
        PointSet b = new QuadTreePointSet();
        Random rng = new Random(333);
        long timeAccumulationA = 0;
        long timeAccumulationB = 0;
        for (int i = 0; i < POINT_COUNT; i++) {
            Point2D.Double randomPoint = new Point2D.Double(
                    (rng.nextDouble() - 0.5) * Math.sqrt(i),
                    (rng.nextDouble() - 0.5) * Math.sqrt(i));
            a = a.add(randomPoint);
            b = b.add(randomPoint);
            Point2D.Double anotherRandomPoint = new Point2D.Double(
                    (rng.nextDouble() - 0.5) * Math.sqrt(i),
                    (rng.nextDouble() - 0.5) * Math.sqrt(i));
            final long timeA = System.currentTimeMillis();
            Point2D.Double resultA = a.findClosest(anotherRandomPoint);
            final long timeB = System.currentTimeMillis();
            Point2D.Double resultB = b.findClosest(anotherRandomPoint);
            final long timeC = System.currentTimeMillis();
            timeAccumulationA += (timeB - timeA);
            timeAccumulationB += (timeC - timeB);
            assertEquals(resultA, resultB);
        }
        System.out.println("Simple: " + timeAccumulationA + "ms");
        System.out.println("QuadTree: " + timeAccumulationB + "ms");
    }
}
