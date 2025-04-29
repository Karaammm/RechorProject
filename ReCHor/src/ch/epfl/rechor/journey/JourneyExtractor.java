package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/**
 * The journey extractor
 *
 * @author Karam Fakhouri (374510)
 */
public abstract class JourneyExtractor {

    /**
     * @param profile given profile
     * @param depStationId ID of departure station
     * @return List of Journeys from a given profile and a departure point
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront pf = profile.forStation(depStationId);
        pf.forEach((long criteria) -> journeys.add(buildJourney(profile, criteria, depStationId)));
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    /**
     * Helper method to initialise date
     * @param date
     * @param mins
     * @return
     */
    private static LocalDateTime initialiseDateTime(LocalDate date, int mins) {
        return date.atStartOfDay().plusMinutes(mins);
    }

    /**
     * Helper method to create a stop
     * @param table
     * @param stopId
     * @return
     */
    private static Stop buildStop(TimeTable table, int stopId) {
        int stationId = table.stationId(stopId);
        String platformName = table.platformName(stopId);
        String stationName = table.stations().name(stationId);
        double longitude = table.stations().longitude(stationId);
        double latitude = table.stations().latitude(stationId);
        return new Stop(stationName, platformName, longitude, latitude);
    }

    /**
     * Helper method to create a foot leg
     * @param table
     * @param date
     * @param depStopId
     * @param depMins
     * @param arrStopId
     * @return
     */
    private static Journey.Leg.Foot buildFootLeg(TimeTable table, LocalDate date, int depStopId,
                                                 int depMins, int arrStopId) {
        int arrStation = table.stationId(arrStopId);
        int depStation = table.stationId(depStopId);
        Stop depStop = buildStop(table, depStopId);
        Stop arrStop = buildStop(table, arrStopId);
        LocalDateTime depTime = initialiseDateTime(date, depMins);
        int minsBetween = table.transfers().minutesBetween(depStation,
                                                 arrStation);
        LocalDateTime arrTime = depTime.plusMinutes(minsBetween);
        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }

    /**
     * Helper method to create 1 intermediate stop
     * @param table
     * @param date
     * @param connections
     * @param connectionId
     * @return
     */
    private static Journey.Leg.IntermediateStop buildIntermediateStop(TimeTable table, LocalDate date, Connections connections,
                                                                      int connectionId) {
        int stopId = connections.arrStopId(connectionId);
        Stop stop = buildStop(table, stopId);
        int nextConnection = connections.nextConnectionId(connectionId);

        int depMins = connections.arrMins(connectionId);
        int arrMins = connections.depMins(nextConnection);
        LocalDateTime depTime = initialiseDateTime(date, depMins);
        LocalDateTime arrTime = initialiseDateTime(date, arrMins);

        return new Journey.Leg.IntermediateStop(stop, depTime, arrTime);
    }

    /**
     * Helper method to create a list of intermediate stops
     * @param table
     * @param date
     * @param connections
     * @param connectionId
     * @param NumOfStops
     * @return
     */
    private static List<Journey.Leg.IntermediateStop> buildIntermediateStops(
        TimeTable table, LocalDate date, Connections connections, int connectionId, int NumOfStops) {
        List<Journey.Leg.IntermediateStop> stops = new ArrayList<>();
        for (int i = 0; i < NumOfStops; i++) {
            stops.add(buildIntermediateStop(table, date, connections, connectionId));
            connectionId = connections.nextConnectionId(connectionId);
        }
        return stops;
    }

    /**
     * Helper method to calculate the final connection index
     * @param connections
     * @param connectionid
     * @param numOfStops
     * @return
     */
    private static int finalConnectionId(Connections connections, int connectionid,
                                         int numOfStops) {
        for (int i = 0; i < numOfStops; i++) {
            connectionid = connections.nextConnectionId(connectionid);
        }
        return connectionid;
    }

