package ch.epfl.rechor.timetable;

/**
 * Public transport stations
 * 
 * @author Karam Fakhouri (374510)
 */
public interface Stations extends Indexed {

    /**
     * Returns the name of the station with the given index
     * 
     * @return the name of the station with the given index
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    String name(int id);

    /**
     * Returns the longitude in degrees, of the given index station
     * 
     * @return the name of the station with the given index
     * 
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    double longitude(int id);

    /**
     * Returns the latitude in degrees, of the given index station
     * 
     * @return the name of the station with the given index
     * 
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    double latitude(int id);
}
