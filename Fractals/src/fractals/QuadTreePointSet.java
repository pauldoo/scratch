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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
    Implementation of the PointSet interface using PR Quadtrees.
*/
final class QuadTreePointSet implements PointSet
{
    private final Node rootNode;

    public QuadTreePointSet()
    {
        this.rootNode = new EmptyNode(new Point2D.Double(0.0, 0.0), new Point2D.Double(1.0, 1.0));
    }

    private QuadTreePointSet(Node rootNode)
    {
        this.rootNode = rootNode;
    }

    public PointSet add(Point2D.Double point)
    {
        return new QuadTreePointSet(rootNode.add((Point2D.Double)point.clone()));
    }

    public PointSet remove(Point2D.Double point)
    {
        return new QuadTreePointSet(rootNode.remove(point));
    }

    public Point2D.Double findClosest(Point2D.Double point)
    {
        Point2D.Double result = rootNode.findClosest(point);
        if (result != null) {
            result = (Point2D.Double)result.clone();
        }
        return result;
    }

    public Iterator<Point2D.Double> iterator()
    {
        Collection<Point2D.Double> collection = new ArrayList<Point2D.Double>();
        rootNode.collectPoints(collection);
        return collection.iterator();
    }

    public int size()
    {
        return rootNode.numberOfPoints();
    }

    /**
        Root class for internal implementation classes.
    */
    private static abstract class Node
    {
        /**
            Most negative corner of the bounds of this node, inclusive.
        */
        protected final Point2D.Double min;

        /**
            Most positive corner of the bounds of this node, exclusive.
        */
        protected final Point2D.Double max;

        Node(Point2D.Double min, Point2D.Double max)
        {
            this.min = min;
            this.max = max;
            if (((max.x > min.x) &&
                (((max.x + min.x) / 2) != min.x) &&
                (((max.x + min.x) / 2) != max.x) &&
                (max.y > min.y) &&
                (((max.y + min.y) / 2) != min.y) &&
                (((max.y + min.y) / 2) != max.y)) == false) {
                throw new RuntimeException();
            }
        }

        /**
            Adds a point to the quadtree, possibly extending
            the quadtree in the process.
        */
        final Node add(Point2D.Double point)
        {
            if (isPointWithinBounds(point)) {
                return addInside(point);
            } else {
                final double dx = point.x - getCenter().x;
                final double dy = point.y - getCenter().y;
                final double width = max.x - min.x;
                final double height = max.y - min.y;
                Point2D.Double newMin = new Point2D.Double(
                        (dx < 0) ? (min.x - width) : (min.x),
                        (dy < 0) ? (min.y - height) : (min.y));
                Point2D.Double newMax = new Point2D.Double(
                        (dx < 0) ? (max.x) : (max.x + width),
                        (dy < 0) ? (max.y) : (max.y + height));

                Quadrant expandDirection = Utilities.quadrant(getCenter(), point);
                return (new InnerNode(newMin, newMax)).repReplaceSubNode(Utilities.diagonallyOpposite(expandDirection), this).add(point);
            }
        }

        /**
            Adds a point to the quadtree, never extending
            the quadtree in the process.
        */
        abstract Node addInside(Point2D.Double point);

        abstract Node remove(Point2D.Double point);

        abstract Point2D.Double findClosest(Point2D.Double point);

        /**
            Returns true iff the given point is within the bounds of the node.
        */
        protected boolean isPointWithinBounds(Point2D.Double point)
        {
            return
                    point.x >= min.x && point.x < max.x &&
                    point.y >= min.y && point.y < max.y;
        }

        /**
            Returns the center point of the node (also the top left corner of the 4th quadrant).
        */
        protected Point2D.Double getCenter()
        {
            return new Point2D.Double((min.x + max.x) / 2, (min.y + max.y) / 2);
        }

        /**
            Returns the lower bound distance of this node to a given point.
        */
        protected double distanceToPoint(Point2D.Double point)
        {
            Point2D.Double clampedPoint = new Point2D.Double(
                    Math.max(min.x, Math.min(max.x, point.x)),
                    Math.max(min.y, Math.min(max.y, point.y)));
            return clampedPoint.distance(point);
        }

