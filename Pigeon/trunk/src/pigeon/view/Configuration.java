/*
    Copyright (c) 2005, 2006, 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import pigeon.competitions.Competition;
import pigeon.competitions.Nomination;
import pigeon.competitions.Pool;

/**
 * Loads the application configuration from an XML file.
 */
public final class Configuration
{
    private final Mode mode;
    private final String resultsFooter;
    private final List<Competition> competitions;

    public static enum Mode {
        FEDERATION,
        CLUB
    }

    public Configuration(InputStream input) throws IOException
    {
        final Properties properties = new Properties();
        properties.loadFromXML(input);

        this.mode = loadMode(properties);
        this.resultsFooter = loadResultsFooter(properties);
        this.competitions = loadCompetitions(properties);
    }

    private static Mode loadMode(Properties properties)
    {
        return Mode.valueOf(properties.getProperty("Mode"));
    }

    private static String loadResultsFooter(Properties properties)
    {
        return properties.getProperty("ResultsFooter");
    }

    private static List<Competition> loadCompetitions(Properties properties) throws IOException
    {
        /**
            This code is a bit messy, not sure what the ideal
            method should be for doing this.
        */
        final int count = Integer.parseInt(properties.getProperty("Competition.Count"));
        List<Competition> result = new ArrayList<Competition>();
        for (int i = 1; i <= count; ++i) {
            final String competitionPrefix = "Competition." + i;
            final String name = properties.getProperty(competitionPrefix + ".Name");
            final String type = properties.getProperty(competitionPrefix + ".Type");
            final double cost = Double.parseDouble(properties.getProperty(competitionPrefix + ".Cost"));
            final double clubTake = Double.parseDouble(properties.getProperty(competitionPrefix + ".ClubTake"));
            final boolean availableInOpen = Boolean.parseBoolean(properties.getProperty(competitionPrefix + ".AvailableInOpen"));

            if ("Pool".equals(type)) {
                // Parse out Pool specific fields
                final int payoutPeriod = Integer.parseInt(properties.getProperty(competitionPrefix + ".PayoutPeriod"));
                result.add(new Pool(name, cost, clubTake, availableInOpen, payoutPeriod));
            } else if ("Nomination".equals(type)) {
                // Parse out Nomination specific fields
                final String payoutRatios = properties.getProperty(competitionPrefix + ".PayoutRatios");
                final StringTokenizer tokenizer = new StringTokenizer(payoutRatios, ":");
                final int payoutCount = tokenizer.countTokens();
                final double[] payouts = new double[payoutCount];
                for (int j = 0; j < payoutCount; ++j) {
                    payouts[j] = Double.parseDouble(tokenizer.nextToken());
                }
                result.add(new Nomination(name, cost, clubTake, availableInOpen, payouts));
            } else {
                throw new IOException("Unknown competiion type: '" + type + "'");
            }
        }
        return Collections.unmodifiableList(result);
    }

    public Mode getMode()
    {
        return this.mode;
    }

    public List<Competition> getCompetitions()
    {
        return this.competitions;
    }

    public String getResultsFooter()
    {
        return resultsFooter;
    }
}
