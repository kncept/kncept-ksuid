package com.kncept.ksuid;

import java.util.Base64;

// TODO: use a class dropin for this
//
public class ByteConverter {
// https://en.wikipedia.org/wiki/Base62
    private static String base62Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static String encodeBase64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }

    // toBase62
    public static String encodeBase62(byte[] data) {
        return BaseCoder.base62Encoder.encode(data);
//        return new String(codecBase62.encode(data), UTF8);
    }

    //fromBase62
    public static byte[] decodeBase62(String data) {
        return BaseCoder.base62Encoder.decode(data);
//        return codecBase62.decode(data.getBytes(UTF8));
    }

    public static String encodeBase16(byte[] value) {
//        return BaseCoder.base16Encoder.encode(value);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < value.length; i++) sb.append(twoCharacterHexValue(value[i]));
        return sb.toString();
    }

    public static byte[] decodeBase16(String value) {
//        return BaseCoder.base16Encoder.decode(value);
        byte[] b = new byte[value.length() / 2];
        for(int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseUnsignedInt(value, i * 2, (i + 1) * 2, 16);
        }
        return b;
    }

    public static byte[] padArray(byte padWith, byte[] data, int length) {
        if (data.length < length) {
            byte[] padded = new byte[length];
            for(int i = 0; i < length - data.length; i++) {
                padded[i] = padWith;
            }
            System.arraycopy(data, 0, padded, length - data.length, data.length);
            return padded;
        }
        return data;
    }

    public static byte[] encodeInt(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) ((value >> 24) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
//        result[0] = (byte) ((value & 0xFF000000) >> 24);
//        result[1] = (byte) ((value & 0x00FF0000) >> 16);
//        result[2] = (byte) ((value & 0x0000FF00) >> 8);
//        result[3] = (byte) ((value & 0x000000FF) >> 0);
        return result;
    }

    public static int decodeInt(byte[] value) {
        value = padArray((byte)0, value, 4);
        return
                value[0] << 24 |
                (value[1] & 0xFF) << 16 |
                (value[2] & 0xFF) << 8 |
                (value[3] & 0xFF);
    }

    public static byte[] join(byte[] prefix, byte[] suffix) {
        byte[] result = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(suffix, 0, result, prefix.length, suffix.length);
        return result;
    }

    private static String twoCharacterHexValue(byte value) {
        String string = Integer.toHexString(Byte.toUnsignedInt(value));
        if (string.length() == 1)
            return "0" + string;
        return string;
    }


}
