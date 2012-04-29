/*
    Copyright (c) 2005, 2006, 2007, 2011, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import pigeon.competitions.Competition;
import pigeon.model.Clock;
import pigeon.model.Member;
import pigeon.model.Organization;
import pigeon.model.Race;
import pigeon.model.Season;
import pigeon.model.Time;

/**
    Public static methods that don't have a natural home in any
    of the view classes.
*/
public final class Utilities {

    // Non-Creatable
    private Utilities()
    {
    }

    /**
        The start range for year drop down combo boxes.
    */
    public static final int YEAR_DISPLAY_START = 2005;

    /**
        The end range for year drop down combo boxes.
    */
    public static final int YEAR_DISPLAY_END = YEAR_DISPLAY_START + 10;

    /**
        DateFormat for formatting times that span just a single day.

        Their 'long' representation spans only from 0 to 24 * 60 * 60 * 1000.
        The resulting string will be in 24hr time (hopefully).
    */
    public static final DateFormat TIME_FORMAT_WITHOUT_LOCALE = DateTimeDisplayMode.HOURS_MINUTES_SECONDS.getFormat();

    /**
        DateFormat for formatting times that occur on a real calendar.

        Their 'long' representation is not confined to spanning just a single day.
        The local time zone is taken into account.
    */
    public static final DateFormat TIME_FORMAT_WITH_LOCALE = new SimpleDateFormat("HH:mm:ss");

    /**
        DateFormat for formatting dates that occur on a real calendar.

        Their 'long' representation is not confined to spanning just a single day.
        The local time zone is taken into account.
    */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

