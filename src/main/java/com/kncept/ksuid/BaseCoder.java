package com.kncept.ksuid;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/**
 * This is intended to be a 'drop in' class to take and customise if required.
 * This includes moving the package.
 * If you do that, please leave this header, and credit nicholas.krul@gmail.com
 * Better yet - raise a PR
 */
public class BaseCoder {
    public static final String alphabet_base16 = "0123456789ABCDEF";
    public static final String alphabet_base62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    //https://en.wikipedia.org/wiki/Base64
    public static final String alphabet_base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
// !"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_ // uuencoding

//    public static final BaseCoder base16Encoder = new BaseCoder(alphabet_base16);
    public static final BaseCoder base62Encoder = new BaseCoder(alphabet_base62);
    public static final BaseCoder base64Encoder = new ByteGroupingBaseCoder(3, 4, alphabet_base64, "");
    public static final BaseCoder base64EncoderWithPadding = new ByteGroupingBaseCoder(3, 4, alphabet_base64, "=");

    private final String alphabet;

    // use bigintegers and basic maths for a reliable enc/dec
    // be consistent with zeros and empty strings
    public BaseCoder(String alphabet) {
        this.alphabet = alphabet;
    }

    public byte[] decode (String input) {
        if (input == null || input.equals("")) return new byte[0];
        byte[] values = new byte[input.length()];
        for(int i = 0; i < values.length; i++)
            values[i] = (byte)(alphabet.indexOf(input.charAt(i)) & 0xFF);
        byte[] decoded = convertBase(values, alphabet.length(), 256);
        if (decoded.length == 0) return new byte[]{0};
        return decoded;
    }

    // TODO: something like this -  https://codegolf.stackexchange.com/questions/1620/arbitrary-base-conversion/21672#21672
    public String encode (byte[] input) {
        if (input == null || input.length == 0) return "";
        byte[] indicies = convertBase(input, 256, alphabet.length());

        StringBuilder accumulator = new StringBuilder();
        for(int i = 0; i < indicies.length; i++)
            accumulator.append(alphabet.charAt(indicies[i]));

        String encoded = accumulator.toString();
        if (encoded.isEmpty()) return "0";
        return encoded;
    }

    protected byte[] convertBase(byte[] fromNumber, int fromBase, int toBase) {
        return fullyReadAndConvert(fromNumber, fromBase, toBase);
    }

