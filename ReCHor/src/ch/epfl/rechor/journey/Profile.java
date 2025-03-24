package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public record Profile(TimeTable timeTable, LocalDate date, int arrStationId, List<ParetoFront> stationFront){








    public static final class Builder {

        TimeTable timeTable;
        LocalDate date;
        int arrStationId;

        private ParetoFront.Builder[] stationBuilders;
        private ParetoFront.Builder[] tripBuilders;

//        private boolean setForStationHappen = false;
//        private boolean setForTripHappen = false;

        int stationNum;
        int tripNum;

        public Builder(TimeTable timeTable, LocalDate date, int arrStationId){
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationBuilders = new ParetoFront.Builder[stationNum];
            this.tripBuilders = new ParetoFront.Builder[tripNum];
        }

        public ParetoFront.Builder forStation(int stationId) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
//            if(setForStationHappen){
//                return stationBuilders[stationId];
//            }else{
//                return null;
//            }
            return stationBuilders[stationId];
        }

        public void setForStation(int stationId, ParetoFront.Builder builder) {
            if (stationId < 0 || stationId >= stationBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
//            setForStationHappen = true;
            stationBuilders[stationId] = builder;
        }

        public ParetoFront.Builder forTrip(int tripId) {
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
//            if(setForTripHappen){
//                return tripBuilders[tripId];
//            }else{
//                return null;
//            }
            return tripBuilders[tripId];
        }

        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            if (tripId < 0 || tripId >= tripBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
//            setForTripHappen = true;
            tripBuilders[tripId] = builder;
        }

        public Profile build() {
            List<ParetoFront> stationFrontiers = Arrays.stream(stationBuilders).map(builder ->
                            builder != null ? builder.build() : ParetoFront.EMPTY).toList();
            return new Profile(timeTable, date, arrStationId, stationFrontiers);
        }
    }

}
