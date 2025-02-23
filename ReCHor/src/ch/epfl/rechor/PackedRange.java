package ch.epfl.rechor;

public abstract class PackedRange {
    public static int pack(int startInclusive, int endExclusive) {
        int length = endExclusive - startInclusive;
        Preconditions.checkArgument(length >= 0);
        return Bits32_24_8.pack(startInclusive, length);
    }

    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    public static int endExclusive(int interval) {
        return Bits32_24_8.unpack24(interval) + Bits32_24_8.unpack8(interval);
    }
}
