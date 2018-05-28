package goa.http1.multipart;

public class ContentType {

    public static String getCharsetFromContentType(String contentType) {

        if (contentType == null) {
            return null;
        }

        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return null;
        }
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\""))
                && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }
        return (encoding.trim());
    }

}
