package goa.http1.multipart;

public interface CompletionHandler<E> {
    void cancelled();

    void failed(Throwable throwable);

    void completed(E result);

    void updated(E result);
}
