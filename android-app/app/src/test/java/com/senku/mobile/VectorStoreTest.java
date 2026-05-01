package com.senku.mobile;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class VectorStoreTest {
    @Test
    public void opensValidInt8VectorFile() throws Exception {
        File file = writeTempVectorFile(vectorFile(2, 3, 2, new byte[] {
            127, 0, 0,
            0, 127, 0
        }));

        try (VectorStore store = new VectorStore(file)) {
            assertEquals(2, store.getRowCount());
            assertEquals(3, store.getDimension());

            List<VectorStore.VectorNeighbor> neighbors = store.findNearest(new float[] {1f, 0f, 0f}, 1);
            assertEquals(1, neighbors.size());
            assertEquals(0, neighbors.get(0).rowId);
        }
    }

    @Test
    public void rejectsFileTooSmallForHeaderWithIOException() throws Exception {
        File file = writeTempVectorFile(new byte[] {1, 2, 3});

        expectRejected(file, "too small for header");
    }

    @Test
    public void rejectsHeaderShorterThanMinimumWithIOException() throws Exception {
        File file = writeTempVectorFile(vectorFile(1, 3, 2, new byte[] {1, 2, 3}, 24));

        expectRejected(file, "header bytes");
    }

    @Test
    public void rejectsTruncatedVectorPayloadWithIOException() throws Exception {
        File file = writeTempVectorFile(vectorFile(2, 3, 2, new byte[] {
            127, 0, 0,
            0, 127
        }));

        expectRejected(file, "truncated");
    }

    @Test
    public void rejectsInvalidDimensionWithIOException() throws Exception {
        File file = writeTempVectorFile(vectorFile(1, 0, 2, new byte[0]));

        expectRejected(file, "dimension");
    }

    @Test
    public void rejectsOverflowingLayoutWithIOException() throws Exception {
        File file = writeTempVectorFile(vectorFile(Integer.MAX_VALUE, 2, 1, new byte[0]));

        expectRejected(file, "too large");
    }

    private static void expectRejected(File file, String expectedMessage) throws Exception {
        try (VectorStore ignored = new VectorStore(file)) {
            fail("Expected IOException containing: " + expectedMessage);
        } catch (IOException exc) {
            assertTrue(
                "Expected message containing " + expectedMessage + " but was: " + exc.getMessage(),
                exc.getMessage().contains(expectedMessage)
            );
        }
    }

    private static File writeTempVectorFile(byte[] bytes) throws IOException {
        File file = File.createTempFile("senku-vectors", ".vec");
        file.deleteOnExit();
        Files.write(file.toPath(), bytes);
        return file;
    }

    private static byte[] vectorFile(int rowCount, int dimension, int dtypeCode, byte[] payload) throws IOException {
        return vectorFile(rowCount, dimension, dtypeCode, payload, 32);
    }

    private static byte[] vectorFile(
        int rowCount,
        int dimension,
        int dtypeCode,
        byte[] payload,
        int headerBytes
    ) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteBuffer header = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
        header.put("SNKUVEC1".getBytes(StandardCharsets.US_ASCII));
        header.putInt(1);
        header.putInt(headerBytes);
        header.putInt(rowCount);
        header.putInt(dimension);
        header.putInt(dtypeCode);
        header.putInt(0);
        output.write(header.array());
        for (int index = 32; index < headerBytes; index++) {
            output.write(0);
        }
        output.write(payload);
        return output.toByteArray();
    }
}
