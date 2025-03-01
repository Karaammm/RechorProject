package ch.epfl.rechor.timetable;

/**
 * Represents alternative names of stations
 * 
 * @author Karam Fakhouri (374510)
 */
public interface StationAliases extends Indexed {
    /**
     * Returns the alternative name of given index station
     * 
     * @return the alternative name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    String alias(int id);

    /**
     * Returns the name of given index station
     * 
     * @return the name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    String stationName(int id);

}
