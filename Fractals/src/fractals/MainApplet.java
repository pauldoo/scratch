/*
    Copyright (c) 2007, 2008, 2009, 2010, 2012 Paul Richards <paul.richards@gmail.com>

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

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class MainApplet extends JApplet
{
    private static final long serialVersionUID = 1011945883208164505L;

    @Override
    public void init()
    {
        super.start();
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("FractalType", this.getParameter("FractalType"));

            this.getContentPane().add(createMainComponent(parameters));
        } catch (Exception e) {
            StringWriter message = new StringWriter();
            e.printStackTrace(new PrintWriter(message));
            JOptionPane.showInternalMessageDialog(this.getContentPane(), message.toString(), e.toString(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void destroy()
    {
        try {
            Utilities.getHeavyThreadPool().shutdownNow();
            Utilities.getLightThreadPool().shutdownNow();
        } finally {
            super.destroy();
        }
    }


    private static JComponent createMainComponent(Map<String, String> parameters)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        String fractalType = parameters.get("FractalType");
        if (fractalType.equals("MandelbrotSet")) {
            panel.add(MandelbrotSet.createView(), BorderLayout.CENTER);
        } else if (fractalType.equals("JuliaSet")) {
            panel.add(BackwardsIterationJuliaView.createView(), BorderLayout.CENTER);
        } else if (fractalType.equals("IteratedFunctionSystem")) {
            panel.add(IteratedFunctionSystem.createView(), BorderLayout.CENTER);
        } else if (fractalType.equals("DiffusionLimitedAggregation")) {
            panel.add(DiffusionLimitedAggregation.createView(), BorderLayout.CENTER);
        } else if (fractalType.equals("Mandelbrot4D")) {
            panel.add(MandelbrotSet.createMandelbrot4dView(), BorderLayout.CENTER);
        } else if (fractalType.equals("NewtonRaphson")) {
            panel.add(NewtonRaphson.createView(), BorderLayout.CENTER);
        } else if (fractalType.equals("BifurcationDiagram")) {
            panel.add(BifurcationDiagram.createComponent(), BorderLayout.CENTER);
        } else if (fractalType.equals("Mandelbulb")) {
            panel.add(Mandelbulb.createView(), BorderLayout.CENTER);
        } else {
            throw new IllegalArgumentException("Unknown fractal type: " + fractalType);
        }

        return panel;
    }

    public static void main(String[] args)
    {
        Map<String, String> parameters = new HashMap<String, String>();
        //parameters.put("FractalType", "MandelbrotSet");
        //parameters.put("FractalType", "JuliaSet");
        //parameters.put("FractalType", "IteratedFunctionSystem");
        //parameters.put("FractalType", "DiffusionLimitedAggregation");
        //parameters.put("FractalType", "Mandelbrot4D");
        //parameters.put("FractalType", "NewtonRaphson");
        //parameters.put("FractalType", "BifurcationDiagram");
        parameters.put("FractalType", "Mandelbulb");

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createMainComponent(parameters));
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
