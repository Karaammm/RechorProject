package ch.epfl.rechor.timetable;

/**
 * Represents the indexed tracks/platforms
 * 
 * @author Karam Fakhouri (374510)
 */
public interface Platforms extends Indexed {

    /**
     * Returns the name of the track/platform, which can be empty
     * 
     * @return the name of the track/platform, which can be empty
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    String name(int id);

    /**
     * Returns the index of the station to which this track/platform belongs to
     * 
     * @return the index of the station to which this track/platform belongs to
     * @param id the index
     * @throws IndexOutOfBoundsException if the index is less than 0 or greater than
     *                                   or equal to the size returned by size()
     */
    int stationId(int id);
}
