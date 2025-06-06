package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Represents a timetable stored in cache
 * @author Karam Fakhouri (374510)
 */
public class CachedTimeTable implements TimeTable{

    private final TimeTable delegate;
    private Connections connections;
    private Trips trips;
    private LocalDate connectionsDate;
    private LocalDate tripsDate;

    /**
     * Constructor of the cached timeTable
     * @param table table to cache
     */
    public CachedTimeTable(TimeTable table){
        delegate = table;
        connections = null;
        trips = null;
    }

    /**
     * @return the indexed stations of the timetable
     */
    @Override
    public Stations stations() {
        return delegate.stations();
    }

    /**
     * @return the indexed alternative name of the stations in the timetable
     */
    @Override
    public StationAliases stationAliases() {
        return delegate.stationAliases();
    }

    /**
     * @return the indexed tracks/platforms of the timetable
     */
    @Override
    public Platforms platforms() {
        return delegate.platforms();
    }

    /**
     * @return the indexed lines of the schedule
     */
    @Override
    public Routes routes() {
        return delegate.routes();
    }

    /**
     * @return the indexed changes of the timetable
     */
    @Override
    public Transfers transfers() {
        return delegate.transfers();
    }

    /**
     * @param date the day
     * @return the indexed trips on the active schedule of the given day
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        if (trips == null || !tripsDate.equals(date)){
            trips = delegate.tripsFor(date);
            tripsDate = date;
        }
        return trips;
    }

    /**
     * @param date the day
     * @return the indexed connections on the active schedule of the given day
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        if (connections == null || !connectionsDate.equals(date)){
            connections = delegate.connectionsFor(date);
            connectionsDate = date;
        }
        return connections;
    }
}
