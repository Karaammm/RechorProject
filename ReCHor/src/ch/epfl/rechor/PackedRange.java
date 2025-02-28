package ch.epfl.rechor;
/**
 * PackedRange
 *
 * @author Karam Fakhouri (374510)
 * @author Ibrahim Khokher(361860)
 */
public abstract class PackedRange {

    /**
     *
     * @param startInclusive the start range
     * @param endExclusive the end range
     * @return returns the value representing the range of integers
     * @throws IllegalArgumentException if its length cannot be represented with 8bits
     */
    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument(length >= 0);
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     *
     * @param interval given interval
     * @return returns the length of the interval
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     *
     * @param interval given interval
     * @return returns the smallest integer of the interval
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     *
     * @param interval given interval
     * @return returns the largest integer of the interval
     */
    public static int endExclusive(int interval) {
        return Bits32_24_8.unpack24(interval) + Bits32_24_8.unpack8(interval);
    }
}
