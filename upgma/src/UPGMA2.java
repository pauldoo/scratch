import static java.lang.System.identityHashCode;
import static java.util.Collections.unmodifiableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class UPGMA2 {

    public static <T> Set<Set<T>> cluster(final Collection<T> values, final double distanceThreshold,
            final DistanceFunction<T> distanceFn) {

        return clusterToThreshold(clusterToTree(values, distanceFn), distanceThreshold);
    }

    public static <T> Set<Set<T>> clusterToThreshold(final Cluster<T> clustering, final double distanceThreshold) {
        final Set<Set<T>> result = new HashSet<Set<T>>();
        clustering.preOrderVisit(new ClusterVisitor<T>() {
            int leavesToExpect = 1;
            Set<T> currentSet = new HashSet<T>();

            @Override
            public void visitLeaf(final Leaf<T> leaf) {
                currentSet.add(leaf.value);
                leavesToExpect--;
                if (leavesToExpect == 0) {
                    result.add(unmodifiableSet(currentSet));
                    currentSet = new HashSet<T>();
                    leavesToExpect = 1;
                }
            }

            @Override
            public void visitPair(final Pair<T> pair) {
                if (pair.averageLinkDistanceBetweenChildren <= distanceThreshold) {
                    leavesToExpect++;
                }
            }
        });

        return unmodifiableSet(result);
    }

    public static <T> Cluster<T> clusterToTree(final Collection<T> values, final DistanceFunction<T> distanceFn) {
        assert (values.isEmpty() == false);

        final Map<Cluster<T>, Map<Cluster<T>, Double>> distanceCache = new HashMap<Cluster<T>, Map<Cluster<T>, Double>>();
        final Set<Cluster<T>> clusters = new HashSet<Cluster<T>>();

        for (final T value : values) {
            final Leaf<T> newCluster = new Leaf<T>(value);
            distanceCache.put(newCluster, new HashMap<Cluster<T>, Double>());
            for (final Cluster<T> otherCluster : clusters) {
                @SuppressWarnings("unchecked")
                final Double distanceValue = distanceFn.distance( //
                        (T) ((Leaf<?>) newCluster).value, //
                        (T) ((Leaf<?>) otherCluster).value);

                distanceCache.get(newCluster).put(otherCluster, distanceValue);
                distanceCache.get(otherCluster).put(newCluster, distanceValue);
            }
            clusters.add(newCluster);
        }

        final ArrayList<Cluster<T>> path = new ArrayList<Cluster<T>>();
        while (clusters.size() > 1) {
            if (path.isEmpty()) {
                path.add(clusters.iterator().next());
            }

            final Cluster<T> c = path.get(path.size() - 1);

            Cluster<T> bestOc = null;
            double bestOcDistance = Double.POSITIVE_INFINITY;

            for (final Cluster<T> oc : clusters) {
                if (oc == c) {
                    continue;
                }

                final double distanceBetweenClusters = lookupDistance(c, oc, distanceCache);
                if (bestOc == null || distanceBetweenClusters < bestOcDistance) {
                    bestOc = oc;
                    bestOcDistance = distanceBetweenClusters;
                }
            }

            if (path.size() >= 2) {
                final Cluster<T> pc = path.get(path.size() - 2);
                final double distanceInPreviousStep = lookupDistance(pc, c, distanceCache);
                if (distanceInPreviousStep == bestOcDistance) {
                    // Local minimum
                    final Pair<T> newCluster = new Pair<T>(c, pc, distanceInPreviousStep);
                    clusters.remove(c);
                    clusters.remove(pc);
                    path.remove(path.size() - 1);
                    path.remove(path.size() - 1);

                    distanceCache.put(newCluster, new HashMap<Cluster<T>, Double>());
                    for (final Cluster<T> otherCluster : clusters) {
                        final Double distanceValue = //
                        (lookupDistance(newCluster.childA, otherCluster, distanceCache) * newCluster.childA.size + //
                                lookupDistance(newCluster.childB, otherCluster, distanceCache) * newCluster.childB.size)
                                / newCluster.size;

                        // Not strictly necessary - might be faster
                        distanceCache.get(otherCluster).remove(newCluster.childA);
                        distanceCache.get(otherCluster).remove(newCluster.childB);

                        distanceCache.get(newCluster).put(otherCluster, distanceValue);
                        distanceCache.get(otherCluster).put(newCluster, distanceValue);
                    }
                    // Not necessary - might be faster
                    distanceCache.remove(newCluster.childA);
                    distanceCache.remove(newCluster.childB);

                    clusters.add(newCluster);
                    continue;
                }
            }

            path.add(bestOc);

        }
        return clusters.iterator().next();
    }

    private static <T> double lookupDistance(final Cluster<T> c, final Cluster<T> oc,
            final Map<Cluster<T>, Map<Cluster<T>, Double>> distanceCache) {

        return distanceCache.get(c).get(oc);
    }

    // Non-createable
    private UPGMA2() {
    }

}

interface ClusterVisitor<T> {
    public void visitLeaf(Leaf<T> leaf);

    public void visitPair(Pair<T> pair);
}

abstract class Cluster<T> {
    public final int size;

    protected Cluster(final int size) {
        this.size = size;
    }

    // TODO: Implementation of this could blow the stack for biased clusterings
    public abstract void preOrderVisit(ClusterVisitor<T> visitor);

    @Override
    abstract public int hashCode();

    @Override
    abstract public boolean equals(Object obj);
}

final class Leaf<T> extends Cluster<T> {
    public final T value;

    public Leaf(final T value) {
        super(1);
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        // Only want reference equality
        return this == obj;
    }

    @Override
    public final int hashCode() {
        // Only want reference equality
        return identityHashCode(this);
    }

    @Override
    public String toString() {
        return "{" + value.toString() + "}";
    }

    @Override
    public void preOrderVisit(final ClusterVisitor<T> visitor) {
        visitor.visitLeaf(this);
    }
}

final class Pair<T> extends Cluster<T> {
    public final Cluster<T> childA;
    public final Cluster<T> childB;
    public final double averageLinkDistanceBetweenChildren;

    public Pair(final Cluster<T> childA, final Cluster<T> childB, final double averageLinkDistanceBetweenChildren) {
        super(childA.size + childB.size);
        this.childA = childA;
        this.childB = childB;
        this.averageLinkDistanceBetweenChildren = averageLinkDistanceBetweenChildren;
    }

    @Override
    // Shallow equality (are children identical references?)
    public boolean equals(final Object obj) {
        @SuppressWarnings("unchecked")
        final Pair<T> other = (Pair<T>) obj;

        return (this == other) || (this.childA == other.childA && this.childB == other.childB);
    }

    @Override
    // Shallow equality (are children identical references?)
    public int hashCode() {
        return identityHashCode(childA) + identityHashCode(childB) * 31;
    }

    @Override
    public String toString() {
        return "{" + childA.toString() + " -(" + averageLinkDistanceBetweenChildren + ")- " + childB.toString() + "}";
    }

    @Override
    public void preOrderVisit(final ClusterVisitor<T> visitor) {
        visitor.visitPair(this);
        childA.preOrderVisit(visitor);
        childB.preOrderVisit(visitor);
    }
}