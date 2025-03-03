package ch.epfl.rechor.timetable;

/**
 * Indexed connections
 *
 * @author Ibrahim Khokher (361860)
 */
public interface Connections extends Indexed{

    /**
     *
     * @param id the index
     * @return the index of the starting stop of the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int depStopId(int id);

    /**
     *
     * @param id the index
     * @return the start time of the given index link (in minutes after midnight)
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int depMins(int id);

    /**
     *
     * @param id the index
     * @return the index of the arrival time of the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int arrStopId(int id);

    /**
     *
     * @param id the index
     * @return the arrival time of the index (in minutes after midnight)
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int arrMins(int id);

    /**
     *
     * @param id the index
     * @return the index of the line of which the given index belongs to
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int tripId(int id);

    /**
     *
     * @param id the index
     * @return the position of the connection with the given index in the line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int tripPos(int id);

    /**
     *
     * @param id the index
     * @return the index of the connection following the one with the given index in the line to which it belongs, or
     * the index of the first connection in the line if the connection with the given index is the last in the line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than or equal to the size returned by
     * size()
     */
    int nextConnectionId(int id);

}
