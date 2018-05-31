package goa.http1.cookie;

import java.util.BitSet;

class CookieUtil {

    private static final BitSet VALID_COOKIE_VALUE_OCTETS = validCookieValueOctets();

    private static final BitSet VALID_COOKIE_NAME_OCTETS = validCookieNameOctets();

    private static BitSet validCookieValueOctets() {
        BitSet bits = new BitSet();
        bits.set(0x21);
        for (int i = 0x23; i <= 0x2B; i++) {
            bits.set(i);
        }
        for (int i = 0x2D; i <= 0x3A; i++) {
            bits.set(i);
        }
        for (int i = 0x3C; i <= 0x5B; i++) {
            bits.set(i);
        }
        for (int i = 0x5D; i <= 0x7E; i++) {
            bits.set(i);
        }
        return bits;
    }

    static CharSequence unwrapValue(CharSequence cs) {
        final int len = cs.length();
        if (len > 0 && cs.charAt(0) == '"') {
            if (len >= 2 && cs.charAt(len - 1) == '"') {
                // properly balanced
                return len == 2 ? "" : cs.subSequence(1, len - 1);
            } else {
                return null;
            }
        }
        return cs;
    }

    static int firstInvalidCookieValueOctet(CharSequence cs) {
        return firstInvalidOctet(cs, VALID_COOKIE_VALUE_OCTETS);
    }

    static int firstInvalidOctet(CharSequence cs, BitSet bits) {
        for (int i = 0; i < cs.length(); i++) {
            char c = cs.charAt(i);
            if (!bits.get(c)) {
                return i;
            }
        }
        return -1;
    }

    static int firstInvalidCookieNameOctet(CharSequence cs) {
        return firstInvalidOctet(cs, VALID_COOKIE_NAME_OCTETS);
    }

    private static BitSet validCookieNameOctets() {
        BitSet bits = new BitSet();
        for (int i = 32; i < 127; i++) {
            bits.set(i);
        }
        int[] separators = new int[]
                {'(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t'};
        for (int separator : separators) {
            bits.set(separator, false);
        }
        return bits;
    }

    static StringBuilder stringBuilder() {
        return new StringBuilder();
    }

    /**
     * @param buf a buffer where some cookies were maybe encoded
     * @return the buffer String without the trailing separator, or null if no cookie was appended.
     */
    static String stripTrailingSeparatorOrNull(StringBuilder buf) {
        return buf.length() == 0 ? null : stripTrailingSeparator(buf);
    }

    static String stripTrailingSeparator(StringBuilder buf) {
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

    static void add(StringBuilder sb, String name) {
        sb.append(name);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

    static void add(StringBuilder sb, String name, String val) {
        sb.append(name);
        sb.append((char) EQUALS);
        sb.append(val);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

    static void add(StringBuilder sb, String name, long val) {
        sb.append(name);
        sb.append((char) EQUALS);
        sb.append(val);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

    /**
     * Horizontal space
     */
    static final byte SP = 32;

    /**
     * Equals '='
     */
    static final byte EQUALS = 61;

    /**
     * Line feed character
     */
    public static final byte LF = 10;

    /**
     * Colon ':'
     */
    public static final byte COLON = 58;

    /**
     * Semicolon ';'
     */
    static final byte SEMICOLON = 59;
}
