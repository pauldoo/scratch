package hashtest;

import java.util.HashSet;
import java.util.Set;

public class Go {
    public static void main(final String[] args) {
        for (int n = 0; n <= 100000; n += 1000) {

            final Set<Integer> set = createSet(n);
            doTiming(set);
        }
    }

    private static Set<Integer> createSet(final int n) {
        final Set<Integer> result = new HashSet<>();
        for (int v = 1; v <= n; v++) {
            result.add(v);
        }
        return result;
    }

    private static void doTiming(final Set<?> set) {
        final int size = set.size();
        final long begin = System.nanoTime();
        removeAll(set);
        final long end = System.nanoTime();
        System.out.println(String.format("%d\t%d", size, (end - begin)));
    }

    private static void removeAll(final Set<?> set) {
        while (set.isEmpty() == false) {
            set.remove(set.iterator().next());
        }
    }
}
