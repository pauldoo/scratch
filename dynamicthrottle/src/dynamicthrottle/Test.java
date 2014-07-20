package dynamicthrottle;

import static dynamicthrottle.LoadDependentThrottle.createDefaultLoadDependentThrottle;

public class Test implements Runnable {
    LoadDependentThrottle throttle = createDefaultLoadDependentThrottle();

    public static void main(final String[] args) {

        final int THREAD_COUNT = 100;
        final Runnable runner = new Test();
        for (int i = 1; i <= THREAD_COUNT; i++) {
            final Thread thread = new Thread(runner);
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) (30000 * Math.random()));
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            throttle.waitUntilOkayToProceed();
            doSomethingExpensive();
        }
    }

    private void doSomethingExpensive() {
        System.out.println("Doing expensive work");
        final long start = System.currentTimeMillis();
        long result = 0;
        final long target = (long) (Math.random() * 1000000000);
        for (int i = 0; i <= target; i++) {
            result += i;
        }
        final long end = System.currentTimeMillis();
        System.out.println(String.format("Did some work, answer was %d, took %dms", result, (end - start)));
    }
}
