import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

public class UPGMATest {
    @Test
    public void emptyInput() {
        final DistanceFunction<Object> distanceFn = new FixedDistance<Object>(4.0);
        final Set<Set<Object>> clusters = UPGMAOld.<Object> cluster(setOf(), 1.0, distanceFn);

        Assert.assertEquals(setOf(), clusters);
    }

    @Test
    public void singleValueInput() {
        final DistanceFunction<String> distanceFn = new FixedDistance<String>(4.0);
        final Set<Set<String>> clusters = UPGMAOld.cluster(setOf("A"), 1.0, distanceFn);

        Assert.assertEquals(setOf(setOf("A")), clusters);
    }

    @Test
    public void nothingLinks() {
        final DistanceFunction<String> distanceFn = new FixedDistance<String>(4.0);
        final Set<Set<String>> clusters = UPGMAOld.cluster(setOf("A", "B", "C", "D"), 1.0, distanceFn);

        Assert.assertEquals(setOf(setOf("A"), setOf("B"), setOf("C"), setOf("D")), clusters);
    }

    @Test
    public void everythingLinks() {
        final DistanceFunction<String> distanceFn = new FixedDistance<String>(4.0);
        final Set<Set<String>> clusters = UPGMAOld.cluster(setOf("A", "B", "C", "D"), 10.0, distanceFn);

        Assert.assertEquals(setOf(setOf("A", "B", "C", "D")), clusters);
    }

    @Test
    public void someItemsLinked() {
        final DistanceFunction<Double> distanceFn = new DoubleDistance();
        final Set<Set<Double>> clusters = UPGMAOld.cluster(setOf(1.0, 2.0, 11.0, 12.0), 2.0, distanceFn);

        Assert.assertEquals(setOf(setOf(1.0, 2.0), setOf(11.0, 12.0)), clusters);
    }

    @Test
    public void itemsClusterInHierarchyHighThreshold() {
        final DistanceFunction<Double> distanceFn = new DoubleDistance();
        final Set<Set<Double>> clusters = UPGMAOld.cluster(setOf(1.0, 2.0, 4.0, 5.0), 5.0, distanceFn);
        Assert.assertEquals(setOf(setOf(1.0, 2.0, 4.0, 5.0)), clusters);
    }

    @Test
    public void itemsClusterInHierarchyLowThreshold() {
        final DistanceFunction<Double> distanceFn = new DoubleDistance();
        final Set<Set<Double>> clusters = UPGMAOld.cluster(setOf(1.0, 2.0, 4.0, 5.0), 2.0, distanceFn);
        Assert.assertEquals(setOf(setOf(1.0, 2.0), setOf(4.0, 5.0)), clusters);
    }

