package com.kncept.ksuid.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static com.kncept.ksuid.utils.BaseCoder.alphabet_base16;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseCoderTest {
    Charset utf8 = Charset.forName("UTF8");

    @Test
    public void alphabetLengths() {
        assertEquals(16, alphabet_base16.length());
        assertEquals(62, BaseCoder.alphabet_base62.length());
        assertEquals(64, BaseCoder.alphabet_base64.length());
    }

    @Test
    public void base62ZeroHandling() {
        assertZeroHandling(BaseCoder.base62Encoder);
    }

    @Test
    public void base16ZeroHandling() {
        assertZeroHandling(BaseCoder.base16Encoder);
    }

    private void assertZeroHandling(BaseCoder baseCoder) {
        assertEquals("0", baseCoder.encode(new byte[1]));
        assertEquals("0", baseCoder.encode(new byte[20]));
        assertArrayEquals(new byte[]{0}, baseCoder.decode("0"));
        assertArrayEquals(new byte[]{0}, baseCoder.decode("000000000000000000000000000"));
    }

    @Test
    public void base62EmptyHandling() {
        assertEmptyHandling(BaseCoder.base62Encoder);
    }

    @Test
    public void base16EmptyHandling() {
        assertEmptyHandling(BaseCoder.base16Encoder);
    }

    private void assertEmptyHandling(BaseCoder baseCoder) {
        assertEquals("", baseCoder.encode(new byte[0]));
        assertEquals("", baseCoder.encode(null));
        assertArrayEquals(new byte[]{}, baseCoder.decode(""));
        assertArrayEquals(new byte[]{}, baseCoder.decode(null));
    }

    @Test
    // examples from https://github.com/seruco/base62
    public void base62Compatability() {
        BaseCoder baseCoder = BaseCoder.base62Encoder;
        String encoded = baseCoder.encode("Hello World".getBytes(utf8));
        Assertions.assertEquals("73XpUgyMwkGr29M", encoded);
        byte decoded[] = baseCoder.decode("73XpUgyMwkGr29M");
        Assertions.assertEquals("Hello World", new String(decoded, utf8));
    }

    @Test
    public void simpleConvertBase() {
        BaseCoder baseCoder = BaseCoder.base62Encoder;

        assertArrayEquals(new byte[]{1,0,1}, baseCoder.convertBase(new byte[]{5}, 8, 2));
        assertArrayEquals(new byte[]{5}, baseCoder.convertBase(new byte[]{1,0,1}, 2, 8));

        assertArrayEquals(new byte[]{1,0,1,0}, baseCoder.convertBase(new byte[]{1,0}, 10, 2));
        assertArrayEquals(new byte[]{1,0}, baseCoder.convertBase(new byte[]{1,0, 1, 0}, 2, 10));

        assertArrayEquals(new byte[]{6, 4}, baseCoder.convertBase(new byte[]{1,0, 0}, 10, 16));
        assertArrayEquals(new byte[]{1, 4, 4}, baseCoder.convertBase(new byte[]{1,0, 0}, 10, 8));

        // just to prove leading zeros do nothing ;)
        assertArrayEquals(new byte[]{6, 4}, baseCoder.convertBase(new byte[]{0, 0, 1,0, 0}, 10, 16));
    }

    @Test
    public void encodeSomeIntegers() {
        BaseCoder baseCoder = BaseCoder.base62Encoder;
        asssertIntegerReconstruction(baseCoder, 0);
        asssertIntegerReconstruction(baseCoder, 1);
        asssertIntegerReconstruction(baseCoder, -1);
        asssertIntegerReconstruction(baseCoder, 23);
        asssertIntegerReconstruction(baseCoder, 255);
        asssertIntegerReconstruction(baseCoder, 256);
        asssertIntegerReconstruction(baseCoder, 1345123);
        asssertIntegerReconstruction(baseCoder, Integer.MAX_VALUE);
        asssertIntegerReconstruction(baseCoder, Integer.MIN_VALUE);

    }
    private void asssertIntegerReconstruction(BaseCoder baseCoder, int value) {
        byte[] valueBytes = ByteConverter.encodeInt(value);
        String encoded = baseCoder.encode(valueBytes);
        byte[] valueReconstructed = ByteConverter.padArray((byte)0, baseCoder.decode(encoded), 4);
        assertArrayEquals(valueBytes, valueReconstructed);
    }

    @Test
    public void b64PrefixPadding() {
        // examples taken from https://en.wikipedia.org/wiki/Base64
        BaseCoder baseCoder = BaseCoder.base64EncoderWithPadding;
        assertEquals("TWFu", baseCoder.encode("Man".getBytes(utf8)));
        assertEquals("Man", new String(baseCoder.decode("TWFu"), utf8));
        assertEquals("TWE=", baseCoder.encode("Ma".getBytes(utf8)));
        assertEquals("Ma", new String(baseCoder.decode("TWE="), utf8));
        assertEquals("TQ==", baseCoder.encode("M".getBytes(utf8)));
        assertEquals("M", new String(baseCoder.decode("TQ=="), utf8));
    }

    @Test
    public void b64PrefixNoPadding() {
        // examples taken from https://en.wikipedia.org/wiki/Base64
        BaseCoder baseCoder = BaseCoder.base64Encoder;
        assertEquals("TWFu", baseCoder.encode("Man".getBytes(utf8)));
        assertEquals("Man", new String(baseCoder.decode("TWFu"), utf8));
        assertEquals("TWE", baseCoder.encode("Ma".getBytes(utf8)));
        assertEquals("Ma", new String(baseCoder.decode("TWE"), utf8));
        assertEquals("TQ", baseCoder.encode("M".getBytes(utf8)));
        assertEquals("M", new String(baseCoder.decode("TQ"), utf8));
    }

    @Test
    public void base64Compatability() {
        BaseCoder baseCoder = BaseCoder.base64Encoder;
        // examples taken from https://en.wikipedia.org/wiki/Base64
        assertEquals(
                "TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu",
                baseCoder.encode("Many hands make light work.".getBytes(utf8))
        );
        assertArrayEquals(
                "Many hands make light work.".getBytes(utf8),
                baseCoder.decode("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu")
        );

        baseCoder = BaseCoder.base64EncoderWithPadding;
        assertEquals(
                "aHR0cHM6Ly93d3cuYmV0dGVyLWNvbnZlcnRlci5jb20vQmFzZTY0LUVuY29kZQ==",
                baseCoder.encode("https://www.better-converter.com/Base64-Encode".getBytes(utf8))
        );
        assertArrayEquals(
                "https://www.better-converter.com/Base64-Encode".getBytes(utf8),
                baseCoder.decode("aHR0cHM6Ly93d3cuYmV0dGVyLWNvbnZlcnRlci5jb20vQmFzZTY0LUVuY29kZQ==")
        );
    }

    @Test
    public void base62Compatibility() {
        BaseCoder baseCoder = BaseCoder.base62Encoder;

        // https://github.com/seruco/base62
        assertEquals("73XpUgyMwkGr29M", baseCoder.encode("Hello World".getBytes(utf8)));
        assertEquals("Hello World", new String(baseCoder.decode("73XpUgyMwkGr29M"), utf8));

        assertEquals("UMNTpGxZW0IadmWS", baseCoder.encode("Kncept Ksuid".getBytes(utf8)));
        assertEquals("Kncept Ksuid", new String(baseCoder.decode("UMNTpGxZW0IadmWS"), utf8));
        assertEquals(
                "OFdrUKABqhgqpDiiXQjFqvi8BIv5DEPYoIzgRilzpmpD9X3quWRhcT51jHhcvcrFG1GohGXDM32Q1lwvhxQcGj",
                baseCoder.encode("https://www.better-converter.com/Encoders-Decoders/Base62-Encode".getBytes(utf8))
        );
        assertEquals(
                "https://www.better-converter.com/Encoders-Decoders/Base62-Encode",
                new String(baseCoder.decode("OFdrUKABqhgqpDiiXQjFqvi8BIv5DEPYoIzgRilzpmpD9X3quWRhcT51jHhcvcrFG1GohGXDM32Q1lwvhxQcGj"), utf8)
        );
    }

    @Test
    public void base16Compatability() {
        BaseCoder baseCoder = BaseCoder.base16Encoder;

        assertEquals("48656c6c6f20576f726c64".toUpperCase(), baseCoder.encode("Hello World".getBytes(utf8)));
        assertEquals("Hello World", new String(baseCoder.decode("48656c6c6f20576f726c64".toUpperCase()), utf8));

        assertEquals(
                "68747470733a2f2f7777772e6265747465722d636f6e7665727465722e636f6d2f456e636f646572732d4465636f646572732f41736369692d546f2d4865782d456e636f646572".toUpperCase(),
                baseCoder.encode("https://www.better-converter.com/Encoders-Decoders/Ascii-To-Hex-Encoder".getBytes(utf8))
        );
        assertEquals(
                "https://www.better-converter.com/Encoders-Decoders/Ascii-To-Hex-Encoder",
                new String(baseCoder.decode("68747470733a2f2f7777772e6265747465722d636f6e7665727465722e636f6d2f456e636f646572732d4465636f646572732f41736369692d546f2d4865782d456e636f646572".toUpperCase()), utf8)
        );
    }

}
