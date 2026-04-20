package com.senku.mobile;

import android.util.Half;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class VectorStore implements AutoCloseable {
    private static final int DTYPE_FLOAT16 = 1;
    private static final int DTYPE_INT8 = 2;

    private final Object closeLock = new Object();
    private final FileInputStream inputStream;
    private final FileChannel channel;
    private MappedByteBuffer buffer;
    private final int rowCount;
    private final int dimension;
    private final int dtypeCode;
    private final int headerBytes;
    private final int rowBytes;
    private boolean closed;

    public VectorStore(File file) throws IOException {
        FileInputStream openedInputStream = new FileInputStream(file);
        FileChannel openedChannel = null;
        MappedByteBuffer mappedBuffer = null;
        boolean success = false;
        int parsedRowCount = 0;
        int parsedDimension = 0;
        int parsedDtypeCode = 0;
        int parsedHeaderBytes = 0;
        int parsedRowBytes = 0;
        try {
            openedChannel = openedInputStream.getChannel();
            mappedBuffer = openedChannel.map(FileChannel.MapMode.READ_ONLY, 0, openedChannel.size());
            mappedBuffer.order(ByteOrder.LITTLE_ENDIAN);

            byte[] magic = new byte[8];
            mappedBuffer.position(0);
            mappedBuffer.get(magic);
            int version = mappedBuffer.getInt();
            parsedHeaderBytes = mappedBuffer.getInt();
            parsedRowCount = mappedBuffer.getInt();
            parsedDimension = mappedBuffer.getInt();
            parsedDtypeCode = mappedBuffer.getInt();
            int flags = mappedBuffer.getInt();

            if (!"SNKUVEC1".equals(new String(magic, StandardCharsets.US_ASCII))) {
                throw new IOException("Unsupported vector file magic");
            }
            if (version != 1) {
                throw new IOException("Unsupported vector file version: " + version);
            }
            if (parsedDtypeCode != DTYPE_FLOAT16 && parsedDtypeCode != DTYPE_INT8) {
                throw new IOException("Unsupported vector dtype code: " + parsedDtypeCode);
            }

            parsedRowBytes = parsedDimension * (parsedDtypeCode == DTYPE_FLOAT16 ? 2 : 1);
            success = true;
        } finally {
            if (!success) {
                tryUnmap(mappedBuffer);
                closeQuietly(openedChannel);
                closeQuietly(openedInputStream);
            }
        }

        this.inputStream = openedInputStream;
        this.channel = openedChannel;
        this.buffer = mappedBuffer;
        this.rowCount = parsedRowCount;
        this.dimension = parsedDimension;
        this.dtypeCode = parsedDtypeCode;
        this.headerBytes = parsedHeaderBytes;
        this.rowBytes = parsedRowBytes;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getDimension() {
        return dimension;
    }

    public float[] buildCentroid(List<Integer> rowIds, int seedCount) {
        synchronized (closeLock) {
            if (rowIds.isEmpty()) {
                return null;
            }

            MappedByteBuffer mappedBuffer = requireOpenBufferLocked();
            int count = Math.min(seedCount, rowIds.size());
            float[] centroid = new float[dimension];
            int used = 0;
            for (int index = 0; index < count; index++) {
                int rowId = rowIds.get(index);
                if (rowId < 0 || rowId >= rowCount) {
                    continue;
                }
                addRowTo(mappedBuffer, rowId, centroid);
                used += 1;
            }
            if (used == 0) {
                return null;
            }
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] /= used;
            }
            return normalize(centroid);
        }
    }

    public List<VectorNeighbor> findNearest(float[] queryVector, int limit) {
        synchronized (closeLock) {
            if (queryVector == null || queryVector.length != dimension || limit <= 0) {
                return new ArrayList<>();
            }

            MappedByteBuffer mappedBuffer = requireOpenBufferLocked();
            float[] normalized = normalize(queryVector.clone());
            PriorityQueue<VectorNeighbor> heap = new PriorityQueue<>(limit, Comparator.comparingDouble(a -> a.score));
            for (int rowId = 0; rowId < rowCount; rowId++) {
                float score = dotRow(mappedBuffer, rowId, normalized);
                if (heap.size() < limit) {
                    heap.offer(new VectorNeighbor(rowId, score));
                } else if (score > heap.peek().score) {
                    heap.poll();
                    heap.offer(new VectorNeighbor(rowId, score));
                }
            }

            ArrayList<VectorNeighbor> ordered = new ArrayList<>(heap);
            ordered.sort((left, right) -> Float.compare(right.score, left.score));
            return ordered;
        }
    }

    private float dotRow(MappedByteBuffer mappedBuffer, int rowId, float[] queryVector) {
        int base = headerBytes + (rowId * rowBytes);
        float sum = 0f;
        if (dtypeCode == DTYPE_FLOAT16) {
            for (int i = 0; i < dimension; i++) {
                short raw = mappedBuffer.getShort(base + (i * 2));
                sum += Half.toFloat(raw) * queryVector[i];
            }
        } else {
            for (int i = 0; i < dimension; i++) {
                byte raw = mappedBuffer.get(base + i);
                sum += (raw / 127.0f) * queryVector[i];
            }
        }
        return sum;
    }

    private void addRowTo(MappedByteBuffer mappedBuffer, int rowId, float[] target) {
        int base = headerBytes + (rowId * rowBytes);
        if (dtypeCode == DTYPE_FLOAT16) {
            for (int i = 0; i < dimension; i++) {
                short raw = mappedBuffer.getShort(base + (i * 2));
                target[i] += Half.toFloat(raw);
            }
        } else {
            for (int i = 0; i < dimension; i++) {
                byte raw = mappedBuffer.get(base + i);
                target[i] += raw / 127.0f;
            }
        }
    }

    private MappedByteBuffer requireOpenBufferLocked() {
        if (closed || buffer == null) {
            throw new IllegalStateException("VectorStore is closed");
        }
        return buffer;
    }

    private static float[] normalize(float[] values) {
        double norm = 0.0;
        for (float value : values) {
            norm += value * value;
        }
        if (norm <= 0.0) {
            return values;
        }
        float inverse = (float) (1.0 / Math.sqrt(norm));
        for (int i = 0; i < values.length; i++) {
            values[i] *= inverse;
        }
        return values;
    }

    @Override
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;

            MappedByteBuffer mappedBuffer = buffer;
            buffer = null;

            IOException closeFailure = null;
            tryUnmap(mappedBuffer);
            try {
                channel.close();
            } catch (IOException exc) {
                closeFailure = exc;
            }
            try {
                inputStream.close();
            } catch (IOException exc) {
                if (closeFailure == null) {
                    closeFailure = exc;
                } else {
                    closeFailure.addSuppressed(exc);
                }
            }
            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }

    private static void tryUnmap(MappedByteBuffer mappedBuffer) {
        if (mappedBuffer == null) {
            return;
        }
        if (tryUnmapWithAndroidNioUtils(mappedBuffer)) {
            return;
        }
        if (tryUnmapWithUnsafe(mappedBuffer)) {
            return;
        }
        tryUnmapWithCleaner(mappedBuffer);
    }

    private static boolean tryUnmapWithAndroidNioUtils(ByteBuffer buffer) {
        try {
            Class<?> nioUtilsClass = Class.forName("java.nio.NioUtils");
            Method freeDirectBuffer = nioUtilsClass.getDeclaredMethod("freeDirectBuffer", ByteBuffer.class);
            freeDirectBuffer.setAccessible(true);
            freeDirectBuffer.invoke(null, buffer);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean tryUnmapWithUnsafe(ByteBuffer buffer) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Object unsafe = theUnsafe.get(null);
            Method invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            invokeCleaner.invoke(unsafe, buffer);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean tryUnmapWithCleaner(ByteBuffer buffer) {
        try {
            Method cleanerMethod = buffer.getClass().getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner == null) {
                return false;
            }
            Method cleanMethod = cleaner.getClass().getMethod("clean");
            cleanMethod.setAccessible(true);
            cleanMethod.invoke(cleaner);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    public static final class VectorNeighbor {
        public final int rowId;
        public final float score;

        public VectorNeighbor(int rowId, float score) {
            this.rowId = rowId;
            this.score = score;
        }
    }
}
