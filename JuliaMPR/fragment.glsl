/*
Copyright (c) 2012 Paul Richards <paul.richards@gmail.com>

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

precision mediump float;

varying vec4 vVolumePosition;

void main(void)
{
    const int maxIter = 50;
    const float escape = 10000.0;
    const float brightness = 4.0;

    vec2 c = vVolumePosition.xy;
    vec2 z = vVolumePosition.zw;

    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    for (int i = 0; i <= maxIter; i++) {
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        float mag = dot(z, z);
        if (mag > escape) {
            float shade = (float(i) - log(mag) / log(escape)) / float(maxIter);
            shade = 1.0 - exp( - brightness * shade );
            gl_FragColor = vec4(vec3(shade), 1.0);  
            break;      
        }
    }
}
