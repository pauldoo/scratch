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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
    An implementation of AudioSource that uses the computer's microphone.
*/
final class MicrophoneAudioSource implements AudioSource, Closeable
{
    static final float SAMPLE_FREQUENCY = 44100.0f;

    private DataInputStream stream;

    public MicrophoneAudioSource() throws LineUnavailableException
    {
        AudioFormat audioFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, SAMPLE_FREQUENCY, 16, 1, 2, SAMPLE_FREQUENCY, true );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(audioFormat);

        AudioInputStream ais = new AudioInputStream( targetDataLine );
        targetDataLine.start();

        this.stream = new DataInputStream(new BufferedInputStream( ais ));
    }

    public synchronized void close()
    {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                stream = null;
            }
        }
    }

    public synchronized AudioPacket nextPacket()
    {
        try {
            final double[] samples = new double[(int)(SAMPLE_FREQUENCY * 0.03)]; // Take 30ms of audio at a time
            final byte[] dataPacket = new byte[samples.length * 2];
            stream.readFully(dataPacket);
            final DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(dataPacket));
            for (int i = 0; i < samples.length; i++) {
                samples[i] = dataStream.readShort();
            }
            return new AudioPacket(samples, SAMPLE_FREQUENCY);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