    /**
     * Helper method to create a transport leg
     * @param table
     * @param date
     * @param connections
     * @param trips
     * @param connectionId
     * @param numOfStops
     * @return
     */
    private static Journey.Leg.Transport buildTransport(TimeTable table, LocalDate date,
                                                        Connections connections, Trips trips,
                                                        int connectionId, int numOfStops) {
        Stop depStop = buildStop(table,  connections.depStopId(connectionId));

        int lastConnectionId = finalConnectionId(connections, connectionId, numOfStops);
        Stop arrStop = buildStop(table, connections.arrStopId(lastConnectionId));

        LocalDateTime depTime = initialiseDateTime(date, connections.depMins(connectionId));
        LocalDateTime arrTime = initialiseDateTime(date, connections.arrMins(lastConnectionId));

        List<Journey.Leg.IntermediateStop> intermediateStops= buildIntermediateStops(table, date ,connections ,connectionId, numOfStops);
        int tripId = connections.tripId(connectionId);
        int routeId = trips.routeId(tripId);
        return new Journey.Leg.Transport(depStop, depTime, arrStop, arrTime,
                                         intermediateStops, table.routes().vehicle(routeId),
                                         table.routes().name(routeId), trips.destination(tripId));
    }

    /**
     * Helper method to build a journey
     * @param profile
     * @param criteria
     * @param depStationId
     * @return
     */
    private static Journey buildJourney(Profile profile, Long criteria,
                                        int depStationId) {
        List<Journey.Leg> legs = new ArrayList<>();
        final TimeTable table = profile.timeTable();
        final LocalDate date = profile.date();
        final Connections connections = profile.connections();
        final Trips trips = profile.trips();

        int currentDepMins = PackedCriteria.depMins(criteria);
        int changes = PackedCriteria.changes(criteria);
        final int finalArrMins = PackedCriteria.arrMins(criteria);
        int payload = PackedCriteria.payload(criteria);

        int currentConnectionId = Bits32_24_8.unpack24(payload);
        int currentIntermediateStops = Bits32_24_8.unpack8(payload);
        int currentDepStopId = connections.depStopId(currentConnectionId);

        int finalArrivalStation = profile.arrStationId();
        int currentStop = currentDepStopId;
        int nextConnectionDepStopId = currentDepStopId;

        if (depStationId != table.stationId(currentDepStopId)) {
            legs.add(buildFootLeg(table, date, depStationId, currentDepMins, currentDepStopId));
        }

        boolean lastLegIsFoot = !legs.isEmpty();

        while (changes >= 0) {

            if (lastLegIsFoot || legs.isEmpty()) {
                legs.add(buildTransport(table, date, connections,trips,currentConnectionId, currentIntermediateStops));
                lastLegIsFoot = false;
                changes--;

                if(changes >= 0){
                    int exitConnectionId = finalConnectionId(connections,currentConnectionId,currentIntermediateStops);
                    currentStop = connections.arrStopId(exitConnectionId);

                    criteria = profile.forStation(table.stationId(currentStop)).get(finalArrMins,changes);
                    payload = PackedCriteria.payload(criteria);
                    currentConnectionId = Bits32_24_8.unpack24(payload);
                    currentIntermediateStops = Bits32_24_8.unpack8(payload);
                    currentDepMins = connections.arrMins(exitConnectionId);
                    nextConnectionDepStopId = connections.depStopId(currentConnectionId);
                }

            } else {
                legs.add(buildFootLeg( table, date, currentStop, currentDepMins, nextConnectionDepStopId));
                lastLegIsFoot = true;
                currentStop = nextConnectionDepStopId;
            }
        }

        final int finalConnectionId = finalConnectionId(connections, currentConnectionId, currentIntermediateStops);
        final int lastArrStopId = connections.arrStopId(finalConnectionId);

        if (finalArrivalStation != table.stationId(lastArrStopId)) {
            legs.add(buildFootLeg(table, date, lastArrStopId, connections.arrMins(finalConnectionId),
                                 finalArrivalStation));
        }
        return new Journey(legs);
    }
}


