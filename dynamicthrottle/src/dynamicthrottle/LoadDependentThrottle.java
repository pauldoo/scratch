package dynamicthrottle;

import static java.lang.Math.max;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

import java.lang.management.OperatingSystemMXBean;
import java.util.Random;

public class LoadDependentThrottle {
    private static final double DEFAULT_ACCEPTABLE_BASELINE_LOAD_MULTIPLE = 1.0;
    private static final double DEFAULT_AGGRESSION = 1.0;

    private final OperatingSystemMXBean osBean;
    private final Random random;
    double acceptableBaselineLoadMultiple;
    private final double aggression;

    public static LoadDependentThrottle createDefaultLoadDependentThrottle() {
        return new LoadDependentThrottle(getOperatingSystemMXBean(), DEFAULT_ACCEPTABLE_BASELINE_LOAD_MULTIPLE,
                DEFAULT_AGGRESSION);
    }

    public LoadDependentThrottle(final OperatingSystemMXBean osBean, final double acceptableBaselineLoadMultiple,
            final double aggression) {
        this.osBean = osBean;
        this.random = new Random();
        this.acceptableBaselineLoadMultiple = acceptableBaselineLoadMultiple;
        this.aggression = aggression;
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
        final double overloadMultiple = max(0.0, (loadMultiple - acceptableBaselineLoadMultiple));
        final double resultingProbability = Math.exp(-overloadMultiple * aggression);
        System.out.println(String.format("%f %d %f %f %f", //
                systemLoadAverage, availableProcessors, loadMultiple, overloadMultiple, resultingProbability));
        return resultingProbability;
    }

    public void waitUntilOkayToProceed() {
        try {
            long delayInMs = 1000;
            while (!shouldProceedWithWork()) {
                System.out.println(String.format("Backing off for %dms", delayInMs));
                Thread.sleep(delayInMs);
                delayInMs = delayInMs * 2;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
