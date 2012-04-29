/*
    Copyright (c) 2008, 2009, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

package fractals;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;

/**
    Abstract base class for components that want to do rendering in the
    background and not in their main paint method.
    <p>
    Useful for components that can't do a decent quality rendering at
    interactive speed or want to incrementally add more to their canvas
    without having to worry about the threading.
*/
abstract class BackgroundRenderingComponent extends JComponent
{
    /**
        Supersample by this amount in both x and y.
    */
    private final int supersamplingFactor;

    /**
        The current buffer that "paint()" will use.  Is being concurrently
        written to by the background rendering thread.
    */
    private BufferedImage buffer = null;

    /**
        Future object kept in order to cancel the background thread.
    */
    private Future renderer = null;

    /**
        Object used as an event for the bufferIsNowOkayToBlit method.

        @see #bufferIsNowOkayToBlit()
    */
    private Object bufferFirstBlitEventObject = null;

    /**
        Future object kept in order to cancel the background thread
        that calls repaint in order to cause the buffer to be reblitted.
    */
    private Future repainter = null;

    protected BackgroundRenderingComponent(int supersamplingFactor)
    {
        this.supersamplingFactor = supersamplingFactor;
    }

    /**
        Copies the buffered image to the screen, and does so quickly.
    */
    @Override
    public final void paintComponent(Graphics g)
    {
        paintComponent((Graphics2D)g);
    }

    private final void paintComponent(Graphics2D g)
    {
        Utilities.setGraphicsToHighQuality(g);
        final boolean repaintAgainLater = isRendering();
        if (bufferFirstBlitEventObject != null) {
            try {
                synchronized (bufferFirstBlitEventObject) {
                    bufferFirstBlitEventObject.wait(200);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } finally {
                bufferFirstBlitEventObject = null;
            }
        }
        if (buffer != null) {
            AffineTransform transform = AffineTransform.getScaleInstance(1.0 / supersamplingFactor, 1.0 / supersamplingFactor);
            g.drawRenderedImage(buffer, transform);
        }
        if (buffer == null || buffer.getWidth() != getSupersampledWidth() || buffer.getHeight() != getSupersampledHeight()) {
            rerender();
        }
        if (repaintAgainLater) {
            if (repainter != null) {
                repainter.cancel(false);
                repainter = null;
            }
            Runnable runnable = new Runnable(){
                public void run()
                {
                    repaint();
                }
            };
            repainter = Utilities.getLightThreadPool().schedule(runnable, 250, TimeUnit.MILLISECONDS);
        }
    }

    /**
        Implementors of this class are expected to render progressively
        higher quality images to the graphics context.  Periodically the image
        which backs the Graphics2D is painted to the screen to make the
        incremental rendering visible to the user.
        <p>
        The implementor is expected to check Thread.interrupted() fairly regularly.
        <p>
        \see bufferIsNowOkayToBlit
    */
    protected abstract void render(Graphics2D g) throws InterruptedException;

    /**
        Restarts the background rendering thread with a new buffer.
    */
    public final void rerender()
    {
        stopBackgroundThread();
        Runnable runner = new Runnable() {
            @Override
            public void run()
            {
                Graphics2D g = null;
                try {
                    if (getWidth() == 0 || getHeight() == 0) {
                        return;
                    }
                    bufferFirstBlitEventObject = new Object();
                    buffer = new BufferedImage(getSupersampledWidth(), getSupersampledHeight(), BufferedImage.TYPE_INT_ARGB);
                    if (Thread.interrupted()) throw new InterruptedException();
                    g = (Graphics2D)buffer.getGraphics();
                    render(g);
                    bufferIsNowOkayToBlit();
                    repaint();
                } catch (InterruptedException ex) {
                } catch (Throwable ex) {
                    ex.printStackTrace();
                } finally {
                    if (g != null) {
                        g.dispose();
                    }
                }
            }
        };
        if (renderer != null) {
            throw new IllegalStateException("There should be no background thread");
        }
        renderer = Utilities.getHeavyThreadPool().submit(runner);
        repaint();
    }

    public final void stopBackgroundThread()
    {
        if (renderer != null) {
            renderer.cancel(true);
            renderer = null;
        }
    }

    /**
        Returns true iff there is a background thread still running.
    */
    private final boolean isRendering()
    {
        return renderer != null && renderer.isDone() == false;
    }

    /**
        To avoid certain types of flicker we don't blit the buffer within
        "paintComponent()" until after the background thread has
        signalled that it is now happy for the contents to be made visible.
        <p>
        Usually the background rendering thread will allow this as soon
        as it has filled the canvas with the background colour and rendered
        anything that is quick to do.
        <p>
        Called by the derrived class.
        <p>
        This is also just a hint, after a fixed time period this class will blit the
        buffer regardless of whether the derrived class is ready for it or not.
    */
    protected final void bufferIsNowOkayToBlit()
    {
        if (bufferFirstBlitEventObject != null) {
            synchronized (bufferFirstBlitEventObject) {
                bufferFirstBlitEventObject.notifyAll();
            }
        }
    }

    /**
        Width of the offscreen buffer to which the render method is drawing.
        May be larger than the width of the component on screen due to
        supersampling.
    */
    protected final int getSupersampledWidth()
    {
        return getWidth() * supersamplingFactor;
    }

    /**
        Height of the offscreen buffer to which the render method is drawing.
        May be larger than the height of the component on screen due to
        supersampling.
    */
    protected final int getSupersampledHeight()
    {
        return getHeight() * supersamplingFactor;
    }
}
