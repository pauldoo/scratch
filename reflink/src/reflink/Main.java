package reflink;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Main {

    private static AtomicLong saving = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
        Path dir =FileSystems.getDefault().getPath(args[0]);
        if (!Files.isDirectory(dir)) {
            throw new IOException(MessageFormat.format("Path isn't a directory: {0}", dir));
        }

        Files.walk(dir)
                .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                .collect(Collectors.groupingBy(Main::weakHash))
                .values()
                .parallelStream()
                .filter(Main::hasTwoOrMore)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Main::strongHash))
                .values()
                .parallelStream()
                .filter(Main::hasTwoOrMore)
                .forEach(Main::dedupe);

        System.out.println(MessageFormat.format("Total saving: {0} bytes", saving.get()));

    }

    private static boolean hasTwoOrMore(List<Path> paths) {
        return paths.size() >= 2;
    }

    private static long weakHash(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String strongHash(Path p) {
            try {
                MessageDigest instance = MessageDigest.getInstance("SHA-256");

                try (InputStream in = Files.newInputStream(p, StandardOpenOption.READ)) {
                    byte[] buffer = new byte[64 * 1024];
                    int len;

                    while ((len = in.read(buffer)) != -1) {
                        instance.update(buffer, 0, len);
                    }
                }

                String hash = DatatypeConverter.printHexBinary(instance.digest());
                return hash;
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }


    }
    private static void dedupe(List<Path> paths) {
        try {
            final Path reference = paths.get(0);
            saving.accumulateAndGet((paths.size() - 1) * Files.size(reference), (a, b) -> (a + b));

            paths.stream().skip(1).forEach(clone -> reflink(reference, clone));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reflink(Path reference, Path clone) {
        try {
            final PosixFileAttributeView permissionsView = Files.getFileAttributeView(clone, PosixFileAttributeView.class);
            final PosixFileAttributes oldAttributes = permissionsView.readAttributes();
            FileTime lastModifiedTime = Files.getLastModifiedTime(clone);

            Files.delete(clone);

            Process cp = new ProcessBuilder("/bin/cp", "--reflink=always", reference.toString(), clone.toString())
                    .inheritIO()
                    .start();
            int exitCode = cp.waitFor();
            if (exitCode != 0) {
                throw new IOException(MessageFormat.format("cp returned: {0}, when linking \"{1}\" -> \"{2}\"", exitCode, reference.toString(), clone.toString()));
            }

            permissionsView.setOwner(oldAttributes.owner());
            permissionsView.setGroup(oldAttributes.group());
            permissionsView.setPermissions(oldAttributes.permissions());
            Files.setLastModifiedTime(clone, lastModifiedTime);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
