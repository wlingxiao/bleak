package goa.http1.multipart;

import java.util.HashMap;
import java.util.Map;


public class MultipartScanner {

    public static final String BOUNDARY_ATTR = "boundary";

    static final String MULTIPART_CONTENT_TYPE = "multipart";

    private MultipartScanner() {
    }


    public static void scan(final Request request,
                            final MultipartEntryHandler partHandler,
                            final CompletionHandler<Request> completionHandler) {
        try {
            final String contentType = request.getContentType();
            if (contentType == null) {
                throw new IllegalStateException("ContentType not found");
            }

            final String[] contentTypeParams = contentType.split(";");
            final String[] contentSubType = contentTypeParams[0].split("/");

            if (contentSubType.length != 2
                    || !MULTIPART_CONTENT_TYPE.equalsIgnoreCase(contentSubType[0])) {
                throw new IllegalStateException("Not multipart request");
            }

            String boundary = null;
            final Map<String, String> contentTypeProperties =
                    new HashMap<String, String>();

            for (int i = 1; i < contentTypeParams.length; i++) {
                final String param = contentTypeParams[i].trim();
                final String[] paramValue = param.split("=", 2);
                if (paramValue.length == 2) {
                    String key = paramValue[0].trim();
                    String value = paramValue[1].trim();
                    if (value.charAt(0) == '"') {
                        value = value.substring(1,
                                value.length()
                                        - 1);
                    }
                    contentTypeProperties.put(key, value);
                    if (BOUNDARY_ATTR.equals(key)) {
                        boundary = value;
                    }
                }
            }

            if (boundary == null) {
                throw new IllegalStateException("Boundary not found");
            }

            final NIOInputStream nioInputStream = request.getNIOInputStream();

            nioInputStream.notifyAvailable(new MultipartReadHandler(request,
                    partHandler, completionHandler,
                    new MultipartContext(boundary, contentType,
                            contentTypeProperties)));
        } catch (Exception e) {
            if (completionHandler != null) {
                completionHandler.failed(e);
            } else {
            }
        }
    }

    public static void scan(final MultipartEntry multipartMixedEntry,
                            final MultipartEntryHandler partHandler,
                            final CompletionHandler<MultipartEntry> completionHandler) {
        try {
            final String contentType = multipartMixedEntry.getContentType();
            final String[] contentTypeParams = contentType.split(";");
            final String[] contentSubType = contentTypeParams[0].split("/");

            if (contentSubType.length != 2
                    || !MULTIPART_CONTENT_TYPE.equalsIgnoreCase(contentSubType[0])) {
                throw new IllegalStateException("Not multipart request");
            }

            String boundary = null;
            final Map<String, String> contentTypeProperties =
                    new HashMap<String, String>();

            for (int i = 1; i < contentTypeParams.length; i++) {
                final String param = contentTypeParams[i].trim();
                final String[] paramValue = param.split("=", 2);
                if (paramValue.length == 2) {
                    String key = paramValue[0].trim();
                    String value = paramValue[1].trim();
                    if (value.charAt(0) == '"') {
                        value = value.substring(1,
                                value.length()
                                        - 1);
                    }
                    contentTypeProperties.put(key, value);
                    if (BOUNDARY_ATTR.equals(key)) {
                        boundary = value;
                    }
                }
            }

            if (boundary == null) {
                throw new IllegalStateException("Boundary not found");
            }

            final NIOInputStream nioInputStream = multipartMixedEntry.getNIOInputStream();

            nioInputStream.notifyAvailable(new MultipartReadHandler(multipartMixedEntry,
                    partHandler, completionHandler,
                    new MultipartContext(boundary, contentType,
                            contentTypeProperties)));
        } catch (Exception e) {
            if (completionHandler != null) {
                completionHandler.failed(e);
            } else {
            }
        }
    }

}
