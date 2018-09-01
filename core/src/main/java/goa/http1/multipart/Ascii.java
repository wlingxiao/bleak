package goa.http1.multipart;

public class Ascii {

    public static int toLower(int c) {
        return (c >= 'A' && c <= 'Z') ? (c - 'A' + 'a') : (c & 0xff);
    }

}
