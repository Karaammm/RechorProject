package ch.epfl.rechor;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MyBits32_24_8Test {
    private int first24BitDigit = 1234;
    private int first8bitDigit = 44;
    private int second24BitDigit = 37458;
    private int second8BitDigit = 146;
    private int moreThan24BitDigit = 16777217;
    private int moreThan8BitDigit = 257;
    private int first32BitDigit = 315948;
    private int second32BitDigit = 9589394;

    @Test
    void packWorksWhenNormalEntry() {
        assertEquals(first32BitDigit, Bits32_24_8.pack(first24BitDigit, first8bitDigit));
        assertEquals(second32BitDigit, Bits32_24_8.pack(second24BitDigit, second8BitDigit));
    }

    @Test
    void packThrowsWhenMoreThan24Bits() {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(moreThan24BitDigit, first8bitDigit);
        });
    }

    @Test
    void packThrowsWhenMoreThan8Bits() {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits32_24_8.pack(first24BitDigit, moreThan8BitDigit);
        });
    }

    @Test
    void unpack24WorksWhenNormalEntry() {
        assertEquals(first24BitDigit, Bits32_24_8.unpack24(first32BitDigit));
        assertEquals(second24BitDigit, Bits32_24_8.unpack24(second32BitDigit));
    }

    @Test
    void unpack8WorksWhenNormalEntry() {
        assertEquals(first8bitDigit, Bits32_24_8.unpack8(first32BitDigit));
        assertEquals(second8BitDigit, Bits32_24_8.unpack8(second32BitDigit));
    }

}