    @Test
    // Example from http://www.southampton.ac.uk/~re1u06/teaching/upgma/
    public void knownExample() {
        final Map<String, Map<String, Double>> distances = new HashMap<String, Map<String, Double>>();
        distances.put("turtle", new HashMap<String, Double>());
        distances.put("man", new HashMap<String, Double>());
        distances.put("tuna", new HashMap<String, Double>());
        distances.put("chicken", new HashMap<String, Double>());
        distances.put("moth", new HashMap<String, Double>());
        distances.put("monkey", new HashMap<String, Double>());
        distances.put("dog", new HashMap<String, Double>());
        distances.get("man").put("turtle", 19.0);
        distances.get("tuna").put("turtle", 27.0);
        distances.get("tuna").put("man", 31.0);
        distances.get("chicken").put("turtle", 8.0);
        distances.get("chicken").put("man", 18.0);
        distances.get("chicken").put("tuna", 26.0);
        distances.get("moth").put("turtle", 33.0);
        distances.get("moth").put("man", 36.0);
        distances.get("moth").put("tuna", 41.0);
        distances.get("moth").put("chicken", 31.0);
        distances.get("monkey").put("turtle", 18.0);
        distances.get("monkey").put("man", 1.0);
        distances.get("monkey").put("tuna", 32.0);
        distances.get("monkey").put("chicken", 17.0);
        distances.get("monkey").put("moth", 35.0);
        distances.get("dog").put("turtle", 13.0);
        distances.get("dog").put("man", 13.0);
        distances.get("dog").put("tuna", 29.0);
        distances.get("dog").put("chicken", 14.0);
        distances.get("dog").put("moth", 28.0);
        distances.get("dog").put("monkey", 12.0);

        final DistanceFunction<String> distanceFn = new DistanceFunction<String>() {
            @Override
            public double distance(final String a, final String b) {
                if (distances.get(a).containsKey(b)) {
                    return distances.get(a).get(b) / 2.0;
                } else {
                    return distances.get(b).get(a) / 2.0;
                }
            }
        };

        final Set<String> allValues = setOf("turtle", "man", "tuna", "chicken", "moth", "monkey", "dog");

        testClusteringToThreshold(distanceFn, allValues, 0.0, setOf(//
                setOf("turtle"), //
                setOf("man"), //
                setOf("tuna"), //
                setOf("chicken"), //
                setOf("moth"), //
                setOf("monkey"), //
                setOf("dog")));

        testClusteringToThreshold(distanceFn, allValues, 0.5, setOf(//
                setOf("turtle"), //
                setOf("man", "monkey"), //
                setOf("tuna"), //
                setOf("chicken"), //
                setOf("moth"), //
                setOf("dog")));
        testClusteringToThreshold(distanceFn, allValues, 4.0, setOf(//
                setOf("turtle", "chicken"), //
                setOf("man", "monkey"), //
                setOf("tuna"), //
                setOf("moth"), //
                setOf("dog")));
        testClusteringToThreshold(distanceFn, allValues, 6.25, setOf(//
                setOf("turtle", "chicken"), //
                setOf("man", "monkey", "dog"), //
                setOf("tuna"), //
                setOf("moth")));
        testClusteringToThreshold(distanceFn, allValues, 8.25, setOf(//
                setOf("turtle", "chicken", "man", "monkey", "dog"), //
                setOf("tuna"), //
                setOf("moth")));
        testClusteringToThreshold(distanceFn, allValues, 14.5, setOf(//
                setOf("turtle", "chicken", "man", "monkey", "dog", "tuna"), //
                setOf("moth")));
        testClusteringToThreshold(distanceFn, allValues, 17.0, setOf(//
                setOf("turtle", "chicken", "man", "monkey", "dog", "tuna", "moth")));
    }

    private void testClusteringToThreshold(final DistanceFunction<String> distanceFn, final Set<String> allValues,
            final double threshold, final Set<Set<String>> expected) {

        final Set<Set<String>> clusters = UPGMA2.cluster(allValues, threshold + 0.1, distanceFn);
        Assert.assertEquals(expected, clusters);
    }

    @Test
    // @Ignore
    public void scaleTest() throws FileNotFoundException, UnsupportedEncodingException {
        final Collection<Double> values = new ArrayList<Double>();
        final DistanceFunction<Double> distanceFn = new DoubleDistance();

        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(
                "/tmp/scale.csv")), "UTF-8"), true);
        for (int i = 1; i <= 650; i++) {
            values.add(Math.random());

            final long start1 = System.currentTimeMillis();
            if (i <= 400) {
                UPGMAOld.cluster(values, 10.0, distanceFn);
            }
            final long end1 = System.currentTimeMillis();

            final long start2 = System.currentTimeMillis();
            UPGMA2.cluster(values, 10.0, distanceFn);
            final long end2 = System.currentTimeMillis();

            writer.println(i + "," + (end1 - start1) + "," + (end2 - start2));
        }
        writer.close();
    }

    @SafeVarargs
    private static <T> Set<T> setOf(final T... args) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(args)));
    }

    private static class FixedDistance<T> implements DistanceFunction<T> {

        private final double distance;

        public FixedDistance(final double distance) {
            super();
            this.distance = distance;
        }

        @Override
        public double distance(final T a, final T b) {
            return distance;
        }

    }

    private static class DoubleDistance implements DistanceFunction<Double> {
        @Override
        public double distance(final Double a, final Double b) {
            return Math.abs(a - b);
        }
    }

}
