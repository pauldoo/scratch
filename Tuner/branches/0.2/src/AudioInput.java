/*

Tuner, a simple application to help you tune your musical instrument.
Copyright (c) 2003, 2004, 2005, 2012 Paul Richards <paul.richards@gmail.com>

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


import java.io.*;

public class AudioInput {

    private DataInputStream dis;
    private int offset;


    public AudioInput(DataInputStream dis) {
	this.dis = dis;
    }

    public int readSample() throws IOException {
        int sample = offset + dis.readShort();

	if ( sample > 0 )
	    offset --;
	else if ( sample < 0 )
	    offset ++;

	return sample;
    }

}

