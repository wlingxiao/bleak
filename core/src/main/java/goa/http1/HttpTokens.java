package goa.http1;

import goa.http1.BaseExceptions.BadCharacter;

public final class HttpTokens {
    static final char EMPTY_BUFF = 0xFFFF;

    static final char REPLACEMENT = 0xFFFD;

    static final char COLON = ':';
    static final char TAB = '\t';
    static final char LF = '\n';
    static final char CR = '\r';
    static final char SPACE = ' ';
    static final char[] CRLF = {CR, LF};
    static final char SEMI_COLON = ';';

    final static byte ZERO = '0';
    final static byte NINE = '9';
    final static byte A = 'A';
    final static byte F = 'F';
    final static byte Z = 'Z';
    final static byte a = 'a';
    final static byte f = 'f';
    final static byte z = 'z';

    public static int hexCharToInt(final char ch) throws BadCharacter {
        if (ZERO <= ch && ch <= NINE) {
            return ch - ZERO;
        } else if (a <= ch && ch <= f) {
            return ch - a + 10;
        } else if (A <= ch && ch <= F) {
            return ch - A + 10;
        } else {
            throw new BadCharacter("Bad hex char: " + (char) ch);
        }
    }

    public static boolean isDigit(final char ch) {
        return HttpTokens.NINE >= ch && ch >= HttpTokens.ZERO;
    }

    public static boolean isHexChar(byte ch) {
        return ZERO <= ch && ch <= NINE ||
                a <= ch && ch <= f ||
                A <= ch && ch <= F;
    }

    public static boolean isWhiteSpace(char ch) {
        return ch == HttpTokens.SPACE || ch == HttpTokens.TAB;
    }

}
