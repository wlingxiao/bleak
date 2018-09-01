package goa.http1.multipart;

import goa.http1.HttpRequest;
import goa.util.BufferUtils;

import java.io.IOException;

public class Request {

    private HttpRequest httpRequest;

    NIOInputStream nioInputStream;

    public Request(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;

        nioInputStream = new NIOInputStream() {

            byte[] body = BufferUtils.bufferToByteArry(httpRequest.body().apply());

            Buffer inputContentBuffer = new HeapBuffer(body, 0, body.length);

            @Override
            public Buffer getBuffer() {
                return inputContentBuffer.duplicate();
            }

            @Override
            public Buffer readBuffer() {
                return null;
            }

            @Override
            public Buffer readBuffer(int size) {
                return null;
            }

            @Override
            public void notifyAvailable(ReadHandler handler) {
                try {
                    handler.onAllDataRead();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void notifyAvailable(ReadHandler handler, int size) {
                try {
                    handler.onAllDataRead();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public int readyData() {
                return body.length;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public long skip(long n) throws IOException {

                long nlen = Math.min(inputContentBuffer.remaining(), n);
                inputContentBuffer.position(inputContentBuffer.position() + (int) nlen);
                return nlen;

            }

        };

    }

    Request() {

    }

    NIOInputStream getNIOInputStream() {
        return nioInputStream;
    }

    String getContentType() {
        assert httpRequest != null;
        return httpRequest.contentType();
        //return "multipart/form-data; boundary=--------------------------744661334412585608819073";
    }
}
