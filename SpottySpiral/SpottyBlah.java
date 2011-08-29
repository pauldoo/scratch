import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import java.io.*;

final class Circle
{
    public final double x;
    public final double y;
    public final double d;

    public Circle(double x, double y, double d) {
        this.x = x;
        this.y = y;
        this.d = d;
    }

    public String toString()
    {
        return "[" + x + ", " + y + ", " + d + "]";
    }
}

final class Constants
{
    public static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5.0)) / 2.0;
    public static final double GOLDEN_ANGLE = 2 * Math.PI / (GOLDEN_RATIO * GOLDEN_RATIO);
}

public final class SpottyBlah
{
    private static double frac(double v)
    {
        return v - Math.floor(v);
    }

    private static double square(double v)
    {
        return v*v;
    }

    public static void main(String[] args) throws IOException
    {
        final Collection<Circle> spots = new ArrayList<Circle>();
        final BufferedImage image = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int i = 0; true; i++) {
            final double x = frac(i * Constants.GOLDEN_RATIO) * 2000.0;
            //final double angle = (Constants.GOLDEN_ANGLE - 0.001) * i;
            //final double angle = Math.E * i;

            final double d = frac(i * Constants.GOLDEN_RATIO) * 20.0;
            double height = 0.0;
            for (Circle c: spots) {

                final double lineDistanceToSpotCenter = Math.abs(x - c.x);
                final double correction = Math.sqrt(square((c.d + d)/2.0) - lineDistanceToSpotCenter * lineDistanceToSpotCenter);
                if (correction > 0.0) {
                    final double newHeight = c.y + correction;

                    if (newHeight > height) {
                        height = newHeight;
                    }
                }
            }

            for (int offset = -1; offset <= 1; offset++) {
                Circle c = new Circle(x + (offset * 2000), height, d);
                spots.add(c);
                Shape circle = new Ellipse2D.Double(c.x - c.d/2.0, c.y - c.d/2.0, c.d, c.d);
                graphics.fill(circle);
            }
            //System.out.println(c);
            if (height > 3000) {
                break;
            }
        }
        graphics.dispose();
        ImageIO.write(image, "png", new File("image.png"));
    }
}
