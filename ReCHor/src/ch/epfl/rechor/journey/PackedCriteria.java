package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

public abstract class PackedCriteria {

    private static int SHIFT = 240;

    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(changes >> 8 == 0);
        Preconditions.checkArgument(arrMins >> 12 == 0);
        long result = 0;
        long longPayload = Integer.toUnsignedLong(payload);
        result = result | arrMins << 39 | changes << 32 | longPayload;
        return result;
    }

    public static boolean hasDepMins(long criteria) {
        if (criteria << 51 == 0) {
            return false;
        }
        return true;
    }

    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int result = ((int) criteria >> 51) + SHIFT;
        return result;
    }

    public static int arrMins(long criteria) {
        long mask = ((1L << 12) - 1) << 39;
        int result = (int) (criteria & mask) + 240;
        return result;
    }

    public static int changes(long criteria) {
        long mask = ((1L << 7) - 1) << 32;
        return (int) (criteria & mask);
    }

    public static int payload(long criteria) {
        long mask = (1L << 33) - 1;
        return (int) (criteria & mask);
    }

    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) & hasDepMins(criteria2));
        int complement1 = 4095 - depMins(criteria1);
        int complement2 = 4095 - depMins(criteria2);
        if (complement1 <= complement2 & arrMins(criteria1) <= arrMins(criteria2)
                & changes(criteria1) <= changes(criteria2)) {
            return true;
        } else {
            return false;
        }
    }

    public static long withoutDepMins(long criteria) {
        long mask = (1L << 52) - 1;
        return criteria & mask;
    }

    public static long withDepMins(long criteria, int depMins1) {
        int depMins = depMins1 << 51;
        return criteria | depMins;
    }

    public static long withAdditionalChange(long criteria) {
        long mask = 1L << 32;
        return criteria + mask;
    }

    public static long withPayload(long criteria, int payload1) {
        long payloadNew = Integer.toUnsignedLong(payload1);
        return criteria | payloadNew;
    }
}
