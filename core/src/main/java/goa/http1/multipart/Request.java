package goa.http1.multipart;

import java.io.IOException;

public class Request {

    NIOInputStream nioInputStream = new NIOInputStream() {

        byte[] body = ("----------------------------744661334412585608819073\r\n" +
                "Content-Disposition: form-data; name=\"description\"\r\n" +
                "\r\n" +
                "666666666611111111111111\r\n" +
                "----------------------------744661334412585608819073--\r\n").getBytes();

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

    NIOInputStream getNIOInputStream() {
        return nioInputStream;
    }

    String getContentType() {
        return "multipart/form-data; boundary=--------------------------744661334412585608819073";
    }
}
