package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

public abstract class PackedCriteria {

    private static int SHIFT = 240;

    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument((changes >> 7 == 0) && (arrMins >= -240) && (arrMins < 48 * 60));
        int shiftedArr = arrMins + SHIFT;
        long longPayload = Integer.toUnsignedLong(payload);
        long arrMinsLong = Integer.toUnsignedLong(shiftedArr) << 39;
        long changesLong = Integer.toUnsignedLong(changes) << 32;
        return arrMinsLong | changesLong | longPayload;
    }

    public static boolean hasDepMins(long criteria) {
        return (criteria >>> 51) != 0;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int stored = (int) ((criteria >> 51) & 0xFFF);
        return 0xFFF - stored + 240;
    }

    public static int arrMins(long criteria) {
        int mask = (1 << 12) - 1;
        return (int) ((criteria >> 39) & mask) - 240;
    }

    public static int changes(long criteria) {
        int mask = (1 << 7) - 1;
        return (int) ((criteria >> 32) & mask);
    }

    public static int payload(long criteria) {
        long mask = (1l << 32) - 1;
        return (int) (criteria & mask);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        return (!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2)) &
                (arrMins(criteria1) <= arrMins(criteria2)) &
                (changes(criteria1) <= changes(criteria2));
    }

    public static long withoutDepMins(long criteria) {
        long mask = (1L << 52) - 1;
        return criteria & mask;
    }

    public static long withDepMins(long criteria, int depMins1) {
        Preconditions.checkArgument((depMins1 >= -240) && (depMins1 < 48 * 60));
        int depMins = 4095 - depMins1 + 240;
        long depMinsLong = ((long) depMins) << 51;
        return withoutDepMins(criteria) | depMinsLong;
    }

    public static long withAdditionalChange(long criteria) {
        return criteria + (1L << 32);
    }

    public static long withPayload(long criteria, int payload1) {
        long payloadMask = (1l << 32) - 1;
        long withoutPayload = criteria & ~payloadMask;
        return withoutPayload | (Integer.toUnsignedLong(payload1));
    }
}
