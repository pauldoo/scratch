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
import java.util.HashSet;
import java.util.Iterator;

/**
    Ideally this class would be implemented using a PR Quadtree but for now
    it's a simple Hash.
*/
final class SimplePointSet implements PointSet
{
    private final HashSet<Point2D.Double> set;

    SimplePointSet()
    {
        this.set = new HashSet<Point2D.Double>();
    }

    public PointSet add(Point2D.Double point)
    {
        set.add((Point2D.Double)point.clone());
        return this;
    }

    public Point2D.Double findClosest(Point2D.Double point)
    {
        Point2D.Double result = null;
        for (Point2D.Double p: set) {
            if (result == null || point.distanceSq(p) < point.distanceSq(result)) {
                result = p;
            }
        }
        return (Point2D.Double)result.clone();
    }

    public Iterator<Point2D.Double> iterator()
    {
        return set.iterator();
    }

    public PointSet remove(Point2D.Double point)
    {
        if (set.remove(point) == false) {
            throw new IllegalArgumentException("Point did not exist in set");
        }
        return this;
    }

    public int size()
    {
        return set.size();
    }
}
