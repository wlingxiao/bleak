package goa.http1.multipart;

public interface ReadHandler {

    void onDataAvailable() throws Exception;

    void onError(final Throwable t);

    void onAllDataRead() throws Exception;

}
