package ch.epfl.rechor.timetable;

/**
 * Indexed public transport rides
 *
 * @author Ibrahim Khokher (361860)
 */
public interface Trips extends Indexed {

    /**
     * Returns the index of the row to which the given index race belongs
     * 
     * @param id the index
     * @return the index of the line
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by
     *                                   size()
     */
    int routeId(int id);

    /**
     *
     * @param id the index
     * @return the name of the final destination
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by
     *                                   size()
     */
    String destination(int id);

}
