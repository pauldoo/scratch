/*
    Copyright (c) 2005, 2006, 2007, 2008, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import pigeon.model.Distance;

/**
    Produces an HTML report of the distances for a member or racepoint.
*/
public final class DistanceReporter<Target> implements Reporter
{
    final String organization;
    final String source;
    final String targetTypeName;
    final Map<Target, Distance> distances;

    public DistanceReporter(String organization, String source, String targetTypeName, Map<Target, Distance> distances)
    {
        this.organization = organization;
        this.source = source;
        this.targetTypeName = targetTypeName;
        this.distances = distances;
    }

    @Override
    public void write(StreamProvider streamProvider) throws IOException
    {
        final OutputStream stream = streamProvider.createNewStream("Distances.html", true);
        PrintStream out = Utilities.writeHtmlHeader(stream, "Distances for " + source);
        out.println("<div class='outer'>");
        out.println("<h1>" + organization + "</h1>");
        out.println("<h2>Distances for " + source + "</h2>");

        out.println("<table>");
        out.println("<tr><th>" + targetTypeName + "</th><th>Distance</th></tr>");
        for (Map.Entry<Target, Distance> entry: distances.entrySet()) {
            out.println("<tr><td>" + entry.getKey().toString() + "</td><td>" + entry.getValue().toString() + "</td></tr>");
        }
        out.println("</table>");
        out.println("</div>");
        Utilities.writeHtmlFooter(out);
    }
}
