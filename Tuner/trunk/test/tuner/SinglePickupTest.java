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

import org.junit.Test;
import static org.junit.Assert.*;


public final class SinglePickupTest
{
    @Test
    public void process()
    {
        AudioPacket packet = (new SingleFrequencyAudioSource(2000)).nextPacket();

        for (double damping = 0.5; damping <= 6.0; damping += 0.5) {
            double bestFrequency = 0;
            double bestResponse = 0;
            for (double frequency = 1980; frequency <= 2020; frequency += 1.0) {
                SinglePickup pickup = new SinglePickup(frequency, damping);
                pickup.process(packet);
                double response = pickup.getResponseMeasure();
                if (response > bestResponse) {
                    bestFrequency = frequency;
                    bestResponse = response;
                }
            }
            //System.out.println(bestFrequency);
            assertTrue("Pickup at 2000Hz should have the best response", bestFrequency == 2000);
        }
    }
}