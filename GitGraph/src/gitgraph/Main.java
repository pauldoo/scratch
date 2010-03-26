/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gitgraph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author pauldoo
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String outputFilename = args[0];
        Process process = null;
        try {
            {
                String command[] = new String[]{"git",  "rev-list",  "--objects",  "--all"};
                process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();
                process.getErrorStream().close();
            }
            Collection<String> objectHashes = new HashSet<String>();
            {
                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
                Pattern pattern = Pattern.compile("^([0123456789abcdef]{40}).*$");
                {
                    String line;
                    while ((line = r.readLine()) != null) {
                        Matcher m = pattern.matcher(line);
                        if (m.matches()) {
                            String objectHash = m.group(1);
                            objectHashes.add(objectHash);
                        } else {
                            System.err.println("Warning: I didn't understand '" + line + "'");
                        }
                    }
                }
            }
            process.waitFor();
            process = null;

            {
                PrintWriter dotOutput = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFilename)));
                dotOutput.println("strict digraph {");
                for (String hash: objectHashes) {
                    dotOutput.println("  \"" + hash + "\";");
                }
                dotOutput.println("}");
                dotOutput.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
