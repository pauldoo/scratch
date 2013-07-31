import static java.lang.System.identityHashCode;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/// Unweighted Pair Group Method with Arithmetic Mean
/// aka Hierarchical average link clustering
/// http://en.wikipedia.org/wiki/UPGMA
public final class UPGMAOld {

    public static <T> Set<Set<T>> cluster(final Collection<T> values, final double distanceThreshold,
            final DistanceFunction<T> distanceFn) {

        final Set<Cluster<T>> activeGroups = new HashSet<Cluster<T>>();
        final Map<Pair<T>, Double> knownDistances = new HashMap<Pair<T>, Double>();
        final PriorityQueue<Pair<T>> proposedMerges = new PriorityQueue<Pair<T>>(1, new ProposedMergeComparator<T>(
                knownDistances));

        populateInitialState(values, distanceFn, activeGroups, knownDistances, proposedMerges);
        repeatedlyMergeClusters(distanceThreshold, activeGroups, knownDistances, proposedMerges);
        return collectResultingClusters(activeGroups);
    }

    private static <T> void repeatedlyMergeClusters(final double distanceThreshold, final Set<Cluster<T>> activeGroups,
            final Map<Pair<T>, Double> knownDistances, final PriorityQueue<Pair<T>> proposedMerges) {
        while (true) {
            if (proposedMerges.isEmpty()) {
                return;
            }

            final Pair<T> proposedMerge = proposedMerges.remove();
            final double distanceBetweenProposedChildren = knownDistances.get(proposedMerge);
            // Not required for correctness
            knownDistances.remove(proposedMerge);

            if (distanceBetweenProposedChildren <= distanceThreshold) {
                if (activeGroups.contains(proposedMerge.childA) && activeGroups.contains(proposedMerge.childB)) {
                    applyProposedClusterMerge(activeGroups, knownDistances, proposedMerges, proposedMerge);
                } else {
                    // The proposed merge is stale, ie one or both of the
                    // children have already been merged into another cluster
                }
            } else {
                // Have reached the maximum distance threshold
                return;
            }
        }
    }

    private static <T> void populateInitialState(final Collection<T> values, final DistanceFunction<T> distanceFn,
            final Set<Cluster<T>> activeGroups, final Map<Pair<T>, Double> knownDistances,
            final PriorityQueue<Pair<T>> proposedMerges) {
        for (final T value : values) {
            final Cluster<T> cluster = new Leaf<T>(value);
            for (final Cluster<T> otherCluster : activeGroups) {
                proposeLeafMerge(distanceFn, knownDistances, proposedMerges, cluster, otherCluster);
            }
            activeGroups.add(cluster);
        }

        assert (activeGroups.size() == values.size());
        assert (proposedMerges.size() == (values.size() * (values.size() - 1)) / 2);
    }

    private static <T> Set<Set<T>> collectResultingClusters(final Set<Cluster<T>> activeGroups) {
        final Set<Set<T>> result = new HashSet<Set<T>>();
        for (final Cluster<T> cluster : activeGroups) {
            final HashSet<T> clusterValues = new HashSet<T>(cluster.size);
            cluster.collectAllValues(clusterValues);
            result.add(unmodifiableSet(clusterValues));
        }
        return unmodifiableSet(result);
    }

    private static <T> void applyProposedClusterMerge(final Set<Cluster<T>> activeGroups,
            final Map<Pair<T>, Double> knownDistances, final PriorityQueue<Pair<T>> proposedMerges,
            final Pair<T> newCluster) {
        activeGroups.remove(newCluster.childA);
        activeGroups.remove(newCluster.childB);

        for (final Cluster<T> otherCluster : activeGroups) {
            proposeClusterMerge(knownDistances, newCluster, otherCluster, proposedMerges);
        }
        activeGroups.add(newCluster);
    }

