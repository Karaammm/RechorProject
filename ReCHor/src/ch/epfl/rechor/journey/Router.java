package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * The Router class is responsible for creating a profile of all optimal
 * journeys from any station in the network to a given destination station on a
 * specific day. It uses timetable data to compute the best routes based on
 * various criteria such as departure and arrival times, number of changes, and
 * intermediate stops.
 *
 * @param timeTable The timetable containing all connections and transfers.
 */
public record Router(TimeTable timeTable) {

    /**
     * Generates a profile of all optimal journeys to the specified destination
     * station on the given date.
     *
     * @param date          The date for which the profile is generated.
     * @param destinationId The ID of the destination station.
     * @return A Profile object containing all optimal journeys.
     */
    public Profile profile(LocalDate date, int destinationId) {
        Profile.Builder profile = new Profile.Builder(timeTable, date, destinationId);
        Connections connections = timeTable.connectionsFor(date);
        Transfers transfers = timeTable.transfers();
        int numberOfStations = timeTable.stations().size();

        int[] walkable = initializeWalkableArray(transfers, destinationId, numberOfStations);

        // i is the current connection id
        for (int i = 0; i < connections.size(); i++) {
            ParetoFront.Builder pareto = new ParetoFront.Builder();
            int currentConnectionId = i;
            int currentTripId = connections.tripId(currentConnectionId);
            int currentArrStation = timeTable.stationId(connections.arrStopId(i));
            int currentDepStation = timeTable.stationId(connections.depStopId(i));

            handleWalkableStations(pareto, walkable, currentArrStation, connections, currentConnectionId);

            handleTripFront(pareto, profile, currentTripId);

            handleStationFront(pareto, profile, currentArrStation, connections, currentConnectionId);

            if (pareto.isEmpty()) {
                continue;
            }
            updateTripBorder(pareto, profile, currentTripId);

            int augmentedDepMins = connections.depMins(currentConnectionId);
            if (profile.forStation(currentDepStation) != null
                    && profile.forStation(currentDepStation).fullyDominates(pareto, augmentedDepMins)) {
                continue;
            }
            updateStationBorders(pareto, profile, transfers, connections, currentConnectionId, currentDepStation);
        }
        return profile.build();
    }

    /**
     * Initializes the walkable array, which stores the walking times from each
     * station to the destination station.
     *
     * @param transfers        The transfers data from the timetable.
     * @param destinationId    The ID of the destination station.
     * @param numberOfStations The total number of stations in the network.
     * @return An array of walking times to the destination station.
     */
    private int[] initializeWalkableArray(Transfers transfers, int destinationId, int numberOfStations) {
        int[] walkable = new int[numberOfStations];
        Arrays.fill(walkable, -1);
        int packedInterval = transfers.arrivingAt(destinationId);
        for (int i = PackedRange.startInclusive(packedInterval); i < PackedRange.endExclusive(packedInterval); i++) {
            walkable[transfers.depStationId(i)] = transfers.minutes(i);
        }
        return walkable;
    }

    /**
     * Handles the addition of walkable stations to the Pareto front.
     *
     * @param pareto              The Pareto front builder.
     * @param walkable            The array of walking times to the destination
     *                            station.
     * @param currentArrStation   The current arrival station.
     * @param connections         The connections data from the timetable.
     * @param currentConnectionId The ID of the current connection.
     */
    private void handleWalkableStations(ParetoFront.Builder pareto, int[] walkable, int currentArrStation,
            Connections connections, int currentConnectionId) {
        if (walkable[currentArrStation] != -1) {
            pareto.add(PackedCriteria.pack(connections.arrMins(currentConnectionId) + walkable[currentArrStation], 0,
                    Bits32_24_8.pack(currentConnectionId, 0)));
        }
    }