    // not efficient. Use maths and fully read into memory
    protected byte[] fullyReadAndConvert(byte[] fromNumber, int fromBase, int toBase) {
        if (fromBase == toBase) throw new IllegalArgumentException();
        if (fromBase < 0 || toBase < 0) throw new IllegalArgumentException();
        if (fromNumber == null || fromNumber.length == 0) throw new IllegalArgumentException();

        BigInteger fromBaseBigint = new BigInteger(Integer.toString(fromBase));
        BigInteger toBaseBigint = new BigInteger(Integer.toString(toBase));

        BigInteger number  = BigInteger.ZERO;
        for(byte in: fromNumber) {
            int digit = in & 0xFF; //Byte.toUnsignedInt(in);
            if (digit >= fromBase || digit < 0) throw new IllegalArgumentException("digit was " + digit + " in base " + fromBase); //invalid base!
            number = number.multiply(fromBaseBigint);
            number = number.add(new BigInteger(Integer.toString(digit)));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(number.compareTo(BigInteger.ZERO) > 0)  {
            BigInteger[] divisorAndRemainder = number.divideAndRemainder(toBaseBigint);
            baos.write((divisorAndRemainder[1].intValue() & 0xFF));
            number = divisorAndRemainder[0];
        }
        byte[] toNumber = baos.toByteArray();
        reverse(toNumber);
        return toNumber;

//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//
//        int accumulateSum = fromNumber[fromNumber.length - 1];
//        int accumulateFromBase = fromBase;
//
//        for(int index = fromNumber.length - 2; index >= 0; index--) {
//
//            accumulateSum += (fromBase * fromNumber[index]);
//
//            while (accumulateFromBase > toBase) {
//                int digit = accumulateSum % toBase;
//                baos.write(digit & 0xFF);
//                accumulateSum /= toBase;
//                accumulateFromBase /= toBase;
//            }
//
//        }
//
//        // drain any remaining digits.
//        // n.b. when accumulateFromBase == toBase, 1 will be the next accumulateFromBase (and it's terminated)
//        while (accumulateFromBase >= toBase) {
//            int digit = accumulateSum % toBase;
//            baos.write(digit & 0xFF);
//            accumulateSum /= toBase;
//            accumulateFromBase /= toBase;
//        }
//
//        return baos.toByteArray();
    }

    // https://stackoverflow.com/questions/12893758/how-to-reverse-the-byte-array-in-java#12893827
    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    // eg: base64
    private static class ByteGroupingBaseCoder extends BaseCoder {
        private final int encodeGroupSize;
        private final int decodeGroupSize;
        private final String padding;
        public ByteGroupingBaseCoder(int encodeGroupSize, int decodeGroupSize, String alphabet, String padding) {
            super(alphabet);
            this.encodeGroupSize = encodeGroupSize;
            this.decodeGroupSize = decodeGroupSize;
            this.padding = padding == null ? "" : padding;
        }

        @Override
        public String encode(byte[] input) {
            if (input == null || input.length == 0) return super.encode(input);
            byte[] group = new byte[encodeGroupSize];
            StringBuilder accumulator = new StringBuilder();
            // take input int chunks of encodeGroupSize
            for(int i = 0; i < input.length; i+= encodeGroupSize) {
                for(int j = 0; j < encodeGroupSize; j++) {
                    // fill the grouping - zero value if we don't match size 100%
                    group[j] = input.length <= i + j ? 0 : input[i + j];
                }
                String encoding = super.encode(group);

                if (i + encodeGroupSize > input.length) { // if not an exact block encoding match
                    for(int j = input.length & group.length; j < group.length; j++) {
                        group[j] = 1; // just the least sigificant bit. so that it doesn't change any partially encoded values
                    }
                    String byteZeroPaddedEncoding = encoding;
                    String byteOnePaddedEncoding = super.encode(group);

                    // when the first character changes, we either pad or truncate (empty pad).
                    int firstDifferentCharacter = 0;
                    while(byteZeroPaddedEncoding.charAt(firstDifferentCharacter) == byteOnePaddedEncoding.charAt(firstDifferentCharacter))
                        firstDifferentCharacter++;
                    encoding = encoding.substring(0, firstDifferentCharacter);
                    for(int j = 0; j < byteZeroPaddedEncoding.length() - firstDifferentCharacter; j++)
                        encoding = encoding + padding;
                }
                accumulator.append(encoding);
            }
            return accumulator.toString();
        }

        @Override
        public byte[] decode(String input) {
            if (input == null || input.isEmpty()) return super.decode(input);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            for (int i = 0; i < input.length(); i += decodeGroupSize) {
                String group = input.substring(i, Math.min(i + decodeGroupSize, input.length()));

                if (group.length() != decodeGroupSize || (!padding.isEmpty() && group.endsWith(padding))) {
                    // trim padding, so we only have to do this once.
                    if (!padding.isEmpty() && group.endsWith(padding)) {
                        while(group.endsWith(padding)) group = group.substring(0, group.length() - 1);
                    }

                    String zeroedGroup = group;
                    while (zeroedGroup.length() < decodeGroupSize) zeroedGroup = zeroedGroup + super.alphabet.charAt(0);
                    byte[] zeroedDecoding = super.decode(zeroedGroup);
                    String mostSignificantBitGroup = group; // MSB, going the other way
                    while (mostSignificantBitGroup.length() < decodeGroupSize) mostSignificantBitGroup = mostSignificantBitGroup + super.alphabet.charAt(super.alphabet.length() - 1);
                    byte[] mostSignificantBitDecoding = super.decode(mostSignificantBitGroup);

                    int indexAtWhichTheyDiffer = 0;
                    while(zeroedDecoding[indexAtWhichTheyDiffer] == mostSignificantBitDecoding[indexAtWhichTheyDiffer])
                        indexAtWhichTheyDiffer++;
                    for(int j = 0; j < indexAtWhichTheyDiffer; j++)
                        baos.write(zeroedDecoding[j]);
                } else {
                    byte[] decoded = super.decode(group);
                    for(int j = 0; j < decoded.length; j++)
                        baos.write(decoded[j]);
                }
            }
            return baos.toByteArray();
        }
    }
}
