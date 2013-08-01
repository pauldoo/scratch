/*
    Copyright (c) 2013 Paul Richards <paul.richards@gmail.com>
    
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
package upgma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unweighted Pair Group Method with Arithmetic Mean (UPGMA) aka Hierarchical
 * average link clustering http://en.wikipedia.org/wiki/UPGMA
 */
public final class UPGMA {

    public static <T> Set<Set<T>> cluster(final Collection<T> values, final double distanceThreshold,
            final DistanceFunction<T> distanceFn) {

        return dendogramToClusters(constructDendogram(values, distanceFn), distanceThreshold);
    }

    public static <T> Set<Set<T>> dendogramToClusters(final Dendogram<T> clustering, final double distanceThreshold) {
        final ThresholdingDendogramVisitor<T> visitor = new ThresholdingDendogramVisitor<>(distanceThreshold);
        clustering.preOrderVisit(visitor);
        return visitor.result();
    }

    /**
     * Produces the dendogram cluster tree for UPGMA In cases of multiple
     * equally minimal UPGMA solutions, will return one arbitrarily. The
     * arbitrary choice will be deterministic based on the iterator ordering of
     * the input values.
     * 
     * O(N^2)
     */
    public static <T> Dendogram<T> constructDendogram(final Collection<T> values, final DistanceFunction<T> distanceFn) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cannot cluster the empty set.");
        }

        final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache = new HashMap<Dendogram<T>, Map<Dendogram<T>, Double>>();
        final Set<Dendogram<T>> clusters = new LinkedHashSet<Dendogram<T>>();

        populateInitialState(values, distanceFn, distanceCache, clusters);
        repeatedlyMergeClusters(distanceCache, clusters);

        return clusters.iterator().next();
    }

    private static <T> void repeatedlyMergeClusters(final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache,
            final Set<Dendogram<T>> clusters) {
        final ArrayList<Dendogram<T>> path = new ArrayList<Dendogram<T>>();
        while (clusters.size() > 1) {
            if (path.isEmpty()) {
                path.add(clusters.iterator().next());
            } else {
                final Dendogram<T> lastCluster = path.get(path.size() - 1);
                final Dendogram<T> nextCluster = closestCluster(lastCluster, clusters, distanceCache);

                if (nextStepIsNoBetterThanPreviousStep(path, nextCluster, distanceCache)) {
                    mergeClustersInLastStep(distanceCache, clusters, path);
                } else {
                    path.add(nextCluster);
                }
            }
        }
    }

    private static <T> boolean nextStepIsNoBetterThanPreviousStep(final ArrayList<Dendogram<T>> path,
            final Dendogram<T> nextCluster, final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache) {
        if (path.size() >= 2) {
            final Dendogram<T> lastCluster = path.get(path.size() - 1);
            final Dendogram<T> nextToLastCluster = path.get(path.size() - 2);
            final double distanceInNextStep = lookupDistance(lastCluster, nextCluster, distanceCache);
            final double distanceInPreviousStep = lookupDistance(nextToLastCluster, lastCluster, distanceCache);
            if (distanceInPreviousStep == distanceInNextStep) {
                return true;
            }
        }
        return false;
    }

    private static <T> void mergeClustersInLastStep(final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache,
            final Set<Dendogram<T>> clusters, final ArrayList<Dendogram<T>> path) {
        final Dendogram<T> lastCluster = path.get(path.size() - 1);
        final Dendogram<T> nextToLastCluster = path.get(path.size() - 2);
        final double distanceInPreviousStep = lookupDistance(nextToLastCluster, lastCluster, distanceCache);

        final Pair<T> newCluster = new Pair<T>(lastCluster, nextToLastCluster, distanceInPreviousStep);

        clusters.remove(lastCluster);
        clusters.remove(nextToLastCluster);
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);

        computeDistancesToNewCluster(distanceCache, clusters, newCluster);
    }

    private static <T> void computeDistancesToNewCluster(
            final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache, final Set<Dendogram<T>> clusters,
            final Pair<T> newCluster) {
        distanceCache.put(newCluster, new HashMap<Dendogram<T>, Double>());
        for (final Dendogram<T> otherCluster : clusters) {
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
        clusters.add(newCluster);

        // Not strictly necessary - might be faster
        distanceCache.remove(newCluster.childA);
        distanceCache.remove(newCluster.childB);
    }

    private static <T> Dendogram<T> closestCluster(final Dendogram<T> cluster, final Set<Dendogram<T>> allClusters,
            final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache) {
        Dendogram<T> closestOtherCluster = null;
        double closestOtherClusterDistance = Double.POSITIVE_INFINITY;

        for (final Dendogram<T> otherCluster : allClusters) {
            if (otherCluster != cluster) {
                final double distanceBetweenClusters = lookupDistance(cluster, otherCluster, distanceCache);
                if (closestOtherCluster == null || distanceBetweenClusters < closestOtherClusterDistance) {
                    closestOtherCluster = otherCluster;
                    closestOtherClusterDistance = distanceBetweenClusters;
                }
            }
        }
        return closestOtherCluster;
    }

    private static <T> void populateInitialState(final Collection<T> values, final DistanceFunction<T> distanceFn,
            final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache, final Set<Dendogram<T>> clusters) {
        for (final T value : values) {
            final Leaf<T> newCluster = new Leaf<T>(value);
            distanceCache.put(newCluster, new HashMap<Dendogram<T>, Double>());
            for (final Dendogram<T> otherCluster : clusters) {
                @SuppressWarnings("unchecked")
                final Double distanceValue = distanceFn.distance( //
                        (T) ((Leaf<?>) newCluster).value, //
                        (T) ((Leaf<?>) otherCluster).value);

                distanceCache.get(newCluster).put(otherCluster, distanceValue);
                distanceCache.get(otherCluster).put(newCluster, distanceValue);
            }
            clusters.add(newCluster);
        }
    }

    private static <T> double lookupDistance(final Dendogram<T> c, final Dendogram<T> oc,
            final Map<Dendogram<T>, Map<Dendogram<T>, Double>> distanceCache) {

        return distanceCache.get(c).get(oc);
    }

    // Non-createable
    private UPGMA() {
    }
}