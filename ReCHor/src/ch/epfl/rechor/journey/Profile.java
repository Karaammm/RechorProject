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
 * A profile
 */
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId,
                      List<ParetoFront> stationFront) {
    /**
     * Constructor that ensures immutability
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
     * @author Karam Fakhouri (374510)
     *
     * Helper class to build a profile
     */
    public static final class Builder {

        TimeTable timeTable;
        LocalDate date;
        int arrStationId;

        private final ParetoFront.Builder[] stationBuilders;
        private final ParetoFront.Builder[] tripBuilders;

        private final boolean[] stationSet;
        private final boolean[] tripSet;


        /**
         * Constructor of the builder
         * @param timeTable given schedule
         * @param date given date
         * @param arrStationId destination station
         */
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

        /**
         * @param stationId station index
         * @return returns the pareto frontier builder for the given station index, which can be null if
         *         no call to setForStation has been made previously for the same station
         * @throws IndexOutOfBoundsException for an invalid index
         */
        public ParetoFront.Builder forStation(int stationId) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            return stationSet[stationId] ? stationBuilders[stationId] : null;
        }

        /**
         * associates the given pareto frontier builder with the given index station
         * @param stationId station index
         * @param builder pareto frontier builder
         * @throws IndexOutOfBoundsException for an invalid index
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            stationBuilders[stationId] = builder;
            stationSet[stationId] = true;
        }

        /**
         * @param tripId trip index
         * @return returns the pareto frontier builder for the given trip index, which can be null if
         *         no call to setForTrip has been made previously for the same trip
         * @throws IndexOutOfBoundsException for an invalid index
         */
        public ParetoFront.Builder forTrip(int tripId) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            return tripSet[tripId] ? tripBuilders[tripId] : null;
        }

        /**
         * associates the given pareto frontier builder with the given index trip
         * @param tripId trip index
         * @param builder pareto frontier builder
         * @throws IndexOutOfBoundsException for an invalid index
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            tripBuilders[tripId] = builder;
            tripSet[tripId] = true;
        }

        /**
         * Builds the profile
         * @return the built profile
         */
        public Profile build() {
            List<ParetoFront> stationFrontiers = Arrays.stream(stationBuilders)
                    .map(builder -> builder != null ? builder.build()
                        : ParetoFront.EMPTY).toList();
            return new Profile(timeTable, date, arrStationId, stationFrontiers);
        }

    }

}
