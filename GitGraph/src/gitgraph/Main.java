/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gitgraph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author pauldoo
 */
public final class Main {
    private static final boolean includeBlobs = true;

    private static final Pattern objectInRevList = Pattern.compile("^([0123456789abcdef]{40})( (.*))?$");
    private static final Pattern parentInCommit = Pattern.compile("^parent ([0123456789abcdef]{40})$");
    private static final Pattern treeInCommit = Pattern.compile("^tree ([0123456789abcdef]{40})$");
    private static final Pattern treeInTree = Pattern.compile("^[0123456789]{6} tree ([0123456789abcdef]{40}).*$");
    private static final Pattern blobInTree = Pattern.compile("^[0123456789]{6} blob ([0123456789abcdef]{40}).*$");

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

            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
            PrintWriter dotOutput = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFilename)));
            dotOutput.println("strict digraph {");

            {
                String line;
                while ((line = r.readLine()) != null) {
                    Matcher m = objectInRevList.matcher(line);
                    if (m.matches()) {
                        String fullHash = m.group(1);
                        String hintName = (m.groupCount() >= 2) ? m.group(2) : null;
                        processObject(fullHash, hintName, dotOutput);
                    } else {
                        System.err.println("Warning: I didn't understand '" + line + "'");
                    }
                }
            }

            dotOutput.println("}");
            dotOutput.close();
            if (dotOutput.checkError()) {
                throw new IOException("Error writing output file.");
            }
            process.waitFor();
            process = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void processObject(String objectHash, String hintName, PrintWriter dotOutput) throws Exception
    {
        Process process = null;
        try {
            {
                String command[] = new String[]{"git", "cat-file", "-t", objectHash};
                process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();
                process.getErrorStream().close();
            }

            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
            String type = r.readLine();
            process.waitFor();
            process = null;
            if ("commit".equals(type)) {
                processCommit(objectHash, dotOutput);
            } else if ("tree".equals(type)) {
                processTree(objectHash, hintName, dotOutput);
            } else if (includeBlobs && "blob".equals(type)) {
                processBlob(objectHash, hintName, dotOutput);
            } else {
                System.err.println("Warning: Did not recognise object type '" + type + "'");
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void processCommit(String hash, PrintWriter dotOutput) throws Exception
    {
        Process process = null;
        try {
            {
                String command[] = new String[]{"git", "cat-file", "-p", hash};
                process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();
                process.getErrorStream().close();
            }

            BufferedReader commitReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
            String line;
            while ((line = commitReader.readLine()).equals("") == false) {
                Matcher parent = parentInCommit.matcher(line);
                if (parent.matches()) {
                    dotOutput.println("  \"" + hash + "\" -> \"" + parent.group(1) + "\";");
                }
                Matcher tree = treeInCommit.matcher(line);
                if (tree.matches()) {
                    dotOutput.println("  \"" + hash + "\" -> \"" + tree.group(1) + "\";");
                }
            }
            String firstLineOfCommitMessage = commitReader.readLine();
            if (firstLineOfCommitMessage == null) {
                firstLineOfCommitMessage = "";
            }
            dotOutput.println("  \"" + hash + "\" [label=\"" + hash.substring(0, 7) + "\\n" + firstLineOfCommitMessage + "\",shape=ellipse];");

            process.waitFor();
            process = null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void processTree(String hash, String hintName, PrintWriter dotOutput) throws Exception
    {
        dotOutput.println("  \"" + hash + "\" [label=\"" + hash.substring(0, 7) + "\\n" + hintName + "\",shape=triangle];");
        Process process = null;
        try {
            {
                String command[] = new String[]{"git", "cat-file", "-p", hash};
                process = Runtime.getRuntime().exec(command);
                process.getOutputStream().close();
                process.getErrorStream().close();
            }

            BufferedReader treeReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
            String line;
            while ((line = treeReader.readLine()) != null) {
                Matcher tree = treeInTree.matcher(line);
                if (tree.matches()) {
                    dotOutput.println("  \"" + hash + "\" -> \"" + tree.group(1) + "\";");
                }
                Matcher blob = blobInTree.matcher(line);
                if (includeBlobs && blob.matches()) {
                    dotOutput.println("  \"" + hash + "\" -> \"" + blob.group(1) + "\";");
                }
            }
            process.waitFor();
            process = null;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static void processBlob(String hash, String hintName, PrintWriter dotOutput)
    {
        dotOutput.println("  \"" + hash + "\" [label=\"" + hash.substring(0, 7) + "\\n" + hintName + "\",shape=rectangle];");
    }
}
