package com.kncept.ksuid;

import com.kncept.ksuid.utils.BaseCoder;
import com.kncept.ksuid.utils.ByteConverter;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

/**
 * The Main Ksuid class/<br>
 *
 * Create new instances for 'now' timestamps, or pass in a <pre>ZonedDateTime</pre> for a specific point in time.<br>
 * Don't use the (int, byte[]) constructor unless you know what you are doing.<br/>
 */
public class Ksuid {
    /** Ksuid Epoc is adjusted. Approx. date is 13th May, 2014 */
    public static final int EPOCH_SECONDS = 1400000000;
    private final byte[] data;

    /** The smallest possible Ksuid = 20 bytes of 0x00 */
    public static final Ksuid MINIMUM_KSUID = new Ksuid("000000000000000000000000000");
    /** The largest possible Ksuid = 20 bytes of 0xFF */
    public static final Ksuid MAXIMUM_KSUID = new Ksuid("aWgEPTl1tmebfsQzFP4bxwgy80V");

    private static SecureRandom random = new SecureRandom();
    /** Bytes Length of the timestamp */
    protected static final int tsLength = 4;
    /** Bytes Length of the entropy */
    protected static final int entropyLength = 16;
    /** Bytes Length of the ksuid */
    protected static final int totalLength = 20;
    /** Encoded Length in base62 */
    protected static final int base62Length = 27;
    /** Encoded Length in base16 */
    protected static final int base16Length = 40;

    public Ksuid() {
        data = generateInitialData();
    }
    public Ksuid(int rawKsuidEpoch, byte[] entropy) {
        if (entropy == null) entropy = generateEntropy();
        if (entropy.length != entropyLength) throw new IllegalArgumentException("Incorrect Entropy");
        data = join(rawKsuidEpoch, entropy);
    }
    public Ksuid(byte[] value) {
        if (value == null) {
            data = generateInitialData();
        } else if (value.length == totalLength) {
            data = new byte[totalLength];
            System.arraycopy(value, 0, data, 0, totalLength);
        } else {
            throw new IllegalArgumentException("Unable to construct a Ksuid");
        }
    }
    public Ksuid(String value) {
        if (value == null || value.equals("")) {
            data = generateInitialData();
        } else if (value.length() == base62Length) {
            data = unpackMinimalArray(BaseCoder.base62Encoder.decode(value));
        } else if (value.length() == base16Length) {
            data = unpackMinimalArray(BaseCoder.base16Encoder.decode(value));
        } else {
            throw new IllegalArgumentException("Unable to construct a Ksuid from " + value);
        }
    }
    public Ksuid(ZonedDateTime when, byte[] entropy) {
        int ts = (int)when.toInstant().getEpochSecond();
        if (entropy == null) entropy = generateEntropy();
        if (entropy.length != entropyLength) throw new IllegalArgumentException("Incorrect Entropy");
        data = join(ts, entropy);
    }

    private byte[] unpackMinimalArray(byte[] data) {
        if (data.length < totalLength) {
            data = ByteConverter.padArray((byte)0, data, totalLength);
        }
        return data;
    }

    private byte[] generateInitialData() {
        int ts = (int)(ZonedDateTime.now(Clock.systemUTC()).toInstant().getEpochSecond() - EPOCH_SECONDS);
        return join(ts, generateEntropy());
    }

    private byte[] generateEntropy() {
        byte[] entropy = new byte[entropyLength];
        random.nextBytes(entropy);
        return entropy;
    }

    private byte[] join(int ts, byte[] entropy) {
        return ByteConverter.join(ByteConverter.encodeInt(ts), entropy);
    }

    public String toBase16() {
        return padPrefixToLength("0", BaseCoder.base16Encoder.encode(data), base16Length);
    }

    public String toBase62() {
        return padPrefixToLength("0", BaseCoder.base62Encoder.encode(data), base62Length);
    }

    public int getRawKsuidEpoch() {
        return ByteConverter.decodeInt(data);
    }

    public ZonedDateTime getTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(getRawKsuidEpoch() + (long) EPOCH_SECONDS), ZoneOffset.UTC);
    }

    public byte[] getEntropy() {
        byte[] entropy = new byte[entropyLength];
        System.arraycopy(data, tsLength, entropy, 0, entropyLength);
        return entropy;
    }

    @Override
    public String toString() {
        return toBase62();
    }

    // allow subclassing - eg: use as a PK in an ORM or PJA library
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ksuid)) return false;
        Ksuid ksuid = (Ksuid) o;
        return Arrays.equals(data, ksuid.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    private static String padPrefixToLength(String prefix, String value, int length) {
        while(value.length() < length) {
            value = prefix + value;
        }
        return value;
    }

    public static void main(String[] args) {
        System.out.println(new Ksuid());
    }
}
