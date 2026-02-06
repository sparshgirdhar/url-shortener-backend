package com.example.urlshortener.util;

public final class Base62Encoder {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62.length();

    private Base62Encoder() {}

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }

        StringBuilder result = new StringBuilder();

        while (value > 0) {
            result.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }

        return result.reverse().toString();
    }
}
