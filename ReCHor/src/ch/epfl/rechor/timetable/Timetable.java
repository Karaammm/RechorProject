package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Public transport timetable
 *
 * @author Ibrahim Khokher (361860)
 */
public interface Timetable {

    /**
     *
     * @return the indexed stations of the timetable
     */
    Stations stations();

    /**
     *
     * @return the indexed alternative name of the stations in the timetable
     */
    StationAliases stationAliases();

    /**
     *
     * @return the indexed tracks/platforms of the timetable
     */
    Platforms platforms();

    /**
     *
     * @return the indexed lines of the schedule
     */
    Routes routes();

    /**
     *
     * @return the indexed changes of the timetable
     */
    Transfers transfers();

    /**
     *
     * @param date the day
     * @return the indexed races on the active schedule of the given day
     */
    Trips tripsFor(LocalDate date);

    /**
     *
     * @param date the day
     * @return the indexed connections on the active schedule of the given day
     */
    Connections connectionsFor(LocalDate date);

    /**
     *
     * @param stopId the stop index
     * @return if the given stop index is a station index
     */
    default boolean isStationId(int stopId){
        return stopId < stations().size();
    }

    /**
     *
     * @param stopId the stop index
     * @return if the given stop index is a platform/track index
     */
    default boolean isPlatformId(int stopId){
        return stopId >= stations().size();
    }

    /**
     *
     * @param stopId the stop index
     * @return the station index of the given stop index
     */
    default int stationId(int stopId){
        return isStationId(stopId) ? stopId : stopId - stations().size();
    }

    /**
     *
     * @param stopId the stop index
     * @return the name of the track/platform or null if it is a station
     */
    default String platformName(int stopId){
        return isPlatformId(stopId) ? platforms().name(stopId) : null;
    }

}
