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

package pigeon.model;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
    Exception thrown when model data is inconsistent.
*/
public final class ValidationException extends Exception {

    private static final long serialVersionUID = 1525226081739583319L;

    private final String message;
    private final Throwable cause;

    public ValidationException(String message) {
        this.message = message;
        this.cause = null;
    }

    public ValidationException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return message;
    }

    public void displayErrorDialog(Component parent) {
        JOptionPane.showMessageDialog(parent, message, "Invalid information", JOptionPane.WARNING_MESSAGE);
    }
}
