package goa.http1.multipart;

import org.junit.Assert;
import org.junit.Test;

public class MultipartScannerTests {

    @Test
    public void tesScan() {
        Request request = new Request();
        MultipartEntryHandler handler = new MultipartEntryHandler() {
            @Override
            public void handle(final MultipartEntry multipartEntry) throws Exception {
                final ContentDisposition contentDisposition =
                        multipartEntry.getContentDisposition();
                final String name = contentDisposition.getDispositionParamUnquoted("name");
                System.out.println(name);
                final NIOReader nioReader = multipartEntry.getNIOReader();
                nioReader.notifyAvailable(new ReadHandler() {
                    @Override
                    public void onDataAvailable() throws Exception {
                        char[] buf = new char[1024];
                        nioReader.read(buf);
                        System.out.println(new String(buf));
                        nioReader.notifyAvailable(this);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onAllDataRead() throws Exception {
                        char[] buf = new char[1024];
                        nioReader.read(buf);
                        String s = new String(buf).trim();
                        System.out.println(s);
                        Assert.assertEquals("666666666611111111111111", s);
                    }
                });
            }
        };
        MultipartScanner.scan(request, handler, null);

    }

}
