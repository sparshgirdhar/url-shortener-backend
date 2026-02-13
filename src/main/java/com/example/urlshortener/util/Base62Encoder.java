package com.example.urlshortener.util;

public final class Base62Encoder {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62.length();

    private Base62Encoder() {}

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }

        if (value == 0) {
            return String.valueOf(BASE62.charAt(0));
        }

        StringBuilder result = new StringBuilder();

        while (value > 0) {
            result.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }

        return result.reverse().toString();
    }

    public static long decode(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        long result = 0;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int index = BASE62.indexOf(c);

            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + c);
            }

            result = result * BASE + index;
        }

        return result;
    }
}
