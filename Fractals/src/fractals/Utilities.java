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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public final class Utilities
{
    private static final ScheduledExecutorService lightThreadPool =
            new ScheduledThreadPoolExecutor(
                2,
                new MyThreadFactory(Thread.NORM_PRIORITY));
    private static final ScheduledExecutorService heavyThreadPool =
            new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() + 1,
                new MyThreadFactory(Thread.MIN_PRIORITY));

    private static final class MyThreadFactory implements ThreadFactory
    {
        final int priority;

        MyThreadFactory(int priority)
        {
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread result = new Thread(r);
            result.setPriority(priority);
            result.setDaemon(true);
            return result;
        }
    }

    private Utilities()
    {
    }

    static void setGraphicsToHighQuality(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    static void setGraphicsToLowQuality(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    /**
        Simplistic exposure function that returns 1.0 - e^(-exposure * value).
    */
    static double expose(double value, double exposure)
    {
        return 1.0 - Math.exp(-exposure * value);
    }

    /**
        A thread pool intended for light background tasks (e.g. to triggering repaints).
    */
    static ScheduledExecutorService getLightThreadPool()
    {
        return lightThreadPool;
    }

    /**
        A thread pool intended for heavy tasks that benefit image quality
        in the short term (e.g. rendering, projecting).
    */
    static ScheduledExecutorService getHeavyThreadPool()
    {
        return heavyThreadPool;
    }

    /**
        Clamps the value 'x' to be in the range [min, max].
    */
    static int clamp(final int min, final int x, final int max)
    {
        if (max < min) {
            throw new IllegalArgumentException("Max is less than min");
        }
        return Math.max(min, Math.min(max, x));
    }

    /**
        Copies an array of doubles, returns the new copy.
    */
    static double[] copyDoubleArray(double[] source)
    {
        final double[] result = new double[source.length];
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    /**
        Asserts that the value is not NaN, then returns it.
    */
    public static double assertNotNaN(double x) throws NotANumberException
    {
        if (Double.isNaN(x)) {
            throw new NotANumberException();
        }
        return x;
    }
}
