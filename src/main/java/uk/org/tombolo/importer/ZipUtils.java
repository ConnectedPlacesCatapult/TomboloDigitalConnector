package uk.org.tombolo.importer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class ZipUtils {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ZipUtils.class);

    public static Path unzipToTemporaryDirectory(File file) throws IOException {
        File tempDirectory = new File("/tmp/" + UUID.nameUUIDFromBytes(file.getName().getBytes()).toString());
        if (!tempDirectory.exists()) {
            tempDirectory.mkdir();
            ZipFile zipFile = new ZipFile(file);
            Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
            while (zipEntries.hasMoreElements()) {
                ZipArchiveEntry entry = zipEntries.nextElement();
                if (!entry.isDirectory()) {
                    FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), new File(Paths.get(tempDirectory.toString(), "/" + entry.getName()).toString()));
                }
            }
            zipFile.close();
        }
        System.out.println(tempDirectory.toPath());
        return tempDirectory.toPath();
    }
    /**
     * Checks if an input stream is gzipped.
     * Gzipped files have a magic number to recognize them.
     */
    public static boolean isGZipped(File f) {
        int magic = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
            raf.close();
        } catch (Throwable e) {
            log.warn("Failed to check if gzipped {}", e.getMessage());
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }
}
