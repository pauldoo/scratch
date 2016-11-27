package kmv;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
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

    Collection<T> allElements();

    int k();

    static <T, H> KMV<T, H> intersect(final KMV<T, H> left, final KMV<T, H> right) {

        final HashSet<T> intersection = new HashSet<>();
        intersection.addAll(left.allElements());
        intersection.retainAll(right.allElements());

        final KMVBuilder<T, H> builder = builder(intersection.size(), left.objectToHash(), left.hashComparator(),
                left.hashToDouble());

        for (final T value : intersection) {
            builder.add(value);
        }
        return builder.build();
    }

    Function<T, H> objectToHash();

    Comparator<H> hashComparator();

    Function<H, Double> hashToDouble();
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
        return new KMVImp<>(k, items.stream().collect(Collectors.toList()), objectToHash, hashComparator, hashToDouble);
    }
}

class KMVImp<T, H> implements KMV<T, H> {

    private final int k;
    private final Function<T, H> objectToHash;
    private final Comparator<H> hashComparator;
    private final Function<H, Double> hashToDouble;
    private final SortedSet<Entry<T, H>> entries;

    public KMVImp(final int k, final Collection<Map.Entry<T, H>> entries, final Function<T, H> objectToHash,
            final Comparator<H> hashComparator, final Function<H, Double> hashToDouble) {
        this.k = k;
        this.objectToHash = objectToHash;
        this.hashComparator = hashComparator;
        this.hashToDouble = hashToDouble;
        this.entries = new TreeSet<>(KMVUtils.compareOnValue(hashComparator));

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

    @Override
    public Collection<T> allElements() {
        return entries.stream().map(e -> e.getKey()).collect(Collectors.toList());
    }

    @Override
    public int k() {
        return k;
    }

    @Override
    public Function<T, H> objectToHash() {
        return objectToHash;
    }

    @Override
    public Comparator<H> hashComparator() {
        return hashComparator;
    }

    @Override
    public Function<H, Double> hashToDouble() {
        return hashToDouble;
    }
}