    private static <T> void proposeClusterMerge(final Map<Pair<T>, Double> knownDistances, final Pair<T> cluster,
            final Cluster<T> otherCluster, final PriorityQueue<Pair<T>> distanceQueue) {
        // Compute average distance between cluster and otherCluster
        final double meanDistA = lookupDistance(knownDistances, cluster.childA, otherCluster);
        final double meanDistB = lookupDistance(knownDistances, cluster.childB, otherCluster);

        final double newMeanDistance = //
        ((meanDistA * cluster.childA.size) + (meanDistB * cluster.childB.size)) / cluster.size;

        final Pair<T> newProposedCluster = new Pair<T>(cluster, otherCluster);
        knownDistances.put(newProposedCluster, newMeanDistance);
        distanceQueue.add(newProposedCluster);
    }

    private static <T> void proposeLeafMerge(final DistanceFunction<T> distanceFn,
            final Map<Pair<T>, Double> knownDistances, final PriorityQueue<Pair<T>> distanceQueue,
            final Cluster<T> cluster, final Cluster<T> otherCluster) {
        @SuppressWarnings("unchecked")
        final double distanceValue = distanceFn.distance( //
                (T) ((Leaf<?>) cluster).value, //
                (T) ((Leaf<?>) otherCluster).value);

        final Pair<T> proposedCluster = new Pair<T>(cluster, otherCluster);
        knownDistances.put(proposedCluster, distanceValue);
        distanceQueue.add(proposedCluster);
    }

    private static <T> Double lookupDistance(final Map<Pair<T>, Double> knownDistances, final Cluster<T> a,
            final Cluster<T> b) {
        final Pair<T> keyA = new Pair<T>(a, b);
        final Pair<T> keyB = new Pair<T>(b, a);
        final Double resultA = knownDistances.get(keyA);
        final Double resultB = knownDistances.get(keyB);
        assert ((resultA == null) != (resultB == null));

        if (resultA != null) {
            return resultA;
        } else {
            return resultB;
        }
    }

    // Private c'tor non-createable
    private UPGMAOld() {
    };

    private static final class ProposedMergeComparator<T> implements Comparator<Pair<T>> {
        private final Map<Pair<T>, Double> knownDistances;

        public ProposedMergeComparator(final Map<Pair<T>, Double> knownDistances) {
            this.knownDistances = knownDistances;
        }

        @Override
        public int compare(final Pair<T> o1, final Pair<T> o2) {
            return Double.compare(knownDistances.get(o1), knownDistances.get(o2));
        }
    }

    private static abstract class Cluster<T> {
        public final int size;

        protected Cluster(final int size) {
            this.size = size;
        }

        public abstract void collectAllValues(Set<T> set);

    }

    private static final class Leaf<T> extends Cluster<T> {
        public final T value;

        public Leaf(final T value) {
            super(1);
            this.value = value;
        }

        @Override
        public void collectAllValues(final Set<T> set) {
            set.add(value);
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
    }

    private static final class Pair<T> extends Cluster<T> {
        public final Cluster<T> childA;
        public final Cluster<T> childB;

        private Pair(final Cluster<T> childA, final Cluster<T> childB) {
            super(childA.size + childB.size);
            this.childA = childA;
            this.childB = childB;
        }

        @Override
        public void collectAllValues(final Set<T> set) {
            childA.collectAllValues(set);
            childB.collectAllValues(set);
        }

        @Override
        // Shallow equality (children are identical references), used when
        // looking up known distances
        public boolean equals(final Object obj) {
            @SuppressWarnings("unchecked")
            final Pair<T> other = (Pair<T>) obj;

            return (this == other) || (this.childA == other.childA && this.childB == other.childB);
        }

        @Override
        // Shallow equality (children are identical references), used when
        // looking up known distances
        public int hashCode() {
            return identityHashCode(childA) + identityHashCode(childB) * 31;
        }

        @Override
        public String toString() {
            return "{" + childA.toString() + "," + childB.toString() + "}";
        }
    }

}
