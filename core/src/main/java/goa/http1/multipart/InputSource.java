package goa.http1.multipart;

public interface InputSource {

    void notifyAvailable(final ReadHandler handler);

    void notifyAvailable(final ReadHandler handler, final int size);

    boolean isFinished();


    int readyData();


    boolean isReady();

}
