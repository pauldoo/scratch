import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PercentilesTest {
	@Test
	public void confidenceIndices() {
		double[] confidenceInterval = Percentiles
				.confidenceInterval(0.95, 1000);
		assertEquals(935.554, confidenceInterval[0], 1e-3);
		assertEquals(962.596, confidenceInterval[1], 1e-3);
	}

	@Test
	public void binomialMassTest() {
		assertEquals(0.007575644925, Percentiles.binomialMass(100, 20, 0.3),
				1e-9);
	}
}
