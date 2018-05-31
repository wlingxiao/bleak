package goa.http1.cookie;

import goa.Cookie;
import goa.logging.Logger;
import goa.logging.Loggers;

import java.nio.CharBuffer;

import static goa.http1.cookie.CookieUtil.*;

abstract class CookieDecoder {

    private final Logger log = Loggers.getLogger(getClass());

    private final boolean strict;

    protected CookieDecoder(boolean strict) {
        this.strict = strict;
    }

    protected Cookie initCookie(String header, int nameBegin, int nameEnd, int valueBegin, int valueEnd) {
        if (nameBegin == -1 || nameBegin == nameEnd) {
            return null;
        }

        if (valueBegin == -1) {
            return null;
        }

        CharSequence wrappedValue = CharBuffer.wrap(header, valueBegin, valueEnd);
        CharSequence unwrappedValue = unwrapValue(wrappedValue);
        if (unwrappedValue == null) {
            return null;
        }

        final String name = header.substring(nameBegin, nameEnd);

        int invalidOctetPos;
        if (strict && (invalidOctetPos = firstInvalidCookieNameOctet(name)) >= 0) {
            return null;
        }

        final boolean wrap = unwrappedValue.length() != valueEnd - valueBegin;

        if (strict && (invalidOctetPos = firstInvalidCookieValueOctet(unwrappedValue)) >= 0) {
            return null;
        }
        Cookie cookie = Cookie.newCookie(name, unwrappedValue.toString());
        return cookie;
    }

}

