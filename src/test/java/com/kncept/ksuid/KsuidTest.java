package com.kncept.ksuid;


import com.kncept.ksuid.utils.BaseCoder;
import com.kncept.ksuid.utils.ByteConverter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static com.kncept.ksuid.Ksuid.EPOCH_SECONDS;
import static org.junit.jupiter.api.Assertions.*;

public class KsuidTest {

    @Test
    public void zeroRepresentation() {
        Ksuid ksuid = Ksuid.MINIMUM_KSUID;
        assertEquals(Ksuid.base62Length, ksuid.toBase62().length());
        assertEquals("000000000000000000000000000", ksuid.toBase62());
        assertEquals(ksuid.toBase62(), ksuid.toString());
        assertEquals(Ksuid.base16Length, ksuid.toBase16().length());
        assertEquals("0000000000000000000000000000000000000000", ksuid.toBase16());

        assertEquals(0, ksuid.getRawKsuidEpoch());
        for(byte value: ByteConverter.encodeInt(ksuid.getRawKsuidEpoch())) {
            assertEquals(0, value);
        }

        assertEquals(13, ksuid.getTime().getDayOfMonth());
        assertEquals(5, ksuid.getTime().getMonthValue());
        assertEquals(2014, ksuid.getTime().getYear());

        for(byte value: ksuid.getEntropy()) {
            assertEquals(0, value);
        }
    }

    @Test
    public void maximumRepresentation() {
        Ksuid ksuid = Ksuid.MAXIMUM_KSUID;
        assertEquals(Ksuid.base62Length, ksuid.toBase62().length());
        assertEquals("aWgEPTl1tmebfsQzFP4bxwgy80V", ksuid.toBase62());
        assertEquals(ksuid.toBase62(), ksuid.toString());
//        assertEquals(Ksuid.base16Length, ksuid.toBase16().length());
        assertEquals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", ksuid.toBase16());

        assertEquals(-1, ksuid.getRawKsuidEpoch());
        for(byte value: ByteConverter.encodeInt(ksuid.getRawKsuidEpoch())) {
            assertEquals(255, value & 0xFF);
        }

        assertEquals(13, ksuid.getTime().getDayOfMonth());
        assertEquals(5, ksuid.getTime().getMonthValue());
        assertEquals(2014, ksuid.getTime().getYear());

        for(byte value: ksuid.getEntropy()) {
            assertEquals(255, value & 0xFF);
        }
    }

    // https://github.com/segmentio/ksuid#inspect-the-components-of-a-ksuid
    @Test
    public void assertCompatabilityWithExample1() {
        assertKsuidFields(
                "0ujtsYcgvSTl8PAuAdqWYSMnLOv",
                "0669F7EFB5A1CD34B5F99D1154FB6853345C9735",
                107608047,
                "B5A1CD34B5F99D1154FB6853345C9735"
        );
    }

    // https://github.com/segmentio/ksuid#generate-a-ksuid-and-inspect-its-components
    @Test
    public void assertCompatabilityWithExample2() {
        assertKsuidFields(
                "0ujzPyRiIAffKhBux4PvQdDqMHY",
                "066A029C73FC1AA3B2446246D6E89FCD909E8FE8",
                107610780,
                "73FC1AA3B2446246D6E89FCD909E8FE8"
        );
    }

    // https://github.com/segmentio/ksuid#generate-ksuids-and-output-json-using-template-formatting
    @Test
    public void assertCompatibilityWithExample3() {
        assertKsuidFields(
                "0uk1Hbc9dQ9pxyTqJ93IUrfhdGq",
                null,
                107611700,
                "9850EEEC191BF4FF26F99315CE43B0C8"
        );
        assertKsuidFields(
                "0uk1HdCJ6hUZKDgcxhpJwUl5ZEI",
                null,
                107611700,
                "CC55072555316F45B8CA2D2979D3ED0A"
        );
        assertKsuidFields(
                "0uk1HcdvF0p8C20KtTfdRSB9XIm",
                null,
                107611700,
                "BA1C205D6177F0992D15EE606AE32238"
        );
        assertKsuidFields(
                "0uk1Ha7hGJ1Q9Xbnkt0yZgNwg3g",
                null,
                107611700,
                "67517BA309EA62AE7991B27BB6F2FCAC"
        );
    }

    @Test
    public void timeComponentIsNotReEncoded() {
        Ksuid initial = new Ksuid();
        Ksuid resulting = new Ksuid(initial.toString());
        assertEquals(initial.getRawKsuidEpoch(), resulting.getRawKsuidEpoch());
    }

    @Test
    public void timeComponentIsCorrect() {
        // resolution is to the SECOND, so we can use an int (stored as 32 bits) rather than a long
        // moment 0 is offset by EPOCH_SECONDS = 1400000000from UTC
        int ts1 = (int)(Instant.now().toEpochMilli() / 1000) - EPOCH_SECONDS;
        Ksuid ksuid = new Ksuid();
        int ts2 = (int)(Instant.now().toEpochMilli() / 1000) - EPOCH_SECONDS;


        System.out.println(" " + ts1 + " " + ksuid.getRawKsuidEpoch() + " " + ts2);

        assertTrue(ksuid.getRawKsuidEpoch() >= ts1);
        assertTrue(ksuid.getRawKsuidEpoch() <= ts2);

    }

    public void assertKsuidFields(
            String base62Value,
            String base16Value,
            int rawTimestamp,
            String base16Entropy
    ) {
        assertEquals(Ksuid.base62Length, base62Value.length());
        if (base16Value != null) assertEquals(Ksuid.base16Length, base16Value.length());
        Ksuid ksuid = new Ksuid(base62Value);
        assertEquals(base62Value, ksuid.toBase62());
        assertEquals(rawTimestamp, ksuid.getRawKsuidEpoch());
        if (base16Value != null) assertEquals(base16Value, ksuid.toBase16());
        byte[] expectedEntropy = BaseCoder.base16Encoder.decode(base16Entropy);
        assertEquals(Ksuid.entropyLength, expectedEntropy.length);
        assertArrayEquals(expectedEntropy, ksuid.getEntropy());
    }

    @Test
    public void timeConverterIsAccurate() {
        // have to trim this to the second
        LocalDateTime utcTime = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime ksuidTime = new Ksuid().getTime();
        assertTrue(utcTime.isBefore(ksuidTime) || utcTime.isEqual(ksuidTime));
        LocalDateTime endUtcTime = utcTime.plusSeconds(10); // MUST be quicker than this.
        assertTrue(endUtcTime.isAfter(ksuidTime));

    }
}