        abstract void collectPoints(Collection<Point2D.Double> collection);

        abstract int numberOfPoints();
    }

    /**
        A node in the tree that is empty and contains no points or further
        child nodes.
    */
    private static final class EmptyNode extends Node
    {
        EmptyNode(Point2D.Double min, Point2D.Double max)
        {
            super(min, max);
        }

        @Override
        Node addInside(Point2D.Double point)
        {
            return new LeafNode(min, max, point);
        }

        @Override
        Node remove(Point2D.Double point)
        {
            throw new IllegalArgumentException("Point does not exist");
        }

        @Override
        Point2D.Double findClosest(Point2D.Double point)
        {
            return null;
        }

        @Override
        void collectPoints(Collection<Point2D.Double> collection)
        {
        }

        @Override
        int numberOfPoints()
        {
            return 0;
        }
    }

    /**
        A node in the tree that itself contains no points but has 4 child nodes.
    */
    private static final class InnerNode extends Node
    {
        private final Node subNodeA;
        private final Node subNodeB;
        private final Node subNodeC;
        private final Node subNodeD;

        InnerNode(Point2D.Double min, Point2D.Double max)
        {
            super(min, max);

            subNodeA = new EmptyNode(
                    min,
                    getCenter());
            subNodeB = new EmptyNode(
                    new Point2D.Double(getCenter().x, min.y),
                    new Point2D.Double(max.x, getCenter().y));
            subNodeC = new EmptyNode(
                    new Point2D.Double(min.x, getCenter().y),
                    new Point2D.Double(getCenter().x, max.y));
            subNodeD = new EmptyNode(
                    getCenter(),
                    max);
        }

        private InnerNode(
                Point2D.Double min,
                Point2D.Double max,
                Node subNodeA,
                Node subNodeB,
                Node subNodeC,
                Node subNodeD)
        {
            super(min, max);

            this.subNodeA = subNodeA;
            this.subNodeB = subNodeB;
            this.subNodeC = subNodeC;
            this.subNodeD = subNodeD;
        }

        @Override
        Node addInside(Point2D.Double point)
        {
            Quadrant q = Utilities.quadrant(getCenter(), point);
            return repReplaceSubNode(q, getSubNode(q).addInside(point));
        }

        @Override
        Node remove(Point2D.Double point)
        {
            Quadrant q = Utilities.quadrant(getCenter(), point);
            return repReplaceSubNode(q, getSubNode(q).remove(point)).normalizeForEmpty();
        }

        private final Node normalizeForEmpty()
        {
            if (this.subNodeA instanceof EmptyNode &&
                this.subNodeB instanceof EmptyNode &&
                this.subNodeC instanceof EmptyNode &&
                this.subNodeD instanceof EmptyNode) {
                return new EmptyNode(min, max);
            } else {
                return this;
            }
        }

