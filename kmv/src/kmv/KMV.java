package kmv;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface KMV<T, H> {

    static <T, H> KMVBuilder<T, H> builder( //
            final int k, //
            final Function<T, H> objectToHash, //
            final Comparator<H> hashComparator, //
            final Function<H, Double> hashToDouble) {

        return new KMVBuilderImp<T, H>(k, objectToHash, hashComparator, hashToDouble);
    }

    public interface KMVBuilder<T, H> {
        void add(T value);

        KMV<T, H> build();
    }

    double distinctElementsEstimate();
}

class KMVUtils {
    private KMVUtils() {
    }

    public static <K, V> Comparator<Map.Entry<K, V>> compareOnValue(final Comparator<V> valueComparator) {
        return (final Map.Entry<K, V> left, final Map.Entry<K, V> right) -> {
            return valueComparator.compare(left.getValue(), right.getValue());
        };
    }
}

class KMVBuilderImp<T, H> implements KMV.KMVBuilder<T, H> {

    private final int k;
    private final Function<T, H> objectToHash;
    private final Comparator<H> hashComparator;
    private final Function<H, Double> hashToDouble;

    private final PriorityQueue<Map.Entry<T, H>> items;

    public KMVBuilderImp(final int k, final Function<T, H> objectToHash, //
            final Comparator<H> hashComparator, final Function<H, Double> hashToDouble) {
        this.k = k;
        this.objectToHash = objectToHash;
        this.hashComparator = hashComparator;
        this.hashToDouble = hashToDouble;

        this.items = new PriorityQueue<>(KMVUtils.<T, H> compareOnValue(hashComparator).reversed());
    }

    @Override
    public void add(final T value) {
        final Map.Entry<T, H> newEntry = new AbstractMap.SimpleImmutableEntry<>(value, objectToHash.apply(value));

        if (items.size() >= k) {
            if (items.comparator().compare(newEntry, items.peek()) < 0) {
                return;
            }
        }

        items.add(newEntry);
        while (items.size() > k) {
            items.remove();
        }
    }

    @Override
    public KMV<T, H> build() {
        return new KMVImp<>(k, items.stream().collect(Collectors.toList()), hashComparator, hashToDouble);
    }
}

class KMVImp<T, H> implements KMV<T, H> {

    private final int k;
    private final SortedSet<Entry<T, H>> entries;
    private final Function<H, Double> hashToDouble;

    public KMVImp(final int k, final List<Map.Entry<T, H>> entries, final Comparator<H> hashComparator,
            final Function<H, Double> hashToDouble) {
        this.k = k;
        this.entries = new TreeSet<>(KMVUtils.compareOnValue(hashComparator));
        this.hashToDouble = hashToDouble;

        this.entries.addAll(entries);
    }

    @Override
    public double distinctElementsEstimate() {
        if (entries.size() < k) {
            return entries.size();
        } else {
            final double limit = hashToDouble.apply(entries.last().getValue());
            return (k / limit) - 1.0;
        }
    }

}