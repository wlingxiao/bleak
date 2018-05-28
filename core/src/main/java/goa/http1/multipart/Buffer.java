package goa.http1.multipart;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Buffer {

    int position() {
        return 0;
    }

    String toStringContent(Charset charset, int position, int limit) {
        return "";
    }

    byte get(int position) {
        return (byte) 'a';
    }

    Buffer put(int index, byte b) {
        return null;
    }

    Buffer limit(int newLimit) {
        return null;
    }

    ByteBuffer toByteBuffer() {
        return null;
    }

}
