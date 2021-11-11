package com.kncept.ksuid;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

public class Ksuid {
    public static final int EPOCH_SECONDS = 1400000000;
    private final byte[] data;

    public static final Ksuid MINIMUM_KSUID = new Ksuid("000000000000000000000000000");
    public static final Ksuid MAXIMUM_KSUID = new Ksuid("aWgEPTl1tmebfsQzFP4bxwgy80V");

    private static SecureRandom random = new SecureRandom();
    protected static final int tsLength = 4;
    protected static final int entropyLength = 16;
    protected static final int totalLength = 20;
    protected static final int base62Length = 27;
    protected static final int base16Length = 40;

    public Ksuid() {
        this((String)null);
    }
    public Ksuid(int rawKsuidEpoch, byte[] entropy) {
        if (entropy == null || entropy.length != entropyLength) throw new IllegalArgumentException("Incorrect Entropy");
        this.data = toArray(rawKsuidEpoch, entropy);
    }
    public Ksuid(byte[] value) {
        if (value == null) {
            this.data = generateInitialData();
        } else if (value.length == totalLength) {
            this.data = value;
        } else {
            throw new IllegalArgumentException("Unable to construct a Ksuid");
        }
    }
    public Ksuid(String value) {
        if (value == null || value.equals("")) {
            this.data = generateInitialData();
        } else if (value.length() == base62Length) {
            this.data = unpackMinimalArray(ByteConverter.decodeBase62(value));
        } else if (value.length() == base16Length) {
            this.data = unpackMinimalArray(ByteConverter.decodeBase16(value));
        } else {
            throw new IllegalArgumentException("Unable to construct a Ksuid from " + value);
        }
    }

    private byte[] unpackMinimalArray(byte[] data) {
        if (data.length < totalLength) {
            data = ByteConverter.padArray((byte)0, data, totalLength);
        }
        return data;
    }

    private byte[] generateInitialData() {
        // UTC based timestamp
        int ts = (int)(Instant.now().toEpochMilli() / 1000);
        ts -= EPOCH_SECONDS;
        byte[] entropy = new byte[entropyLength];
        random.nextBytes(entropy);
        return toArray(ts, entropy);
    }

    private byte[] toArray(int ts, byte[] entropy) {
        return ByteConverter.join(ByteConverter.encodeInt(ts), entropy);
    }

    public String toBase16() {
//        return ByteConverter.encodeBase16(data);
        return padPrefixToLength("0", ByteConverter.encodeBase16(data).toUpperCase(), base16Length);

    }

    public String toBase62() {
//        return ByteConverter.encodeBase62(data);
        return padPrefixToLength("0", ByteConverter.encodeBase62(data), base62Length);
    }

    public String toBase64() {
        return ByteConverter.encodeBase64(data);
    }


    public int getRawKsuidEpoch() {
        return ByteConverter.decodeInt(data);
    }

    public LocalDateTime getTime() {
        return LocalDateTime.ofEpochSecond(getRawKsuidEpoch() + (long) EPOCH_SECONDS, 0, ZoneOffset.UTC);
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
}
