import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

public class PercentilesTest {
	@Test
	public void confidenceIndices() {
		double[] confidenceInterval = Percentiles.confidenceIntervalIndices(
				0.95, 1000);
		assertEquals(935.554, confidenceInterval[0], 1e-3);
		assertEquals(962.596, confidenceInterval[1], 1e-3);
	}

	@Test
	public void binomialMassTest() {
		assertEquals(0.007575644925, Percentiles.binomialMass(100, 20, 0.3),
				1e-9);
	}

	@Test
	public void numericTest() {
		final RandomGenerator generator = new MersenneTwister(42);
		final int trialCount = 1000;
		final int sampleCount = 500;

		for (int per = 10; per <= 90; per += 10) {
			int pCount = 0;
			for (int i = 0; i < trialCount; i++) {
				final Double[] samples = new Double[sampleCount];
				for (int k = 0; k < sampleCount; k++) {
					final double x = generator.nextDouble();
					samples[k] = nonLinearFunc(x);
				}

				Arrays.sort(samples);
				final double perAsFrac = per / 100.0;
				final double interval[] = Percentiles.confidenceIntervalValues(
						perAsFrac, Arrays.<Double> asList(samples));

				if (interval[0] <= nonLinearFunc(perAsFrac)
						&& nonLinearFunc(perAsFrac) <= interval[1]) {
					pCount++;
				}
			}

			System.out.println(((double) pCount) / trialCount);
		}
	}

	private static double nonLinearFunc(double d) {
		return d * d + 6;
	}
}
