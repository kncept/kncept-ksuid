package com.kncept.ksuid.utils;

/**
 * A small set of byte based utilities required for ksuids
 */
public class ByteConverter {

    /** left pads an array if required.
     *
     * @param padWith the value to pad the array with - typically 0x00 or 0xFF
     * @param data the source array
     * @param length the desired array
     * @return an array of length (or more), padded to the left is nessesary
     */
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

    /** Encodes an integer into a 4 byte array
     *
     * @param value int to encode
     * @return byte array encoding the int
     */
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

    /** byte maths - reads an int from the first 4 bytes of the array. <br>
     * will zero pad the beginning if a short array is passed in
     *
     * @param value byte array to decode
     * @return int value
     */
    public static int decodeInt(byte[] value) {
        value = padArray((byte)0, value, 4);
        return
                value[0] << 24 |
                (value[1] & 0xFF) << 16 |
                (value[2] & 0xFF) << 8 |
                (value[3] & 0xFF);
    }

    /** Joins two arrays together
     *
     * @param prefix start of new array
     * @param suffix end of new array
     * @return prefix + suffix
     */
    public static byte[] join(byte[] prefix, byte[] suffix) {
        byte[] result = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(suffix, 0, result, prefix.length, suffix.length);
        return result;
    }
}
