package goa.http1.multipart;

class Constants {

    // -------------------------------------------------------------- Constants

    /**
     * CR.
     */
    public static final byte CR = (byte) '\r';


    /**
     * LF.
     */
    public static final byte LF = (byte) '\n';


    /**
     * SP.
     */
    public static final byte SP = (byte) ' ';


    /**
     * HT.
     */
    public static final byte HT = (byte) '\t';


    /**
     * COMMA.
     */
    public static final byte COMMA = (byte) ',';


    /**
     * COLON.
     */
    public static final byte COLON = (byte) ':';


    /**
     * SEMI_COLON.
     */
    public static final byte SEMI_COLON = (byte) ';';


    /**
     * 'A'.
     */
    public static final byte A = (byte) 'A';


    /**
     * 'a'.
     */
    public static final byte a = (byte) 'a';


    /**
     * 'Z'.
     */
    public static final byte Z = (byte) 'Z';


    /**
     * '?'.
     */
    public static final byte QUESTION = (byte) '?';


    /**
     * Lower case offset.
     */
    public static final byte LC_OFFSET = A - a;


    // START SJSAS 6328909
    /**
     * The default response-type
     */
    public final static String DEFAULT_RESPONSE_TYPE =
            /*"text/html; charset=iso-8859-1"*/ null;

    public final static String CHUNKED_ENCODING = "chunked";

    public static final String FORM_POST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static final int KEEP_ALIVE_TIMEOUT_IN_SECONDS = 30;
    /**
     * Default max keep-alive count.
     */
    public static final int DEFAULT_MAX_KEEP_ALIVE = 256;

}
