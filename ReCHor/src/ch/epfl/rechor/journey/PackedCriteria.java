package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * PackedCriteria
 *
 * @author Karam Fakhouri (374510)
 * @author Ibrahim Khokher(361860)
 */
public abstract class PackedCriteria {


    //The origin from which times are calculated
    private final static int SHIFT = 240;
    private final static int CHANGES_SHIFT = 7;
    private final static int PAYLOAD_SHIFT = 32;
    private final static int ARRIVAL_MINUTES_SHIFT = PAYLOAD_SHIFT + CHANGES_SHIFT;
    private final static int DEPARTURE_MINUTES_SHIFT = 51;
    private final static int COMPLEMENT_MINUTES_SHIFT = 0xFFF;
    private final static int MAX_MINS = 2880;


    /**
     *
     * @param arrMins arrival in minutes
     * @param changes amount of changes
     * @param payload payload
     * @return returns the packed value of type long of the given information
     * @throws IllegalArgumentException if arrival time is invalid or change is not
     *                                  in 7 bits
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument((changes >> CHANGES_SHIFT == 0)
                                        && (arrMins >= -SHIFT) && (arrMins < MAX_MINS));
        int shiftedArr = arrMins + SHIFT;
        long longPayload = Integer.toUnsignedLong(payload);
        long arrMinsLong = (long)(shiftedArr) << ARRIVAL_MINUTES_SHIFT;
        long changesLong = (long)(changes) << PAYLOAD_SHIFT;
        return arrMinsLong | changesLong | longPayload;
    }

    /**
     *
     * @param criteria packaged criteria
     * @return checks if the given value has a departure time
     */
    public static boolean hasDepMins(long criteria) {
        return ((criteria >>> DEPARTURE_MINUTES_SHIFT) & COMPLEMENT_MINUTES_SHIFT )!= 0;
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns the departure time
     * @throws IllegalArgumentException if it does not contain departure time
     * 
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        int stored = (int) (criteria >> DEPARTURE_MINUTES_SHIFT);
        return (COMPLEMENT_MINUTES_SHIFT - (COMPLEMENT_MINUTES_SHIFT & stored)) - SHIFT;
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns arrival time
     */
    public static int arrMins(long criteria) {
        int mask = (1 << 12) - 1;
        return (int) ((criteria >> ARRIVAL_MINUTES_SHIFT) & mask) - SHIFT;
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns the number of platform changes
     */
    public static int changes(long criteria) {
        int mask = (1 << CHANGES_SHIFT) - 1;
        return (int) ((criteria >> PAYLOAD_SHIFT) & mask);
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns the payload
     */
    public static int payload(long criteria) {
        long mask = (1L << PAYLOAD_SHIFT) - 1;
        return (int) (criteria & mask);
    }

    /**
     *
     * @param criteria1 first packaged criteria
     * @param criteria2 second packaged criteria
     * @return checks if the first packaged criteria dominates or is equal to the
     *         second
     * @throws IllegalArgumentException if one of the criteria has a start time and
     *                                  the other doesn't
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        return (!hasDepMins(criteria1) || depMins(criteria1) >= depMins(criteria2)) &
                (arrMins(criteria1) <= arrMins(criteria2)) &
                (changes(criteria1) <= changes(criteria2));
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns the packaged criteria without the departure time
     */
    public static long withoutDepMins(long criteria) {
        long mask =  ((long) COMPLEMENT_MINUTES_SHIFT << DEPARTURE_MINUTES_SHIFT);
        return criteria & ~mask;
    }

    /**
     *
     * @param criteria packaged criteria
     * @param depMins1 the departure time
     * @return returns the packaged criteria with the departure time
     * @throws IllegalArgumentException if it is an invalid departure time
     */
    public static long withDepMins(long criteria, int depMins1) {
        Preconditions.checkArgument((depMins1 >= -SHIFT) && (depMins1 < MAX_MINS));
        int depMins = COMPLEMENT_MINUTES_SHIFT - (depMins1 + SHIFT);
        long depMinsLong = ((long) depMins) << DEPARTURE_MINUTES_SHIFT;
        return withoutDepMins(criteria) | depMinsLong;
    }

    /**
     *
     * @param criteria packaged criteria
     * @return returns the packaged criteria with one more change
     */
    public static long withAdditionalChange(long criteria) {
        return criteria + (1L << PAYLOAD_SHIFT);
    }

    /**
     *
     * @param criteria packaged criteria
     * @param payload1 payload
     * @return returns the packaged criteria with the given payload
     */
    public static long withPayload(long criteria, int payload1) {
        long payloadMask = (1L << PAYLOAD_SHIFT) - 1;
        long withoutPayload = criteria & ~payloadMask;
        return withoutPayload | (Integer.toUnsignedLong(payload1));
    }
}
