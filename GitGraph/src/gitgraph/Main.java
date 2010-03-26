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
import java.util.TreeSet;
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
            Collection<String> objectHashes = new TreeSet<String>();
            {
                String command[] = new String[]{"git",  "rev-list",  "--objects",  "--all"};
                process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();
                process.getErrorStream().close();

                BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
                Pattern pattern = Pattern.compile("^([0123456789abcdef]{40}).*$");
                {
                    String line;
                    while ((line = r.readLine()) != null) {
                        Matcher m = pattern.matcher(line);
                        if (m.matches()) {
                            String fullHash = m.group(1);
                            objectHashes.add(fullHash);
                        } else {
                            System.err.println("Warning: I didn't understand '" + line + "'");
                        }
                    }
                }
                process.waitFor();
                process = null;
            }

            {
                PrintWriter dotOutput = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFilename)));
                dotOutput.println("strict digraph {");

                for (String hash: objectHashes) {
                    String command[] = new String[]{"git", "cat-file", "-t", hash};
                    process = Runtime.getRuntime().exec(command);
                    process.getOutputStream().close();
                    process.getErrorStream().close();

                    BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
                    String type = r.readLine();
                    String shapeName = null;
                    if ("commit".equals(type)) {
                        shapeName = "circle";
                    } else if ("tree".equals(type)) {
                        shapeName = "triangle";
                    } else if ("blob".equals(type)) {
                        shapeName = "rectangle";
                    }
                    process.waitFor();
                    process = null;

                    dotOutput.println("  \"" + hash + "\" [label=\"" + hash.substring(0, 7) + "\",shape=" + shapeName + "];");
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
