package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * The router, responsible for creating a profile of all optimal journeys to get from any station
 * in the network to the given arrival station, on a given day
 * @param timeTable
 */
public record Router(TimeTable timeTable) {
    /**
     *
     * @param date given day
     * @param destinationId destination station
     * @return the profile ofall optimal journeys from any station to the destination station, on a
     * given day
     */
    public Profile profile(LocalDate date, int destinationId){
            Profile.Builder profile = new Profile.Builder(timeTable, date, destinationId);
            Connections connections = timeTable.connectionsFor(date);
            Transfers transfers = timeTable.transfers();
            int numberOfStations = timeTable.stations().size();

        int[] walkable = new int[numberOfStations];
            Arrays.fill(walkable,-1);
            int packedInterval = transfers.arrivingAt(destinationId);
            for(int i = PackedRange.startInclusive(packedInterval);
                    i < PackedRange.endExclusive(packedInterval); i++){
                walkable[transfers.depStationId(i)] = transfers.minutes(i);
            }

            //i is the current connection id
        for(int i = 0; i < connections.size() ; i++){
            ParetoFront.Builder pareto = new ParetoFront.Builder();
            int currentConnectionId = i;
            int currentTripId = connections.tripId(currentConnectionId);
            int currentArrStation = timeTable.stationId(connections.arrStopId(i));
            int currentDepStation = timeTable.stationId(connections.depStopId(i));


            // walk from arrival l to destination IF its walkable
            if(walkable[currentArrStation] != -1){
                pareto.add(PackedCriteria.pack(connections.arrMins(i) + walkable[currentArrStation],
                                               0,
                                               Bits32_24_8.pack(currentConnectionId, 0)));
            }
            // continue with next link
            ParetoFront.Builder tripFront = profile.forTrip(currentTripId);
            if (tripFront != null) {
                pareto.addAll(tripFront);
            }

            // change vehicle at the end of l
            ParetoFront.Builder stationFront = profile.forStation(currentArrStation);
            if(profile.forStation(currentArrStation) != null){
                profile.forStation(currentArrStation).forEach( t -> {
                    if(PackedCriteria.depMins(t) >= connections.arrMins(currentConnectionId)){
                        pareto.add(PackedCriteria.arrMins(t),
                                   PackedCriteria.changes(t) + 1,Bits32_24_8.pack(currentConnectionId,0));
                    }
                });
            }

            if(pareto.isEmpty()){
                continue;
            }
            // update from the border of the trip
            if(tripFront != null){
                tripFront.addAll(pareto);
            } else{
                profile.setForTrip(currentTripId, pareto);
            }

            int augmentedDepMins = connections.depMins(currentConnectionId);
            if(profile.forStation(currentDepStation) != null && profile.forStation(currentDepStation).fullyDominates(pareto,augmentedDepMins)){
                continue;
            }
            // update the borders of the station
            int allChanges = transfers.arrivingAt(currentDepStation);
            for (int j = PackedRange.startInclusive(allChanges);
                 j < PackedRange.endExclusive(allChanges); j++) {
                int d = connections.depMins(currentConnectionId) - transfers.minutes(j);
                int fromStation = transfers.depStationId(j);


                pareto.forEach(t -> {
                    int firstConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(t));
                    if (connections.tripId(firstConnectionId) != connections.tripId(currentConnectionId)) {
                        return;
                    }
                    int numOfIntermediateStops = intermediateStops(connections,
                                                                   currentConnectionId,
                                                                   firstConnectionId);
                    int updatedPayload = Bits32_24_8.pack(currentConnectionId,
                                                          numOfIntermediateStops);
                    long tuple = PackedCriteria.withDepMins(PackedCriteria.pack(PackedCriteria.arrMins(t),
                                                            PackedCriteria.changes(t),
                                                            updatedPayload),
                                                            d);
                    if (profile.forStation(fromStation) != null) {
                        profile.forStation(fromStation).add(tuple);
                    } else {
                        ParetoFront.Builder newFront = new ParetoFront.Builder().add(tuple);
                        profile.setForStation(fromStation, newFront);
                    }
                });
            }
        }
        return profile.build();
    }

    /**
     * Helper method to calculate the number of intermatiate stops between two connections
     * @param connections
     * @param from
     * @param to
     * @return
     */
    private int intermediateStops(Connections connections, int from, int to){
        int numOfStops = 0;
        while(from != to){
            from = connections.nextConnectionId(from);
            numOfStops++;
        }
        return numOfStops;
    }
}
