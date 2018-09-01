package goa.http1.multipart;

import java.util.Map;
import java.util.TreeMap;

public enum Header {

    Accept("Accept"),
    AcceptCharset("Accept-Charset"),
    AcceptEncoding("Accept-Encoding"),
    AcceptRanges("Accept-Ranges"),
    Age("Age"),
    Allow("Allow"),
    Authorization("Authorization"),
    CacheControl("Cache-Control"),
    Cookie("Cookie"),
    Connection("Connection"),
    ContentDisposition("Content-Disposition"),
    ContentEncoding("Content-Encoding"),
    ContentLanguage("Content-Language"),
    ContentLength("Content-Length"),
    ContentLocation("Content-Location"),
    ContentMD5("Content-MD5"),
    ContentRange("Content-Range"),
    ContentType("Content-Type"),
    Date("Date"),
    ETag("ETag"),
    Expect("Expect"),
    Expires("Expires"),
    From("From"),
    Host("Host"),
    IfMatch("If-Match"),
    IfModifiedSince("If-Modified-Since"),
    IfNoneMatch("If-None-Match"),
    IfRange("If-Range"),
    IfUnmodifiedSince("If-Unmodified-Since"),
    KeepAlive("Keep-Alive"),
    LastModified("Last-Modified"),
    Location("Location"),
    MaxForwards("Max-Forwards"),
    Pragma("Pragma"),
    ProxyAuthenticate("Proxy-Authenticate"),
    ProxyAuthorization("Proxy-Authorization"),
    ProxyConnection("Proxy-Connection"),
    Range("Range"),
    @SuppressWarnings("SpellCheckingInspection")
    Referer("Referer"),
    RetryAfter("Retry-After"),
    Server("Server"),
    SetCookie("Set-Cookie"),
    TE("TE"),
    Trailer("Trailer"),
    TransferEncoding("Transfer-Encoding"),
    Upgrade("Upgrade"),
    UserAgent("User-Agent"),
    Vary("Vary"),
    Via("Via"),
    Warnings("Warning"),
    WWWAuthenticate("WWW-Authenticate"),
    XPoweredBy("X-Powered-By"),
    HTTP2Settings("HTTP2-Settings");


    private static final Map<String, Header> VALUES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final String headerName;

    static {
        for (final Header h : Header.values()) {
            VALUES.put(h.toString(), h);
        }
    }


    Header(final String headerName) {
        this.headerName = headerName;
    }

    @Override
    public final String toString() {
        return headerName;
    }
}
