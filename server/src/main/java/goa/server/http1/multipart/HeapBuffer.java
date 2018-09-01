package goa.server.http1.multipart;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class HeapBuffer implements Buffer {

    protected boolean allowBufferDispose = false;

    protected Exception disposeStackTrace;

    protected byte[] heap;

    protected int offset;

    protected int pos;

    protected int cap;

    protected int lim;

    protected int mark = -1;

    boolean hasClonedArray;

    protected ByteBuffer byteBuffer;

    HeapBuffer(final byte[] heap,
               final int offset,
               final int cap) {
        this.heap = heap;
        this.offset = offset;
        this.cap = cap;
        this.lim = this.cap;
    }

    protected void checkDispose() {
        if (heap == null) {
            throw new IllegalStateException(
                    "HeapBuffer has already been disposed",
                    disposeStackTrace);
        }
    }

    @Override
    public final int position() {
        checkDispose();
        return pos;
    }

    public String toStringContent(Charset charset, int position, int limit) {
        checkDispose();
        if (charset == null) {
            charset = Charset.defaultCharset();
        }

        final boolean isRestoreByteBuffer = byteBuffer != null;
        int oldPosition = 0;
        int oldLimit = 0;

        if (isRestoreByteBuffer) {
            // ByteBuffer can be used by outer code - so save its state
            oldPosition = byteBuffer.position();
            oldLimit = byteBuffer.limit();
        }

        final ByteBuffer bb = toByteBuffer0(position, limit, false);

        try {
            return charset.decode(bb).toString();
        } finally {
            if (isRestoreByteBuffer) {
                Buffers.setPositionLimit(byteBuffer, oldPosition, oldLimit);
            }
        }
    }

    protected ByteBuffer toByteBuffer0(final int pos,
                                       final int lim,
                                       final boolean slice) {
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.wrap(heap);
        }

        Buffers.setPositionLimit(byteBuffer, offset + pos, offset + lim);

        return ((slice) ? byteBuffer.slice() : byteBuffer);

    }

    @Override
    public byte get(int index) {
        if (index < 0 || index >= lim) {
            throw new IndexOutOfBoundsException();
        }
        return heap[offset + index];
    }

    @Override
    public HeapBuffer put(int index, byte b) {
        if (index < 0 || index >= lim) {
            throw new IndexOutOfBoundsException();
        }
        heap[offset + index] = b;
        return this;
    }

    @Override
    public final HeapBuffer limit(final int newLimit) {
        checkDispose();
        lim = newLimit;
        if (mark > lim) mark = -1;
        return this;
    }

    public ByteBuffer toByteBuffer(final int position, final int limit) {
        return toByteBuffer0(position, limit, false);
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return toByteBuffer(pos, lim);
    }

    @Override
    public void shrink() {
        checkDispose();
    }

    @Override
    public final int remaining() {
        return (lim - pos);
    }

    @Override
    public final HeapBuffer position(final int newPosition) {
        checkDispose();
        pos = newPosition;
        if (mark > pos) mark = -1;
        return this;
    }

    @Override
    public Buffer duplicate() {
        checkDispose();

        final HeapBuffer duplicate =
                createHeapBuffer(0, cap);
        duplicate.position(pos);
        duplicate.limit(lim);
        return duplicate;
    }

    protected HeapBuffer createHeapBuffer(final int offs, final int capacity) {
        onShareHeap();

        return new HeapBuffer(
                heap,
                offs + offset,
                capacity);
    }

    protected void onShareHeap() {
        if (!hasClonedArray) {
            heap = Arrays.copyOfRange(heap, offset, offset + cap);
            offset = 0;
            hasClonedArray = true;
        }
    }
}
