package kmv;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kmv.KMV.KMVBuilder;

import org.junit.Assert;
import org.junit.Test;

public class KMVTest {

    private static AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private static ThreadLocal<MessageDigest> digestCache = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    @Test
    public void unsaturatedTest() {
        final KMVBuilder<Long, BitSet> builder = createBuilder(1000);

        for (int i = 0; i < 100; i++) {
            builder.add(uid());
        }

        final double N = builder.build().distinctElementsEstimate();
        Assert.assertEquals(100, N, 0.0);
    }

    @Test
    public void saturatedTest() {

        for (final int trueN : new int[] { 100_000, 1_000_000, 10_000_000 }) {
            for (final int K : new int[] { 100, 1_000, 10_000 }) {
                final long startTime = System.currentTimeMillis();
                final List<Double> samples = IntStream.range(0, 100).parallel().mapToObj(trial -> {
                    final KMVBuilder<Long, BitSet> builder = createBuilder(K);

                    for (int i = 0; i < trueN; i++) {
                        builder.add(uid());
                    }

                    final double N = builder.build().distinctElementsEstimate();
                    return N;
                }).collect(Collectors.toList());

                final long endTime = System.currentTimeMillis();

                final Double sum = samples.stream().reduce(0.0, (a, b) -> (a + b));
                final double mean = sum / samples.size();

                final Double varSum = samples.stream() //
                        .map(v -> ((v - mean) * (v - mean))) //
                        .reduce(0.0, (a, b) -> (a + b));
                final double variance = varSum / (samples.size() - 1);

                System.out
                .println(String.format(
                        "N=%d, k=%d, mean=%f, stddev=%f (%f%%), time=%dms", //
                        trueN, K, mean, Math.sqrt(variance), 100 * (Math.sqrt(variance) / mean),
                        (endTime - startTime)));
            }
        }

    }

    @Test
    public void intersectionTest() {
        final KMVBuilder<Long, BitSet> builderA = createBuilder(1000);
        final KMVBuilder<Long, BitSet> builderB = createBuilder(1000);

        for (int i = 0; i < 10_000; i++) {
            final Long uid = uid();
            builderA.add(uid);
            builderB.add(uid);
        }
        for (int i = 10_000; i < 100_000; i++) {
            builderA.add(uid());
            builderB.add(uid());
        }

        System.out.println(KMV.intersect(builderA.build(), builderB.build()).distinctElementsEstimate());
    }

    private KMVBuilder<Long, BitSet> createBuilder(final int k) {
        final KMVBuilder<Long, BitSet> builder = KMV.<Long, BitSet> builder(k, KMVTest::uidToHash,
                KMVTest::hashComparator, KMVTest::hashToDouble);
        return builder;
    }

    private static Long uid() {
        return counter.incrementAndGet();
    }

    @Test
    public void hashToDoubleTests() {
        Assert.assertEquals(0.0, hashToDouble(bs(false, false, false, false)), 0.0);
        Assert.assertEquals(0.9375, hashToDouble(bs(true, true, true, true)), 0.0);
        Assert.assertEquals(0.625, hashToDouble(bs(true, false, true, false)), 0.0);
        Assert.assertEquals(0.3125, hashToDouble(bs(false, true, false, true)), 0.0);
    }

    private static BitSet bs(final boolean... values) {
        final BitSet bs = new BitSet(values.length);
        for (int i = 0; i < values.length; i++) {
            bs.set(i, values[i]);
        }
        return bs;
    }

    private static BitSet uidToHash(final Long uid) {
        final MessageDigest instance = getDigest();
        instance.update(uid.toString().getBytes(StandardCharsets.UTF_8));
        return BitSet.valueOf(instance.digest());
    }

    private static MessageDigest getDigest() {
        final MessageDigest instance = digestCache.get();
        instance.reset();
        return instance;
    }

    private static int hashComparator(final BitSet left, final BitSet right) {
        assert (left.length() == right.length());

        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            final int compare = Boolean.compare(left.get(i), right.get(i));
            if (compare != 0) {
                result = compare;
                break;
            }
        }

        if (Boolean.FALSE) {
            final int doubleCompare = Double.compare(hashToDouble(left), hashToDouble(right));
            Assert.assertTrue(doubleCompare == 0 || doubleCompare == result);
        }
        return result;
    }

    private static double hashToDouble(final BitSet hash) {
        double value = 0.0;
        double acc = 0.5;
        for (int i = 0; i < hash.length(); i++) {
            if (value + acc == value) {
                break;
            }
            if (hash.get(i)) {
                value += acc;
            }
            acc /= 2;
        }
        return value;
    }
}