    /**
     * Adds the trips associated with the current trip ID to the Pareto front.
     *
     * @param pareto        The Pareto front builder.
     * @param profile       The profile builder containing trip data.
     * @param currentTripId The ID of the current trip.
     */
    private void handleTripFront(ParetoFront.Builder pareto, Profile.Builder profile, int currentTripId) {
        ParetoFront.Builder tripFront = profile.forTrip(currentTripId);
        if (tripFront != null) {
            pareto.addAll(tripFront);
        }
    }

    /**
     * Processes the station front for the current arrival station and adds relevant
     * journeys to the Pareto front.
     *
     * @param pareto              The Pareto front builder.
     * @param profile             The profile builder containing station data.
     * @param currentArrStation   The current arrival station.
     * @param connections         The connections data from the timetable.
     * @param currentConnectionId The ID of the current connection.
     */
    private void handleStationFront(ParetoFront.Builder pareto, Profile.Builder profile, int currentArrStation,
            Connections connections, int currentConnectionId) {
        ParetoFront.Builder stationFront = profile.forStation(currentArrStation);
        if (stationFront != null) {
            stationFront.forEach(t -> {
                if (PackedCriteria.depMins(t) >= connections.arrMins(currentConnectionId)) {
                    pareto.add(PackedCriteria.arrMins(t), PackedCriteria.changes(t) + 1,
                            Bits32_24_8.pack(currentConnectionId, 0));
                }
            });
        }
    }

    /**
     * Updates the trip border in the profile with the current Pareto front.
     *
     * @param pareto        The Pareto front builder.
     * @param profile       The profile builder containing trip data.
     * @param currentTripId The ID of the current trip.
     */
    private void updateTripBorder(ParetoFront.Builder pareto, Profile.Builder profile, int currentTripId) {
        ParetoFront.Builder tripFront = profile.forTrip(currentTripId);
        if (tripFront != null) {
            tripFront.addAll(pareto);
        } else {
            profile.setForTrip(currentTripId, pareto);
        }
    }

    /**
     * Updates the station borders in the profile with the current Pareto front.
     *
     * @param pareto              The Pareto front builder.
     * @param profile             The profile builder containing station data.
     * @param transfers           The transfers data from the timetable.
     * @param connections         The connections data from the timetable.
     * @param currentConnectionId The ID of the current connection.
     * @param currentDepStation   The current departure station.
     */
    private void updateStationBorders(ParetoFront.Builder pareto, Profile.Builder profile, Transfers transfers,
            Connections connections, int currentConnectionId, int currentDepStation) {
        int allChanges = transfers.arrivingAt(currentDepStation);
        for (int j = PackedRange.startInclusive(allChanges); j < PackedRange.endExclusive(allChanges); j++) {
            int d = connections.depMins(currentConnectionId) - transfers.minutes(j);
            int fromStation = transfers.depStationId(j);

            pareto.forEach(t -> {
                int firstConnectionId = Bits32_24_8.unpack24(PackedCriteria.payload(t));
                if (connections.tripId(firstConnectionId) != connections.tripId(currentConnectionId)) {
                    return;
                }
                int numOfIntermediateStops = intermediateStops(connections, currentConnectionId, firstConnectionId);
                int updatedPayload = Bits32_24_8.pack(currentConnectionId, numOfIntermediateStops);
                long tuple = PackedCriteria.withDepMins(
                        PackedCriteria.pack(PackedCriteria.arrMins(t), PackedCriteria.changes(t), updatedPayload), d);
                if (profile.forStation(fromStation) != null) {
                    profile.forStation(fromStation).add(tuple);
                } else {
                    ParetoFront.Builder newFront = new ParetoFront.Builder().add(tuple);
                    profile.setForStation(fromStation, newFront);
                }
            });
        }
    }

    /**
     * Calculates the number of intermediate stops between two connections.
     *
     * @param connections The connections data from the timetable.
     * @param from        The starting connection ID.
     * @param to          The ending connection ID.
     * @return The number of intermediate stops between the two connections.
     */
    private int intermediateStops(Connections connections, int from, int to) {
        int numOfStops = 0;
        while (from != to) {
            from = connections.nextConnectionId(from);
            numOfStops++;
        }
        return numOfStops;
    }
}
