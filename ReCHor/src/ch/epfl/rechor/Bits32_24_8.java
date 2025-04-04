package ch.epfl.rechor;

/**
 * Static Methods for ease of bit Manipulation
 *
 * @author Karam Fakhouri (374510)
 * @author Ibrahim Khokher(361860)
 */
public abstract class Bits32_24_8 {

    /**
     *
     * @param bits24 24-bit vector
     * @param bits8  8-bit vector
     * @return returns a 32 bit int with the high 24 bits as bits24 and the low 8 as bits8
     * @throws IllegalArgumentException if both bits are not the required length
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits24 >> 24) == 0);
        Preconditions.checkArgument((bits8 >> 8) == 0);
        int result = (bits24 << 8) | bits8;
        return result;
    }

    /**
     *
     * @param bits32 32-bit vector
     * @return returns the value of the high 24 bits
     */
    public static int unpack24(int bits32) {
        return bits32 >>> 8;
    }

    /**
     *
     * @param bits32 32-bit vector
     * @return returns the value of the low 8 bits
     */
    public static int unpack8(int bits32) {
        int mask = 0xFF;
        return bits32 & mask;
    }
}
