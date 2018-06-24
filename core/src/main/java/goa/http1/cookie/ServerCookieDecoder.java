package goa.http1.cookie;

import goa.Cookie;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ServerCookieDecoder extends CookieDecoder {
    private static final String RFC2965_VERSION = "$Version";

    private static final String RFC2965_PATH = "$" + CookieHeaderNames.PATH;

    private static final String RFC2965_DOMAIN = "$" + CookieHeaderNames.DOMAIN;

    private static final String RFC2965_PORT = "$Port";

    /**
     * Strict encoder that validates that name and value chars are in the valid scope
     * defined in RFC6265
     */
    public static final ServerCookieDecoder STRICT = new ServerCookieDecoder(true);

    /**
     * Lax instance that doesn't validate name and value
     */
    public static final ServerCookieDecoder LAX = new ServerCookieDecoder(false);

    ServerCookieDecoder(boolean strict) {
        super(strict);
    }

    /**
     * Decodes the specified Set-Cookie HTTP header value into a {@link Cookie}.
     *
     * @return the decoded {@link Cookie}
     */
    public Set<Cookie> decode(String header) {
        final int headerLen = header.length();

        if (headerLen == 0) {
            return Collections.emptySet();
        }

        Set<Cookie> cookies = new HashSet<>();

        int i = 0;

        boolean rfc2965Style = false;
        if (header.regionMatches(true, 0, RFC2965_VERSION, 0, RFC2965_VERSION.length())) {
            // RFC 2965 style cookie, move to after version value
            i = header.indexOf(';') + 1;
            rfc2965Style = true;
        }

        loop:
        for (; ; ) {

            // Skip spaces and separators.
            for (; ; ) {
                if (i == headerLen) {
                    break loop;
                }
                char c = header.charAt(i);
                if (c == '\t' || c == '\n' || c == 0x0b || c == '\f'
                        || c == '\r' || c == ' ' || c == ',' || c == ';') {
                    i++;
                    continue;
                }
                break;
            }

            int nameBegin = i;
            int nameEnd;
            int valueBegin;
            int valueEnd;

            for (; ; ) {

                char curChar = header.charAt(i);
                if (curChar == ';') {
                    // NAME; (no value till ';')
                    nameEnd = i;
                    valueBegin = valueEnd = -1;
                    break;

                } else if (curChar == '=') {
                    // NAME=VALUE
                    nameEnd = i;
                    i++;
                    if (i == headerLen) {
                        // NAME= (empty value, i.e. nothing after '=')
                        valueBegin = valueEnd = 0;
                        break;
                    }

                    valueBegin = i;
                    // NAME=VALUE;
                    int semiPos = header.indexOf(';', i);
                    valueEnd = i = semiPos > 0 ? semiPos : headerLen;
                    break;
                } else {
                    i++;
                }

                if (i == headerLen) {
                    // NAME (no value till the end of string)
                    nameEnd = headerLen;
                    valueBegin = valueEnd = -1;
                    break;
                }
            }

            if (rfc2965Style && (header.regionMatches(nameBegin, RFC2965_PATH, 0, RFC2965_PATH.length()) ||
                    header.regionMatches(nameBegin, RFC2965_DOMAIN, 0, RFC2965_DOMAIN.length()) ||
                    header.regionMatches(nameBegin, RFC2965_PORT, 0, RFC2965_PORT.length()))) {

                // skip obsolete RFC2965 fields
                continue;
            }

            Cookie cookie = initCookie(header, nameBegin, nameEnd, valueBegin, valueEnd);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }

        return cookies;
    }

}