    /**
        Given an Organization, return a list of all the club names mentioned in member profiles.
    */
    public static Collection<String> findClubNames(Organization organization)
    {
        SortedSet<String> result = new TreeSet<String>();
        for (Member m: organization.getMembers()) {
            if (m.getClub() != null && !m.getClub().equals("")) {
                result.add(m.getClub());
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
        Given an Organization, return a list of all the section names mentioned in member profiles.
    */
    public static Collection<String> findSectionNames(Organization organization)
    {
        SortedSet<String> result = new TreeSet<String>();
        for (Member m: organization.getMembers()) {
            if (m.getSection() != null && !m.getSection().equals("")) {
                result.add(m.getSection());
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
        Checks all the times entered in a season and returns all the competitions
        mentioned in the pigeon time entries.

        Used when loading a season to verify that all of the competitions used when
        saving the season are still present in the configuration file.
    */
    private static Collection<String>  getCompetitionNames(Season season)
    {
        // Use a Set here to ensure that duplicates are removed.
        Set<String> result = new TreeSet<String>();
        for (Race r: season.getRaces()) {
            for (Clock c: r.getClocks()) {
                for (Time t: c.getTimes()) {
                    result.addAll(t.getOpenCompetitionsEntered());
                    result.addAll(t.getSectionCompetitionsEntered());
                }
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
        Checks all the birds in a season and returns all of the colours mentioned.
    */
    public static Collection<String>  getBirdColors(Season season)
    {
        Set<String> result = new TreeSet<String>();
        for (Race r: season.getRaces()) {
            for (Clock c: r.getClocks()) {
                for (Time t: c.getTimes()) {
                    final String color = t.getColor();
                    if (color.length() != 0) {
                        result.add(color);
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
        Checks through previous races looking for a bird with the given ring number.

        This is used to "guess" the sex and colour of a bird when the same ring number
        is entered in a later race.
    */
    public static Time findBirdEntry(final Season season, final String ringNumber)
    {
        if (ringNumber.length() != 0) {
            for (Race r: season.getRaces()) {
                for (Clock c: r.getClocks()) {
                    for (Time t: c.getTimes()) {
                        if (ringNumber.equals(t.getRingNumber())) {
                            return t;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
        Returns a list of all the competition names in a configuration.
    */
    private static List<String>  getCompetitionNames(List<Competition> competitions)
    {
        // Don't expect duplicate names in the configuration, so we can use
        // any kind of collection.
        List<String> result = new ArrayList<String>();
        for (Competition c: competitions) {
            result.add(c.getName());
        }
        return Collections.unmodifiableList(result);
    }

    /**
        Verifies that a season is valid with respect to the current
        application version and configuration.

        Checks (for example) that the competition names mentioned
        in the loaded file are still present in the application configuration
        file.
    */
    public static void validateSeason(Component parent, Season season, Configuration configuration) throws UserCancelledException
    {
        Collection<String> competitionsMentionedInFile = getCompetitionNames(season);
        Collection<String> competitionsConfigured = getCompetitionNames(configuration.getCompetitions());
        if (!competitionsConfigured.containsAll(competitionsMentionedInFile)) {
            String message =
                "This season's pools do not match the current pool list.\n" +
                "Some of the pool entry information may not load correctly.\n" +
                "\n" +
                "Continue anyway?";
            int result = JOptionPane.showConfirmDialog(parent, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                throw new UserCancelledException();
            }
        }
    }

    public static JFileChooser createFileChooser()
    {
        JFileChooser chooser = new JFileChooser();
        FileFilter filter = SimpleFileFilter.createSeasonFileFilter();
        chooser.setFileFilter(filter);
        File mostRecentFile = getMostRecentFile();
        if (mostRecentFile != null) {
            chooser.setSelectedFile(mostRecentFile);
        }
        return chooser;
    }

    private static Properties loadUserSettings()
    {
        String filename = System.getProperty("user.home") + File.separator + ".RacePoint.xml";
        Properties result = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            BufferedInputStream inBuf = new BufferedInputStream(in);
            result.loadFromXML(inBuf);
            inBuf.close();
        } catch (IOException e) {
            result = new Properties();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    private static void saveUserSettings(Properties properties)
    {
        String filename = System.getProperty("user.home") + File.separator + ".RacePoint.xml";
        OutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            BufferedOutputStream outBuf = new BufferedOutputStream(out);
            properties.storeToXML(outBuf, "User settings for RacePoint");
            outBuf.close();
        } catch (IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String loadProperty(String key)
    {
        Properties properties = loadUserSettings();
        return properties.getProperty(key);
    }

    private static void saveProperty(String key, String value)
    {
        Properties properties = loadUserSettings();
        properties.setProperty(key, value);
        saveUserSettings(properties);
    }

    public static File getMostRecentFile()
    {
        String mostRecentFilename = loadProperty("MostRecentFile");
        if (mostRecentFilename != null) {
            return new File(mostRecentFilename);
        } else {
            return null;
        }
    }

    public static void setMostRecentFile(File file)
    {
        saveProperty("MostRecentFile", file.getPath());
    }

    public static Season loadSeasonFromFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        InputStream fileIn = null;
        try {
            fileIn = new FileInputStream(file);
            ObjectInput objectIn = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(fileIn)));
            Season loaded = (Season)objectIn.readObject();
            objectIn.close();
            setMostRecentFile(file);
            return loaded;
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }

    public static void writeSeasonToFile(Season season, File file) throws FileNotFoundException, IOException
    {
        OutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);
            ObjectOutput objectOut = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(fileOut)));
            objectOut.writeObject(season);
            objectOut.close();
            setMostRecentFile(file);
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }

    public static NumberFormat currencyFormat()
    {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format;
    }

    public interface Func1<O, I> {
        public O call(I arg);
    }

    public static <I, O> List<O> map(List<I> coll, Func1<O, I> func) {
        List<O> result = new ArrayList<O>();
        for (I v: coll) {
            result.add(func.call(v));
        }
        return Collections.unmodifiableList(result);
    }

    public static <T extends Comparable<? super T>> T median00(Collection<T> coll) {
        if (coll.isEmpty()) {
            return null;
        } else {
            List<T> copy = new ArrayList<T>(coll);
            Collections.sort(copy);
            int index = (copy.size()-1) / 2;
            return copy.get(index);
        }
    }
}
