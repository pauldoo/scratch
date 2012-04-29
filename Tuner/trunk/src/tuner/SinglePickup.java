/*
    Copyright (c) 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package tuner;

public final class SinglePickup
{
    private final double tuneFrequencyInHz;
    private final double damping;

    private double position = 0.0;
    private double velocity = 0.0;
    private double currentAmplitude = 0.0;
    private double currentOffset = 0.0;

    public SinglePickup(double tuneFrequencyInHz, double damping)
    {
        this.tuneFrequencyInHz = tuneFrequencyInHz;
        this.damping = damping;
    }

    private final double springConstant()
    {
        return
            Math.pow(tuneFrequencyInHz * 2.0 * Math.PI, 2.0) +
            Math.pow(damping, 2.0) / 4.0;
    }

    public synchronized void process(AudioPacket packet)
    {
        final double timeStep = 1.0 / packet.getSampleFrequencyInHz();

        currentAmplitude = 0.0;
        for (double sample: packet.getSamples()) {
            final double offsettedSample = sample - currentOffset;
            currentOffset += Math.signum(offsettedSample) * 0.1;
            final double force = offsettedSample + (-damping * velocity) + (-springConstant() * position);

            if (Double.isNaN(force) || Double.isInfinite(force)) {
                throw new RuntimeException("Force Hit: "+ force);
            }

            velocity += 1.0 * force * timeStep;
            position += velocity * timeStep;

            currentAmplitude = Math.max(currentAmplitude, Math.abs(position));
            if (Double.isNaN(currentAmplitude) || Double.isInfinite(currentAmplitude)) {
                throw new RuntimeException("Amplitude Hit: "+ force);
            }
        }
    }

    public synchronized double getResponseMeasure()
    {
        return 0.5 * springConstant() * Math.pow(currentAmplitude, 2.0);
    }
}
