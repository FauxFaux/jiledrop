package com.goeswhere.jiledrop.app;

import com.goeswhere.jiledrop.types.FileId;
import com.goeswhere.jiledrop.types.Target;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public class Storage {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    public static final int CHUNK_SIZE = 1_000_000;

    final File targetDirectory;
    private final File wipDirectory;

    public Storage(Target target, File storageDirectory) {
        targetDirectory = new File(storageDirectory, target.value).getAbsoluteFile();
        wipDirectory = new File(targetDirectory, "wip").getAbsoluteFile();

        ensureDirectory(wipDirectory);
    }

    public boolean partComplete(FileId fileId, int chunkNumber) {
        final File file = pathForChunk(fileId, chunkNumber);
        return file.isFile() && file.canRead();
    }

    private File pathForChunk(FileId fileId, int chunkNumber) {
        return new File(wipDirectory, fileId.value + "." + chunkNumber + ".chunk");
    }

    public boolean storePart(FileId fileId, int chunkNumber, InputStream data, boolean lastChunk) throws IOException {
        if (partComplete(fileId, chunkNumber)) {
            return false;
        }

        final File temp = File.createTempFile("uploading", ".wip", wipDirectory);
        try {
            try (OutputStream tempStream = new FileOutputStream(temp)) {
                if (CHUNK_SIZE != ByteStreams.copy(data, tempStream) && !lastChunk) {
                    throw new IllegalStateException("not enough data / failure writing");
                }
            }

            if (!temp.renameTo(pathForChunk(fileId, chunkNumber))) {
                throw new IllegalStateException("couldn't rename file into place");
            }

            return true;
        } finally {
            if (temp.exists() && !temp.delete()) {
                logger.warn("couldn't delete temporary file; ignoring: {}", temp);
            }
        }
    }

    public String combine(FileId fileId, String destinationFileName, int totalChunks) throws IOException {
        final File destination = alterToUniqueFileName(new File(targetDirectory, new File(destinationFileName).getName()));
        final File temp = File.createTempFile("combining", ".wip", targetDirectory);
        try {

            try (OutputStream dest = new FileOutputStream(temp)) {
                for (int chunk = 1; chunk <= totalChunks; chunk++) {
                    try (final InputStream chunkStream = new FileInputStream(pathForChunk(fileId, chunk))) {
                        ByteStreams.copy(chunkStream, dest);
                    }
                }
            }

            Files.move(temp.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            for (int chunk = 0; chunk < totalChunks; chunk++) {
                final File chunkFile = pathForChunk(fileId, chunk);

                if (!chunkFile.delete()) {
                    logger.warn("couldn't remove part file: {}", chunkFile);
                }
            }

            return destination.getName();
        } finally {
            if (temp.exists() && !temp.delete()) {
                logger.warn("couldn't delete temporary file; ignoring: {}", temp);
            }
        }
    }

    static File alterToUniqueFileName(File original) throws IOException {
        File candidate = original;
        while (!candidate.createNewFile()) {
            candidate = new File(candidate.getParentFile(),
                    Instant.now().toString() + "-" + candidate.getName());
        }
        return candidate;
    }

    private static void ensureDirectory(File path) {
        if (path.isDirectory()) {
            return;
        }

        if (path.mkdirs()) {
            return;
        }

        if (path.isDirectory()) {
            return;
        }

        throw new IllegalStateException("couldn't create directory:" + path);
    }
}
