/*
    Creates a really cheesy recursive facebook profile picture.

    Completely hardcoded transforms etc.  Total hack.



    Usage:
    javac Blah.java && java -Xmx1024m -server Blah && open output.png
*/


import javax.imageio.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;


public final class Blah {

    private static int recur(BufferedImage image, Point2D.Double p, double width, double height, double[] t, double blur, double dimness, int depth)
    {
        Point2D.Double q = new Point2D.Double(
            p.x * t[0] + p.y * t[1] + t[2] + (Math.random() - 0.5) * blur,
            p.x * t[3] + p.y * t[4] + t[5] + (Math.random() - 0.5) * blur);

        if (q.x >= 0.0 && q.x <= width &&
            q.y >= 0.0 && q.y <= height &&
            q.equals(p) == false &&
            (true || Math.random() > 0.5)) {
            return recur(image, q, width, height, t, blur, dimness, depth+1);
        } else {
            int color = image.getRGB((int)Math.round(p.x), (int)Math.round(p.y));
            double d = Math.pow(dimness, depth);
            color =
                ((int)Math.round(((color >> 16) & 0xff) * d)) << 16 |
                ((int)Math.round(((color >> 8) & 0xff) * d)) << 8 |
                ((int)Math.round(((color >> 0) & 0xff) * d)) << 0;
            return color;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new File("input.jpg"));

        final double scale = 3.8;
        final double angle = -0.08;
        final double[] t = new double[] {
            Math.cos(angle) * scale, Math.sin(angle) * scale, (-1315) * scale,
            -Math.sin(angle) * scale, Math.cos(angle) * scale, (-1290) * scale
        };
        final double blur = 60;
        final double dimness = 0.75;

        BufferedImage result = new BufferedImage(
            image.getWidth() * 4,
            image.getHeight() * 4,
            BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < result.getHeight(); y++) {
            System.err.print("\r" + ((y * 100) / (result.getHeight()-1)) +" % done");
            for (int x = 0; x < result.getWidth(); x++) {
                Point2D.Double point = new Point2D.Double(
                    (x * image.getWidth()) / result.getWidth(),
                    (y * image.getHeight()) / result.getHeight());
                int color = recur(image, point, image.getWidth()-1, image.getHeight()-1, t, blur, dimness, 0);

                result.setRGB(x, y, color);
            }
        }

        ImageIO.write(result, "png", new File("output.png"));
    }


}

