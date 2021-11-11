package com.kncept.ksuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.kncept.ksuid.BaseCoder.alphabet_base16;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseCoderTest {

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
        assertZeroHandling(new BaseCoder(alphabet_base16));
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
        assertEmptyHandling(new BaseCoder(alphabet_base16));
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
        String encoded = baseCoder.encode("Hello World".getBytes());
        Assertions.assertEquals("73XpUgyMwkGr29M", encoded);
        byte decoded[] = baseCoder.decode("73XpUgyMwkGr29M");
        Assertions.assertEquals("Hello World", new String(decoded));
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
        assertEquals("TWFu", baseCoder.encode("Man".getBytes()));
        assertEquals("Man", new String(baseCoder.decode("TWFu")));
        assertEquals("TWE=", baseCoder.encode("Ma".getBytes()));
        assertEquals("Ma", new String(baseCoder.decode("TWE=")));
        assertEquals("TQ==", baseCoder.encode("M".getBytes()));
        assertEquals("M", new String(baseCoder.decode("TQ==")));
    }

    @Test
    public void b64PrefixNoPadding() {
        // examples taken from https://en.wikipedia.org/wiki/Base64
        BaseCoder baseCoder = BaseCoder.base64Encoder;
        assertEquals("TWFu", baseCoder.encode("Man".getBytes()));
        assertEquals("Man", new String(baseCoder.decode("TWFu")));
        assertEquals("TWE", baseCoder.encode("Ma".getBytes()));
        assertEquals("Ma", new String(baseCoder.decode("TWE")));
        assertEquals("TQ", baseCoder.encode("M".getBytes()));
        assertEquals("M", new String(baseCoder.decode("TQ")));
    }

    @Test
    public void base64Compatability() {
        BaseCoder baseCoder = BaseCoder.base64Encoder;
        // examples taken from https://en.wikipedia.org/wiki/Base64
        assertEquals(
                "TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu",
                baseCoder.encode("Many hands make light work.".getBytes())
        );
        assertArrayEquals(
                "Many hands make light work.".getBytes(),
                baseCoder.decode("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu")
        );
    }

}
