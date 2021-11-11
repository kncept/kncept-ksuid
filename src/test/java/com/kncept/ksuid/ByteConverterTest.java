package com.kncept.ksuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteConverterTest {

    @Test
    public void integerToBase62AndBackShortDecoding() {
        byte data[] = ByteConverter.encodeInt(1);
        Assertions.assertArrayEquals(new byte[]{0,0,0,1}, data);
        int value = ByteConverter.decodeInt(data);
        assertEquals(1, value);

        // should still work
        value = ByteConverter.decodeInt(new byte[]{1});
        assertEquals(1, value);

        value = ByteConverter.decodeInt(new byte[]{-1});
        assertEquals(255, value);
    }

    @Test
    public void checkSomeInts() {
        assertReconstruction(0);
        assertReconstruction(1);
        assertReconstruction(-1);
        assertReconstruction(4);
        assertReconstruction(45426);
        assertReconstruction(-234);
        assertReconstruction(Integer.MAX_VALUE);
        assertReconstruction(Integer.MIN_VALUE);
    }

//    @Test
    public void convertsIntegerToArrayAndBackAgain() {
        Random random = new SecureRandom();
        for(int i = 0; 1 < 10; i++) { // not going to solution space this.
            int value = random.nextInt();
            assertReconstruction(value);
        }
    }
    private void assertReconstruction(int value) {
        int reconstructed = ByteConverter.decodeInt(ByteConverter.encodeInt(value));
        assertEquals(value, reconstructed);
    }

}
