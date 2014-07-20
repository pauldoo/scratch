package dynamicthrottle;

import static java.lang.Math.max;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

import java.lang.management.OperatingSystemMXBean;
import java.util.Random;

public class LoadDependentThrottle {

    private final OperatingSystemMXBean osBean;
    private final Random random;

    public static LoadDependentThrottle createDefaultLoadDependentThrottle() {
        return new LoadDependentThrottle(getOperatingSystemMXBean());
    }

    public LoadDependentThrottle(final OperatingSystemMXBean osBean) {
        this.osBean = osBean;
        this.random = new Random();
    }

    public boolean shouldProceedWithWork() {
        final double proceedProbability = proceedProbability();
        final boolean result = random.nextDouble() < proceedProbability;
        System.out.println(String.format("Should proceed: %b", result));
        return result;
    }

    private double proceedProbability() {
        final double systemLoadAverage = osBean.getSystemLoadAverage();
        final int availableProcessors = osBean.getAvailableProcessors();
        final double loadMultiple = systemLoadAverage / availableProcessors;
        final double resultingProbability = 1.0 / max(1.0, loadMultiple);
        // final double resultingProbability = 1.0 / max(1.0, exp(loadMultiple -
        // 1));
        System.out.println(String.format("%f %d %f %f", //
                systemLoadAverage, availableProcessors, loadMultiple, resultingProbability));
        return resultingProbability;
    }

    public void waitUntilOkayToProceed() {
        try {
            long delayInMs = 1000;
            while (!shouldProceedWithWork()) {
                final double r = (random.nextDouble() * 2.0 + 2.0) / 3.0;
                final long sleep = (long) (delayInMs * r);
                System.out.println(String.format("Backing off for %dms (%d)", sleep, delayInMs));
                Thread.sleep(sleep);
                delayInMs = delayInMs * 2;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
