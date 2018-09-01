package goa.http1.multipart;

public interface BinaryNIOInputSource extends InputSource {

    Buffer getBuffer();

    Buffer readBuffer();

    Buffer readBuffer(int size);

}
