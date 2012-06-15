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

package pigeon.report;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public final class DefaultStreamProvider implements StreamProvider
{
    private final File outputDirectory;;
    private final Collection<File> filesToShow = new LinkedList<File>();
    private final Collection<OutputStream> streams = new LinkedList<OutputStream>();

    public DefaultStreamProvider() throws IOException
    {
        outputDirectory = Utilities.createTemporaryDirectory("RacePoint");
    }

    public void closeAllStreams() throws IOException
    {
        for (OutputStream stream: streams) {
            stream.close();
        }
    }

    public Collection<File> getFilesToShow()
    {
        return Collections.unmodifiableCollection(filesToShow);
    }

    @Override
    public OutputStream createNewStream(String name, boolean showToUser) throws IOException
    {
        OutputStream result;
        File outputFile = new File(outputDirectory, name);
        if (outputFile.createNewFile() != true) {
            throw new IOException("Unable to create new file: " + outputFile.toString());
        }

        FileOutputStream fileOut = new FileOutputStream(outputFile);
        if (showToUser) {
            filesToShow.add(outputFile);
        }
        result = new BufferedOutputStream(fileOut);
        streams.add(result);
        return result;
    }
}
