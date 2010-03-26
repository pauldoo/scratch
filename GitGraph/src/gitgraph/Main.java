/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gitgraph;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        Process process = null;
        try {
            String command[] = new String[]{"git",  "rev-list",  "--objects",  "--all"};
            process = Runtime.getRuntime().exec(command);
            process.getOutputStream().close();
            process.getErrorStream().close();
            BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));

            Pattern pattern = Pattern.compile("^([0123456789abcdef]{40}).*$");

            String line;
            while ((line = r.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    System.out.println("Object: " + m.group(1));
                } else {
                    System.err.println("Warning: I didn't understand '" + line + "'");
                }
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
}
