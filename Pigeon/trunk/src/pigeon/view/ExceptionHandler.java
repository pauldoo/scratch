/*
    Copyright (c) 2005, 2006, 2007, 2012 Paul Richards <paul.richards@gmail.com>

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

package pigeon.view;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import pigeon.About;

/**
 * Handles uncaught exceptions and presents a "friendly" crash dialog.
 */
final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    static public void register() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
    }

    public ExceptionHandler() {
    }

    public void handle(Throwable e)
    {
        uncaughtException(null, e);
    }

    public void uncaughtException(Thread t, Throwable e) {
        StringWriter message = new StringWriter();
        PrintWriter writer = new PrintWriter(message);
        writer.println(About.TITLE);
        writer.println();
        e.printStackTrace(writer);
        writer.flush();

        JTextComponent message_text = new JTextArea(message.toString(), 10, 50);
        message_text.setEditable(false);
        JScrollPane message_pane = new JScrollPane(message_text);

        Object[] contents = new Object[]{
            new JLabel("An unexpected error has occured.  Please copy & paste the following and send to Paul <paul.richards@gmail.com>."),
            message_pane,
            new JLabel("The application may be unstable until you restart.")
        };

        JOptionPane.showMessageDialog(null, contents, "Oops!", JOptionPane.ERROR_MESSAGE);
    }
}
