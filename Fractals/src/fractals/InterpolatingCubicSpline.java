/*
    Copyright (c) 2008, 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and/or distribute this software for any
    purpose with or without fee is hereby granted, provided that the above
    copyright notice and this permission notice appear in all copies.

    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package fractals;

final class InterpolatingCubicSpline
{
    private final double samples[];
    private final double coefficients[][];

    InterpolatingCubicSpline(double[] samples)
    {
        this.samples = Utilities.copyDoubleArray(samples);
        this.coefficients = computeCoefficients(this.samples);
        if (this.samples.length != this.coefficients.length+1) {
            throw new IllegalArgumentException("Coefficients array should be exactly one element shorter than the samples array");
        }
    }

    double sample(double x)
    {
        final int i = Utilities.clamp(0, (int)Math.floor(x), coefficients.length-1);
        final double[] localCoefficients = coefficients[i];
        if (localCoefficients.length != 4) {
            throw new IllegalArgumentException("Incorrect number of cubic coefficients");
        }

        final double z = x - i;
        final double result =
                localCoefficients[0] +
                localCoefficients[1] * z +
                localCoefficients[2] * (z * z) +
                localCoefficients[3] * (z * z * z);
        return result;
    }

    private static double[][] computeCoefficients(double[] samples)
    {
        // Source array is valid over [0, n].
        final int n = samples.length - 1;

        double[] gamma = new double[n+1];
        gamma[0] = 0.5;
        for (int i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i-1]);
        }
        gamma[n] = 1 / (2 - gamma[n-1]);

        double[] delta = new double[n+1];
        delta[0] = 3 * (samples[1] - samples[0]) * gamma[0];
        for (int i = 1; i < n; i++) {
            delta[i] = (3 * (samples[i+1] - samples[i-1]) - delta[i-1]) * gamma[i];
        }
        delta[n] = (3 * (samples[n] - samples[n-1]) - delta[n-1]) * gamma[n];

        double[] d = new double[n+1];
        d[n] = delta[n];
        for (int i = n-1; i>= 0; i--) {
            d[i] = delta[i] - gamma[i] * d[i+1];
        }

        double[][] result = new double[n][];
        for (int i = 0; i < n; i++) {
            result[i] = new double[4];
            result[i][0] = samples[i];
            result[i][1] = d[i];
            result[i][2] = 3 * (samples[i+1] - samples[i]) - 2 * d[i] - d[i+1];
            result[i][3] = 2 * (samples[i] - samples[i+1]) + d[i] + d[i+1];
        }

        return result;
    }
}
