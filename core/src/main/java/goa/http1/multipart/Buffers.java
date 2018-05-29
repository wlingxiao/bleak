package goa.http1.multipart;

import java.nio.ByteBuffer;

public class Buffers {

    public static void setPositionLimit(final ByteBuffer buffer,
                                        final int position, final int limit) {
        buffer.limit(limit);
        buffer.position(position);
    }

}
