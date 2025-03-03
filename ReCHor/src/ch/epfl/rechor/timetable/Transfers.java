package ch.epfl.rechor.timetable;

/**
 * Represents the changes
 *
 * @author Ibrahim Khokher (361860)
 */
public interface Transfers extends Indexed{

    /**
     *
     * @param id the index change
     * @return the index of the starting station
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    int depStationID(int id);

    /**
     *
     * @param id the index change
     * @return the duration
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    int minutes(int id);

    /**
     *
     * @param stationId the index of the arrival station
     * @return the packed interval of the index of changes
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    int arrivingAt(int stationId);

    /**
     *
     * @param depStationId departure station
     * @param arrStationId arrival station
     * @return the duration in minutes of the change between the two stations
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    int minutesBetween(int depStationId, int arrStationId);
}
