/*
    Copyright (C) 2010  Paul Richards.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fractals;

import fractals.math.Triplex;
import junit.framework.TestCase;

/**
 *
 * @author pauldoo
 */
public class MandelbulbTest extends TestCase {
    
    public MandelbulbTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDistanceEstimate() {
        Triplex point = new Triplex(1.0, 1.0, 0.0);

        final boolean inside = Mandelbulb.evaluate(point, Mandelbulb.maxIterations);
        System.out.println(inside);
        final double distanceEstimate = Mandelbulb.distanceEstimate(point, Mandelbulb.maxIterations);

        System.out.println(distanceEstimate);
    }

    public void testSomeMethod() {
        //Mandelbulb.computeNormal(new Triplex(0.2912884237062956, 0.04246198765516082, 0.640625), 10);
        Mandelbulb.computeNormal(new Triplex(0.291, 0.042, 0.641), 4);
    }
}
