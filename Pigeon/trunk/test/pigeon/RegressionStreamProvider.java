/*
    Copyright (c) 2008, 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and distribute this software for any
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

package pigeon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import pigeon.report.StreamProvider;

final class RegressionStreamProvider implements StreamProvider
{
    final SortedMap<String, ByteArrayOutputStream> streams = new TreeMap<String, ByteArrayOutputStream>();

    @Override
    public OutputStream createNewStream(String filename, boolean showToUser) throws IOException {
        if (streams.containsKey(filename)) {
            throw new IOException("Duplicate filename");
        }
        if ((filename.endsWith(".html") || filename.endsWith(".xml")) != showToUser) {
            throw new IOException("Only .html and .xml files should be shown to the user");
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        streams.put(filename, result);
        return result;
    }

    public byte[] getBytes()
    {
        if (streams.size() != 1) {
            throw new IllegalStateException("Do not one stream");
        }
        return getBytes(streams.firstKey());
    }

    public byte[] getBytes(String filename)
    {
        return streams.get(filename).toByteArray();
    }

    public Set<String> getFilenames()
    {
        return Collections.unmodifiableSet(new TreeSet<String>(streams.keySet()));
    }
}
