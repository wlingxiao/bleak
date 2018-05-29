package goa.http1.multipart;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public interface Buffer {

    int position();

    String toStringContent(Charset charset, int position, int limit);

    byte get(int position);

    Buffer put(int index, byte b);

    Buffer limit(int newLimit);

    ByteBuffer toByteBuffer();

    void shrink();

    int remaining();

    HeapBuffer position(final int newPosition);

    Buffer duplicate();
}
