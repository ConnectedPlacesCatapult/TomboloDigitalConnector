package uk.org.tombolo.importer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

public class ZipUtils {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ZipUtils.class);

    public static Path unzipToTemporaryDirectory(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
        Path tempDirectory = Files.createTempDirectory("temp");
        while (zipEntries.hasMoreElements()) {
            ZipArchiveEntry entry = zipEntries.nextElement();
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), new File(Paths.get(tempDirectory.toString(),"/" + entry.getName()).toString()));
        }

        zipFile.close();
        return tempDirectory;
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
