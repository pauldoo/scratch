import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by pauldoo on 14/09/2017.
 */
public class Go {

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 2; i++) {
            go(10_000_000);
        }
    }

    private static void go(int testLengthInBytes) throws IOException {
        byte[] uncompressedData = generateData(testLengthInBytes);
        System.out.println(String.format("%d bytes of test data created", uncompressedData.length));

        File tmpFile = File.createTempFile("gzipbuffertest", null);
        tmpFile.deleteOnExit();

        List<ThrowingWrapper<OutputStream>> outputWrappers = Arrays.<ThrowingWrapper<OutputStream>>asList(
                new NamedWrapper<>("none", x -> x),
                new NamedWrapper<>("default", x -> new BufferedOutputStream(x)));

        compressTests(uncompressedData, tmpFile, outputWrappers);

        List<ThrowingWrapper<InputStream>> inputWrappers = Arrays.<ThrowingWrapper<InputStream>>asList(
                new NamedWrapper<>("none", x -> x),
                new NamedWrapper<>("default", x -> new BufferedInputStream(x)));

        decompressTests(tmpFile, inputWrappers);

    }

    private static void compressTests(final byte[] uncompressedData, File tmpFile, List<ThrowingWrapper<OutputStream>> wrappers) throws IOException {
        for (ThrowingWrapper<OutputStream> before : wrappers) {
            for (ThrowingWrapper<OutputStream> after : wrappers) {
                //System.out.println(String.format("Compress:\t%s\t%s", before.toString(), after.toString()));
                long start = System.currentTimeMillis();
                OutputStream out = before.wrap(new GZIPOutputStream(after.wrap(new FileOutputStream(tmpFile))));
                ByteArrayInputStream in = new ByteArrayInputStream(uncompressedData);

                int v;
                while ((v = in.read()) != -1) {
                    out.write(v);
                }

                out.close();
                long end = System.currentTimeMillis();

                System.out.println(String.format("Compress:\t%s\t%s\t%fs", before.toString(), after.toString(), (end - start) / 1000.0));
            }
        }
    }

    private static void decompressTests(File tmpFile, List<ThrowingWrapper<InputStream>> wrappers) throws IOException {
        for (ThrowingWrapper<InputStream> before : wrappers) {
            for (ThrowingWrapper<InputStream> after : wrappers) {
                //System.out.println(String.format("Decompress:\t%s, %s", before.toString(), after.toString()));
                long start = System.currentTimeMillis();
                InputStream in = before.wrap(new GZIPInputStream(after.wrap(new FileInputStream(tmpFile))));

                int v;
                while ((v = in.read()) != -1) {
                }

                in.close();
                long end = System.currentTimeMillis();

                System.out.println(String.format("Decompress:\t%s\t%s\t%fs", before.toString(), after.toString(), (end - start) / 1000.0));
            }
        }
    }

    private static byte[] generateData(int lengthInBytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutput dos = new DataOutputStream(bos);
        Random rng = new Random();

        while (bos.size() < lengthInBytes) {
            dos.writeInt(rng.nextInt(1000));
        }

        return bos.toByteArray();
    }
}

class NamedWrapper<T> implements ThrowingWrapper<T> {

    private final String name;
    private final ThrowingWrapper<T> inner;

    public NamedWrapper(String name, ThrowingWrapper<T> inner) {
        this.name = name;
        this.inner = inner;
    }

    public String toString() {
        return name;
    }

    public T wrap(T x) throws IOException {
        return inner.wrap(x);
    }
}

@FunctionalInterface
interface ThrowingWrapper<T> {
    T wrap(T x) throws IOException;
}