        private Node getSubNode(Quadrant quadrant)
        {
            switch (quadrant) {
                case A:
                    return subNodeA;
                case B:
                    return subNodeB;
                case C:
                    return subNodeC;
                case D:
                    return subNodeD;
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        Point2D.Double findClosest(Point2D.Double point)
        {
            final double dx = Math.abs(getCenter().x - point.x);
            final double dy = Math.abs(getCenter().y - point.y);
            final Quadrant[] quadrantList = new Quadrant[4];
            quadrantList[0] = Utilities.quadrant(getCenter(), point);
            quadrantList[1] = (dx <= dy) ? Utilities.neighbourAlongX(quadrantList[0]) : Utilities.neighbourAlongY(quadrantList[0]);
            quadrantList[2] = (dx <= dy) ? Utilities.neighbourAlongY(quadrantList[0]) : Utilities.neighbourAlongX(quadrantList[0]);
            quadrantList[3] = Utilities.diagonallyOpposite(quadrantList[0]);

            Point2D.Double result = null;
            for (Quadrant q: quadrantList) {
                final Node subNode = getSubNode(q);
                if (result == null || subNode.distanceToPoint(point) <= result.distance(point)) {
                    Point2D.Double newCandidate = subNode.findClosest(point);
                    if (newCandidate != null && (result == null || newCandidate.distance(point) < result.distance(point))) {
                        result = newCandidate;
                    }
                } else {
                    break;
                }
            }
            return result;
        }

        InnerNode repReplaceSubNode(Quadrant q, Node replacementSubNode)
        {
            switch (q) {
                case A:
                    return new InnerNode(min, max, replacementSubNode, subNodeB, subNodeC, subNodeD);
                case B:
                    return new InnerNode(min, max, subNodeA, replacementSubNode, subNodeC, subNodeD);
                case C:
                    return new InnerNode(min, max, subNodeA, subNodeB, replacementSubNode, subNodeD);
                case D:
                    return new InnerNode(min, max, subNodeA, subNodeB, subNodeC, replacementSubNode);
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        void collectPoints(Collection<Point2D.Double> collection)
        {
            subNodeA.collectPoints(collection);
            subNodeB.collectPoints(collection);
            subNodeC.collectPoints(collection);
            subNodeD.collectPoints(collection);
        }

        @Override
        int numberOfPoints()
        {
            return
                subNodeA.numberOfPoints() +
                subNodeB.numberOfPoints() +
                subNodeC.numberOfPoints() +
                subNodeD.numberOfPoints();
        }
    }

    /**
        A node in the tree that contains one point and no other children nodes.
    */
    private static final class LeafNode extends Node
    {
        private final Point2D.Double point;

        LeafNode(Point2D.Double min, Point2D.Double max, Point2D.Double point)
        {
            super(min, max);
            this.point = point;
            if (isPointWithinBounds(point) == false) {
                throw new RuntimeException("Point is outside of leaf bounds");
            }
        }

        @Override
        Node addInside(Point2D.Double point)
        {
            return (new InnerNode(min, max)).addInside(this.point).addInside(point);
        }

        @Override
        Node remove(Point2D.Double point)
        {
            if (this.point.equals(point)) {
                return new EmptyNode(min, max);
            } else {
                throw new IllegalArgumentException("Point does not exist");
            }
        }

        @Override
        Point2D.Double findClosest(Point2D.Double point)
        {
            return this.point;
        }

        @Override
        void collectPoints(Collection<Point2D.Double> collection)
        {
            collection.add((Point2D.Double)point.clone());
        }

        @Override
        int numberOfPoints()
        {
            return 1;
        }
    }

    private static enum Quadrant {
        A, B, C, D;
    }

    private static final class Utilities
    {
        private Utilities()
        {
        }

        static Quadrant quadrant(Point2D.Double origin, Point2D.Double point)
        {
            final int quadrantIndex =
                    ((point.getX() >= origin.getX()) ? 1 : 0) +
                    ((point.getY() >= origin.getY()) ? 2 : 0);
            return Quadrant.values()[quadrantIndex];
        }

        static Quadrant diagonallyOpposite(Quadrant q)
        {
            switch (q) {
                case A:
                    return Quadrant.D;
                case B:
                    return Quadrant.C;
                case C:
                    return Quadrant.B;
                case D:
                    return Quadrant.A;
                default:
                    throw new RuntimeException();
            }
        }

        static Quadrant neighbourAlongX(Quadrant q)
        {
            switch (q) {
                case A:
                    return Quadrant.B;
                case B:
                    return Quadrant.A;
                case C:
                    return Quadrant.D;
                case D:
                    return Quadrant.C;
                default:
                    throw new RuntimeException();
            }
        }

        static Quadrant neighbourAlongY(Quadrant q)
        {
            switch (q) {
                case A:
                    return Quadrant.C;
                case B:
                    return Quadrant.D;
                case C:
                    return Quadrant.A;
                case D:
                    return Quadrant.B;
                default:
                    throw new RuntimeException();
            }
        }
    }
}
