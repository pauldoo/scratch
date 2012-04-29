/*
    Copyright (c) 2008, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

import fractals.math.Complex;
import javax.swing.JComponent;

/**
    Hardcoded to render the newton raphson fractal for z^4 + z^3 - 1 = 0.
*/
final class NewtonRaphson implements TileProvider<IntegerTile>
{
    /**
        These have been precomputed using maxima:

        (%i14) float(solve(z^4 + z^3 - 1));<br/>
        (%o14) [z = - 1.380277569097613, z = .8191725133961627,<br/>
        z = - .9144736629677245 %i - 0.219447472149275,<br/>
        z = .9144736629677245 %i - 0.219447472149275]<br/>
    */
    private static final Complex[] roots = new Complex[]{
        new Complex(- 1.380277569097613, 0.0),
        new Complex(.8191725133961627, 0.0),
        new Complex(- 0.219447472149275, - .9144736629677245),
        new Complex(0.219447472149275, .9144736629677245)
    };

    private static final Complex minusOne = new Complex(-1.0, 0.0);
    private static final Complex two = new Complex(2.0, 0.0);
    private static final Complex three = new Complex(3.0, 0.0);
    private static final Complex four = new Complex(4.0, 0.0);

    final int maxIterations;

    NewtonRaphson(int maxIterations)
    {
        this.maxIterations = maxIterations;
    }

    static JComponent createView()
    {
        TileProvider<RenderableTile> source = new ColorAndExposeFilter(new NewtonRaphson(50), roots.length, 0.08);
        CanvasView view = new CanvasView(800, 600, source);
        view.startAllThreads();
        return view;
    }

    private static int iterateUntilFinished(final double x, final double y, final int maxIterations)
    {
        Complex z = new Complex(x, y);
        int i;
        for (i = 0; i < maxIterations; i++) {
            for (int j = 0; j < roots.length; j++) {
                final Complex root = roots[j];
                if (root.subtract(z).magnitude() <= 1e-6) {
                    return i * roots.length + j;
                }
            }
            Complex step = calculateStep(z);
            if (step.magnitude() < 1e-6) {
                break;
            }
            Complex.subtractReplace(z, step);
        }
        return 0;
    }

    private static Complex calculateStep(final Complex x)
    {
        // (z^4 + z^3 - 1) / (4x^3 + 3x^2)
        Complex numerator = minusOne.clone();

        Complex temp = x.multiply(x);
        Complex denom = temp.multiply(three);

        Complex.multiplyReplace(temp, x);
        Complex.addReplace(numerator, temp);
        Complex.addReplace(denom, temp.multiply(four));

        Complex.multiplyReplace(temp, x);
        Complex.addReplace(numerator, temp);

        Complex.divideReplace(numerator, denom);

        return numerator;
    }


    public IntegerTile getTile(TilePosition pos)
    {
        IntegerTile tile = new IntegerTile(pos);
        for (int iy = pos.getMinY(); iy <= pos.getMaxY(); iy++) {
            for (int ix = pos.getMinX(); ix <= pos.getMaxX(); ix++) {
                final double r = ix * pos.relativeScale() / 200 - 2.5;
                final double i = iy * pos.relativeScale() / 200 - 1.5;
                int v = iterateUntilFinished(r, i, maxIterations);
                tile.setValue(ix, iy, v);
            }
        }
        return tile;
    }
}
