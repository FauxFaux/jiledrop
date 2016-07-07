package com.goeswhere.jiledrop.app;

import com.goeswhere.jiledrop.types.FileId;
import com.goeswhere.jiledrop.types.Target;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;

import static org.junit.Assert.*;

public class StorageTest {
    final Target t = new Target(StringUtils.repeat('t', Target.LENGTH));
    final FileId f = new FileId(StringUtils.repeat('f', FileId.LENGTH));
    private final Random random = new Random(1);

    @Test
    public void happy() throws IOException {
        final Storage s = temporaryStorage();
        assertFalse(s.partComplete(f, 0));
        s.storePart(f, 0, randomChunk(), false);
        assertTrue(s.partComplete(f, 0));
        assertFalse(s.partComplete(f, 1));
        s.storePart(f, 1, randomChunk(), false);
        assertTrue(s.partComplete(f, 1));
        System.out.println(s.combine(f, "out", 2));
    }

    @Test
    public void checksum() throws Exception {
        final Storage s = temporaryStorage();
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final int totalChunks = 25;

        for (int i = 0; i < totalChunks; i++) {
            final byte[] out = chunkOfBytes();
            s.storePart(f, i, new ByteArrayInputStream(out), false);
            digest.update(out);
        }

        final byte[] expected = digest.digest();

        digest.reset();

        final File written = new File(s.targetDirectory, s.combine(f, "foo", totalChunks));
        final byte[] actual = digest.digest(Files.toByteArray(written));

        assertArrayEquals(expected, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidChunkShort() throws IOException {
        final Storage s = temporaryStorage();
        s.storePart(f, 0, new ByteArrayInputStream(new byte[Storage.CHUNK_SIZE - 1]), false);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidChunkLong() throws IOException {
        final Storage s = temporaryStorage();
        s.storePart(f, 0, new ByteArrayInputStream(new byte[Storage.CHUNK_SIZE + 1]), false);
    }

    @Test
    public void fileNameCollision() throws IOException {
        final File dir = deleteAtExit(Files.createTempDir());

        final File input = new File(dir, "foo");
        assertEquals(input.getAbsolutePath(), Storage.alterToUniqueFileName(input).getAbsolutePath());

        final File secondResult = Storage.alterToUniqueFileName(input);
        assertNotEquals(input.getAbsolutePath(), secondResult.getAbsolutePath());
        assertEquals(input.getParent(), secondResult.getParent());
    }

    @Test(expected = FileNotFoundException.class)
    public void combineWithoutChunks() throws IOException {
        final Storage s = temporaryStorage();
        s.combine(f, "bar", 1);
    }

    @Test(expected = FileNotFoundException.class)
    public void combineMissingChunk() throws IOException {
        final Storage s = temporaryStorage();
        s.storePart(f, 0, randomChunk(), false);
        // chunk 1 missing
        s.storePart(f, 2, randomChunk(), false);
        s.combine(f, "bar", 3);
    }

    private byte[] chunkOfBytes() {
        final byte[] buf = new byte[Storage.CHUNK_SIZE];
        random.nextBytes(buf);
        return buf;
    }

    private ByteArrayInputStream randomChunk() {
        final byte[] buf = chunkOfBytes();
        return new ByteArrayInputStream(buf);
    }

    private Storage temporaryStorage() {
        final File tmp = deleteAtExit(Files.createTempDir());
        return new Storage(t, tmp);
    }

    private static File deleteAtExit(File tmp) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tmp);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }));
        return tmp;
    }
}