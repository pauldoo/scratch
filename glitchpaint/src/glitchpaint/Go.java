package glitchpaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Go {
    private final int width;
    private final int height;
    private final Random rng = new Random();

    private BufferedImage image;
    private ImageComponent imageComponent;
    private final int brushRadius = 8;
    private final int scaleFactor = 2;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Go(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public static void main(final String[] args) {
        new Go(400, 400).run();
    }

    private void run() {
        createDefaultImage();

        this.imageComponent = new ImageComponent();
        final JFrame imageFrame = new JFrame();
        imageFrame.getContentPane().add(imageComponent);
        imageFrame.pack();
        imageFrame.setResizable(false);
        imageFrame.setVisible(true);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        imageComponent.addMouseMotionListener(new Listener());

        executorService.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(this::doGlitch);
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void doGlitch() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            saveJpeg(output);

            while (true) {
                try {
                    final byte[] buffer = output.toByteArray();
                    if (shouldBitFlip()) {
                        tweakRandomBit(buffer);
                    }
                    final BufferedImage read = ImageIO.read(new ByteArrayInputStream(buffer));
                    if (read != null && read.getWidth() == width && read.getHeight() == height) {
                        image.flush();
                        image = read;
                        break;
                    }
                } catch (final Exception e) {

                }
            }

            imageComponent.repaint();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveJpeg(final ByteArrayOutputStream output) throws IOException {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        writer.setOutput(new MemoryCacheImageOutputStream(output));
        final JPEGImageWriteParam params = new JPEGImageWriteParam(null);
        params.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(randomJpegQuality());
        if (jpegShoudBeProgressive()) {
            params.setProgressiveMode(JPEGImageWriteParam.MODE_DEFAULT);
        }
        writer.write(null, new IIOImage(image, null, null), params);
        writer.dispose();
    }

    private boolean jpegShoudBeProgressive() {
        return rng.nextBoolean();
    }

    private boolean shouldBitFlip() {
        return rng.nextInt(4) == 0;
    }

    private float randomJpegQuality() {
        return rng.nextFloat() * 0.5f + 0.5f;
    }

    private void tweakRandomBit(final byte[] buffer) {
        final int bitToTweak = rng.nextInt(buffer.length * 8);
        byte v = buffer[bitToTweak / 8];
        final byte mask = (byte) (1 << (bitToTweak % 8));
        v = (byte) (v ^ mask);
        buffer[bitToTweak / 8] = v;
    }

    private void createDefaultImage() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private class ImageComponent extends JComponent {

        private static final long serialVersionUID = 1L;

        public ImageComponent() {
            setPreferredSize(new Dimension(width * scaleFactor, height * scaleFactor));
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(image, 0, 0, width * scaleFactor, height * scaleFactor, null);
        }
    }

    private class Listener extends MouseMotionAdapter {

        @Override
        public void mouseDragged(final MouseEvent e) {
            Graphics g = null;
            try {
                g = image.getGraphics();
                g.setColor(new Color(rng.nextFloat(), rng.nextFloat(), rng.nextFloat()));
                g.fillOval( //
                        e.getX() / scaleFactor - brushRadius, //
                        e.getY() / scaleFactor - brushRadius, //
                        brushRadius * 2, //
                        brushRadius * 2);
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
            imageComponent.repaint( //
                    e.getX() - brushRadius * scaleFactor, //
                    e.getY() - brushRadius * scaleFactor, //
                    brushRadius * 2 * scaleFactor, //
                    brushRadius * 2 * scaleFactor);
        }
    }

}
