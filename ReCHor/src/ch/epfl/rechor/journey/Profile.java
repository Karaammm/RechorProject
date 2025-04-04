package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Karam Fakhouri (374510)
 * 
 *         A profile
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId,
                      List<ParetoFront> stationFront) {
    /**
     * Ensures immutability
     * 
     * @param timeTable of public transport
     * @param date of the connections
     * @param arrStationId arrival station ID
     * @param stationFront list of ParetoFrontiers for all the stations
     */
    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * @return the connections corresponding to the profile
     */
    public Connections connections() {
        return timeTable.connectionsFor(date);
    }

    /**
     * @return the trips corresponding to the profile
     */
    public Trips trips() {
        return timeTable.tripsFor(date);
    }

    /**
     * 
     * @param stationId given index
     * @return Pareto frontier for the station with the given index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public ParetoFront forStation(int stationId) {
        Preconditions.checkIndex(stationFront.size(), stationId);
        return stationFront.get(stationId);
    }

    /**
     * @author Ibrahim Khokher (361860)
     * 
     * 
     */
    public static final class Builder {

        TimeTable timeTable;
        LocalDate date;
        int arrStationId;

        private final ParetoFront.Builder[] stationBuilders;
        private final ParetoFront.Builder[] tripBuilders;

        private final boolean[] stationSet;
        private final boolean[] tripSet;



        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            int stationNum = timeTable.stations().size();
            int tripNum = timeTable.tripsFor(date).size();
            this.stationBuilders = new ParetoFront.Builder[stationNum];
            this.tripBuilders = new ParetoFront.Builder[tripNum];
            this.stationSet = new boolean[stationNum];
            this.tripSet = new boolean[tripNum];
        }

        public ParetoFront.Builder forStation(int stationId) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            return stationSet[stationId] ? stationBuilders[stationId] : null;
        }

        public void setForStation(int stationId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            stationBuilders[stationId] = builder;
            stationSet[stationId] = true;
        }

        public ParetoFront.Builder forTrip(int tripId) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            return tripSet[tripId] ? tripBuilders[tripId] : null;
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            tripBuilders[tripId] = builder;
            tripSet[tripId] = true;
        }

        public Profile build() {
            List<ParetoFront> stationFrontiers = Arrays.stream(stationBuilders)
                    .map(builder -> builder != null ? builder.build()
                        : ParetoFront.EMPTY).toList();
            return new Profile(timeTable, date, arrStationId, stationFrontiers);
        }

    }

}
