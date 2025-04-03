package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
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
     * @author Karam Fakhouri (374510)
     *
     *
     */
    public static final class Builder {

        TimeTable timeTable;
        LocalDate date;
        int arrStationId;

        private ParetoFront.Builder[] stationBuilders;
        private ParetoFront.Builder[] tripBuilders;
        private int stationBuildersLength;

        private boolean[] setForStationList;
        private boolean[] setForTripList;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            stationBuildersLength = stationBuilders.length;
            setForStationList = new boolean[timeTable.stations().size()];
            this.tripBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
            setForTripList = new boolean[timeTable.tripsFor(date).size()];
        }

        public ParetoFront.Builder forStation(int stationId) {
            Preconditions.checkIndex(stationBuildersLength, stationId);
            if (!setForStationList[stationId]) {
                return null;
            }
            return stationBuilders[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(stationBuildersLength, stationId);
            setForStationList[stationId] = true;
            stationBuilders[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            if (!setForTripList[tripId]) {
                return null;
            }
            return tripBuilders[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            Preconditions.checkIndex(tripBuilders.length, tripId);
            setForTripList[tripId] = true;
            tripBuilders[tripId] = builder;
        }

        public Profile build() {
            List<ParetoFront> paretoFrontiers = new ArrayList<>(stationBuildersLength);
            for (int i = 0; i < stationBuildersLength; i++) {
                if (stationBuilders[i] == null) {
                    paretoFrontiers.add(i, ParetoFront.EMPTY);
                } else {
                    paretoFrontiers.add(i, stationBuilders[i].build());
                }
            }
            return new Profile(timeTable, date, arrStationId, paretoFrontiers);
        }
    }

}
