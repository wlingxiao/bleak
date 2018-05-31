package goa.http1.cookie;

import static goa.http1.cookie.CookieUtil.*;

class CookieEncoder {

    protected final boolean strict;

    protected CookieEncoder(boolean strict) {
        this.strict = strict;
    }

    protected void validateCookie(String name, String value) {
        if (strict) {
            int pos;

            if ((pos = firstInvalidCookieNameOctet(name)) >= 0) {
                throw new IllegalArgumentException("Cookie name contains an invalid char: " + name.charAt(pos));
            }

            CharSequence unwrappedValue = unwrapValue(value);
            if (unwrappedValue == null) {
                throw new IllegalArgumentException("Cookie value wrapping quotes are not balanced: " + value);
            }

            if ((pos = firstInvalidCookieValueOctet(unwrappedValue)) >= 0) {
                throw new IllegalArgumentException("Cookie value contains an invalid char: " + value.charAt(pos));
            }
        }
    }
}
