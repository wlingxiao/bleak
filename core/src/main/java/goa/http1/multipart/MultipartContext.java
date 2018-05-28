package goa.http1.multipart;

import java.util.Collections;
import java.util.Map;

public class MultipartContext {

    public static final String START_ATTR = "start";
    public static final String START_INFO_ATTR = "start-info";
    public static final String TYPE_ATTR = "type";
    public static final String BOUNDARY_ATTR = "boundary";

    private final String contentType;
    private final String boundary;
    private final Map<String, String> contentTypeAttributes;

    public MultipartContext(final String boundary,
                            final String contentType,
                            final Map<String, String> contentTypeAttributes) {
        this.contentType = contentType;
        this.boundary = boundary;
        this.contentTypeAttributes =
                Collections.unmodifiableMap(contentTypeAttributes);
    }

    public String getBoundary() {
        return boundary;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getContentTypeAttributes() {
        return contentTypeAttributes;
    }

}
