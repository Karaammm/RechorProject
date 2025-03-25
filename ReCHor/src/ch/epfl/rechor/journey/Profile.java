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
public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront) {
    /**
     * Ensures immutability
     * 
     * @param timeTable
     * @param date
     * @param arrStationId
     * @param stationFront
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

        private ParetoFront.Builder[] stationBuilders;
        private ParetoFront.Builder[] tripBuilders;

        // private boolean setForStationHappen = false;
        // private boolean setForTripHappen = false;

        int stationNum;
        int tripNum;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationBuilders = new ParetoFront.Builder[stationNum];
            this.tripBuilders = new ParetoFront.Builder[tripNum];
        }

        public ParetoFront.Builder forStation(int stationId) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            // if(setForStationHappen){
            // return stationBuilders[stationId];
            // }else{
            // return null;
            // }
            return stationBuilders[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(stationBuilders.length, stationId);
            // setForStationHappen = true;
            stationBuilders[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            // if(setForTripHappen){
            // return tripBuilders[tripId];
            // }else{
            // return null;
            // }
            return tripBuilders[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            // setForTripHappen = true;
            tripBuilders[tripId] = builder;
        }

        public Profile build() {
            List<ParetoFront> stationFrontiers = Arrays.stream(stationBuilders)
                    .map(builder -> builder != null ? builder.build() : ParetoFront.EMPTY).toList();
            return new Profile(timeTable, date, arrStationId, stationFrontiers);
        }
    }

}
