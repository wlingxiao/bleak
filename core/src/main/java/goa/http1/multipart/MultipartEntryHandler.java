package goa.http1.multipart;

public interface MultipartEntryHandler {

    void handle(final MultipartEntry multipartEntry) throws Exception;

}
