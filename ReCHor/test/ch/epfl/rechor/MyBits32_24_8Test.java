package ch.epfl.rechor;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class MyBits32_24_8Test {
    private int first24BitDigit = 1234;
    private int first8bitDigit = 44;

    @Test
    void packWorksWhenNormalEntry() {
        assertEquals(315948, Bits32_24_8.pack(first24BitDigit, first8bitDigit));
    }
}
