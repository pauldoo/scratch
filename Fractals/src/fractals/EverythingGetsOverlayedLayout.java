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

package fractals;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
    A layout manager that simply forces all components to the same bounds
    and to completely fill the parent container.
*/
final class EverythingGetsOverlayedLayout implements LayoutManager
{

    public void addLayoutComponent(String name, Component comp)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLayoutComponent(Component comp)
    {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Dimension preferredLayoutSize(Container parent)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Dimension minimumLayoutSize(Container parent)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void layoutContainer(Container parent)
    {
        Insets insets = parent.getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = parent.getWidth() - insets.left - insets.right;
        int height = parent.getHeight() - insets.top - insets.bottom;
        for (Component c: parent.getComponents()) {
            c.setBounds(x, y, width, height);
        }
    }
}