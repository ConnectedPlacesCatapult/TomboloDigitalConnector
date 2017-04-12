package uk.org.tombolo.importer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

public class ZipUtils {
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
}
