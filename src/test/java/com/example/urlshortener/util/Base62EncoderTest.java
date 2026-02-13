package com.example.urlshortener.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void shouldEncodeZero() {
        assertEquals("0", Base62Encoder.encode(0));
    }

    @Test
    void shouldEncodePositiveNumbers() {
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("z", Base62Encoder.encode(35));
        assertEquals("Z", Base62Encoder.encode(61));
    }

    @Test
    void shouldEncodeLargeNumbers() {
        long largeNum = System.nanoTime();
        String encoded = Base62Encoder.encode(largeNum);

        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
        assertTrue(encoded.length() <= 11); // Reasonable length
    }

    @Test
    void shouldEncodeAndDecode() {
        long original = 123456789L;
        String encoded = Base62Encoder.encode(original);
        long decoded = Base62Encoder.decode(encoded);

        assertEquals(original, decoded);
    }
}
